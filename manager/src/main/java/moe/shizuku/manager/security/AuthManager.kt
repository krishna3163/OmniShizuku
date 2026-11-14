package moe.shizuku.manager.security

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.atomic.AtomicReference

object AuthManager : Application.ActivityLifecycleCallbacks {

    private const val AUTH_TYPES = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

    private val snapshotRef = AtomicReference(AuthSnapshot())

    val currentSnapshot: AuthSnapshot
        get() = snapshotRef.get()

    val currentAuthState: AuthState
        get() = snapshotRef.get().state

    @Volatile
    private var startedActivityCount: Int = 0

    private val androidClock = object : MonotonicClock {
        override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
    }

    fun dispatchEvent(event: AuthEvent) {
        snapshotRef.updateAndGet { current ->
            AuthStateMachine.reduce(current, event, androidClock)
        }
    }

    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(AUTH_TYPES)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isAppOpenSessionValid(): Boolean {
        if (!SecuritySettings.isAuthEnabled || !SecuritySettings.authOnAppOpen) return true
        val snapshot = snapshotRef.get()
        if (!snapshot.isAppOpenUnlocked) return false

        val session = snapshot.appOpenSession ?: return false
        if (!session.isValid(SystemClock.elapsedRealtime())) return false

        // If the app is currently in background or returned from background, verify timeout
        val bgTime = snapshot.backgroundTimestampElapsedRealtimeMs
        if (bgTime > 0L) {
            val elapsedMs = SystemClock.elapsedRealtime() - bgTime
            val timeoutSec = SecuritySettings.timeoutSeconds
            if (timeoutSec <= 0) return false
            val elapsedSec = elapsedMs / 1000
            if (elapsedSec >= timeoutSec) return false
        }
        return true
    }

    fun isSessionValid(): Boolean = isAppOpenSessionValid()

    fun markAppOpenAuthenticated() {
        val gen = snapshotRef.get().generation
        dispatchEvent(AuthEvent.AuthenticationSucceeded(generation = gen, timeoutSeconds = SecuritySettings.timeoutSeconds))
    }

    fun markAuthenticated() {
        markAppOpenAuthenticated()
    }

    fun isActionAuthenticatedInSession(action: SecuritySettings.ProtectedAction): Boolean {
        val now = SystemClock.elapsedRealtime()
        return snapshotRef.get().isActionAuthorized(action.name, now)
    }

    fun markActionAuthenticatedInSession(action: SecuritySettings.ProtectedAction) {
        val gen = snapshotRef.get().generation
        snapshotRef.updateAndGet { current ->
            val now = SystemClock.elapsedRealtime()
            val expiresAt = if (SecuritySettings.timeoutSeconds > 0) {
                now + (SecuritySettings.timeoutSeconds * 1000L)
            } else {
                Long.MAX_VALUE
            }
            val session = AuthSession(
                scope = AuthScope.PROTECTED_ACTION,
                actionName = action.name,
                authenticatedElapsedRealtimeMs = now,
                expiresElapsedRealtimeMs = expiresAt
            )
            val updated = current.actionSessions.toMutableMap()
            updated[action.name] = session
            current.copy(
                state = AuthState.AUTHENTICATED_ACTION_SESSION,
                actionSessions = updated
            )
        }
    }

    fun invalidateSession() {
        dispatchEvent(AuthEvent.InvalidateAll)
    }

    override fun onActivityStarted(activity: Activity) {
        if (startedActivityCount == 0) {
            dispatchEvent(AuthEvent.ActivityForegrounded(timeoutSeconds = SecuritySettings.timeoutSeconds))
        }
        startedActivityCount++
    }

    override fun onActivityStopped(activity: Activity) {
        val changing = activity.isChangingConfigurations
        startedActivityCount--
        if (startedActivityCount <= 0 && !changing) {
            startedActivityCount = 0
            dispatchEvent(AuthEvent.ActivityBackgrounded(isChangingConfig = false))
        } else if (changing) {
            dispatchEvent(AuthEvent.ConfigurationChangeStarted(changing = true))
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    fun executeWithAuth(
        activity: FragmentActivity,
        action: SecuritySettings.ProtectedAction,
        onSuccess: () -> Unit
    ) {
        val title = activity.getString(moe.shizuku.manager.R.string.security_auth_prompt_title)
        val subtitle = when (action) {
            SecuritySettings.ProtectedAction.DEVICE_OWNER -> activity.getString(moe.shizuku.manager.R.string.security_action_device_owner)
            SecuritySettings.ProtectedAction.PERMISSIONS -> activity.getString(moe.shizuku.manager.R.string.security_action_permissions)
            SecuritySettings.ProtectedAction.SERVER_TOGGLE -> activity.getString(moe.shizuku.manager.R.string.security_action_server)
            SecuritySettings.ProtectedAction.STUB_MANAGEMENT -> activity.getString(moe.shizuku.manager.R.string.security_action_stubs)
            SecuritySettings.ProtectedAction.APP_OPEN -> null
        }
        executeWithAuth(
            activity = activity,
            action = action,
            title = title,
            subtitle = subtitle,
            onSuccess = onSuccess
        )
    }

    fun executeWithAuth(
        activity: FragmentActivity,
        action: SecuritySettings.ProtectedAction,
        title: String,
        subtitle: String? = null,
        onSuccess: () -> Unit
    ) {
        if (!SecuritySettings.isActionProtected(action) || isActionAuthenticatedInSession(action)) {
            onSuccess()
            return
        }

        dispatchEvent(AuthEvent.RequestActionAuth(action.name))
        val currentGen = snapshotRef.get().generation

        authenticate(
            activity = activity,
            title = title,
            subtitle = subtitle,
            expectedGeneration = currentGen,
            onResult = { authenticated ->
                if (authenticated) {
                    markActionAuthenticatedInSession(action)
                    onSuccess()
                }
            }
        )
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        expectedGeneration: Long = snapshotRef.get().generation,
        onResult: (Boolean) -> Unit
    ) {
        if (!canAuthenticate(activity)) {
            // If device cannot authenticate (no lock screen set), allow gracefully
            markAuthenticated()
            onResult(true)
            return
        }

        dispatchEvent(AuthEvent.PromptDisplayed(expectedGeneration))

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                dispatchEvent(
                    AuthEvent.AuthenticationSucceeded(
                        generation = expectedGeneration,
                        timeoutSeconds = SecuritySettings.timeoutSeconds
                    )
                )
                onResult(true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                ) {
                    dispatchEvent(AuthEvent.AuthenticationCancelled(generation = expectedGeneration))
                } else if (errorCode == BiometricPrompt.ERROR_LOCKOUT ||
                    errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
                ) {
                    dispatchEvent(
                        AuthEvent.AuthenticationLockout(
                            generation = expectedGeneration,
                            permanent = errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
                        )
                    )
                } else {
                    dispatchEvent(AuthEvent.AuthenticationFailedSoft(generation = expectedGeneration, reason = errString.toString()))
                }
                onResult(false)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                dispatchEvent(AuthEvent.AuthenticationFailedSoft(generation = expectedGeneration, reason = "Soft failure"))
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                if (!subtitle.isNullOrBlank()) {
                    setSubtitle(subtitle)
                }
            }
            .setAllowedAuthenticators(AUTH_TYPES)
            .build()

        try {
            prompt.authenticate(promptInfo)
        } catch (_: Exception) {
            dispatchEvent(AuthEvent.AuthenticationFailedSoft(generation = expectedGeneration, reason = "Prompt error"))
            onResult(false)
        }
    }
}
