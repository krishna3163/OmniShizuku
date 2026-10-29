package moe.shizuku.manager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.shizuku.manager.ui.theme.ShizukuTheme

@OptIn(ExperimentalMaterial3Api::class)
class FirstLaunchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShizukuSettings.initialize(this)
        if (ShizukuSettings.isFirstLaunch()) {
            setContent {
                ShizukuTheme {
                    FirstLaunchOnboarding(
                        onFinish = {
                            ShizukuSettings.setFirstLaunch(false)
                            val intent = Intent(this@FirstLaunchActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        } else {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}

private enum class OnboardingStep {
    DISCLAIMER_1, DISCLAIMER_2, DISCLAIMER_3, DISCLAIMER_4, DISCLAIMER_5, DISCLAIMER_6,
    AGREEMENT, LANGUAGE, THEME, ACTIVATION, DONE
}

private data class DisclaimerPage(
    val title: Int,
    val body: Int,
    val iconRes: Int
)

@Composable
private fun FirstLaunchOnboarding(onFinish: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(OnboardingStep.DISCLAIMER_1.ordinal) }
    var agreed by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(5) }
    var timerRunning by remember { mutableStateOf(false) }

    val current = OnboardingStep.values()[step]

    val pages = listOf(
        DisclaimerPage(R.string.onboarding_title_1, R.string.onboarding_body_1, R.drawable.ic_info),
        DisclaimerPage(R.string.onboarding_title_2, R.string.onboarding_body_2, R.drawable.ic_info),
        DisclaimerPage(R.string.onboarding_title_3, R.string.onboarding_body_3, R.drawable.ic_info),
        DisclaimerPage(R.string.onboarding_title_4, R.string.onboarding_body_4, R.drawable.ic_info),
        DisclaimerPage(R.string.onboarding_title_5, R.string.onboarding_body_5, R.drawable.ic_info),
        DisclaimerPage(R.string.onboarding_title_6, R.string.onboarding_body_6, R.drawable.ic_info)
    )

    LaunchedEffect(current == OnboardingStep.AGREEMENT) {
        if (current == OnboardingStep.AGREEMENT && !timerRunning) {
            timerRunning = true
            timerSeconds = 5
            scope.launch {
                while (timerSeconds > 0) {
                    delay(1000)
                    timerSeconds = timerSeconds - 1
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.onboarding_title)) },
                navigationIcon = {
                    if (current != OnboardingStep.DISCLAIMER_1) {
                        IconButton(onClick = { if (step > 0) step = step - 1 }) {
                            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            when (current) {
                OnboardingStep.DISCLAIMER_1, OnboardingStep.DISCLAIMER_2, OnboardingStep.DISCLAIMER_3,
                OnboardingStep.DISCLAIMER_4, OnboardingStep.DISCLAIMER_5, OnboardingStep.DISCLAIMER_6 -> {
                    val page = pages[current.ordinal]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Icon(
                            imageVector = ImageVector.vectorResource(id = page.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(page.title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(page.body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        val isLast = current == OnboardingStep.DISCLAIMER_6
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { step = step + 1 }) {
                                Text(if (isLast) stringResource(R.string.onboarding_i_agree_proceed) else stringResource(R.string.onboarding_next))
                            }
                        }
                    }
                }

                OnboardingStep.AGREEMENT -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_agreement),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.onboarding_agreement_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.onboarding_agreement_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreed,
                                onCheckedChange = { agreed = it }
                            )
                            Text(
                                text = stringResource(R.string.onboarding_i_have_read_and_agree),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = (5 - timerSeconds).coerceAtLeast(0) / 5f
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (timerSeconds > 0) {
                                context.getString(R.string.onboarding_wait_seconds, timerSeconds)
                            } else {
                                stringResource(R.string.onboarding_can_proceed)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                if (agreed && timerSeconds <= 0) {
                                    step = step + 1
                                }
                            },
                            enabled = agreed && timerSeconds <= 0,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.onboarding_next))
                        }
                    }
                }

                OnboardingStep.LANGUAGE -> {
                    LanguageSelectionScreen(
                        onBack = { step = step - 1 },
                        onNext = { step = step + 1 }
                    )
                }

                OnboardingStep.THEME -> {
                    ThemeSelectionScreen(
                        onBack = { step = step - 1 },
                        onNext = { step = step + 1 }
                    )
                }

                OnboardingStep.ACTIVATION -> {
                    ActivationMethodScreen(
                        onBack = { step = step - 1 },
                        onNext = { step = step + 1 }
                    )
                }

                OnboardingStep.DONE -> {
                    onFinish()
                }
            }
        }
    }
}

@Composable
private fun LanguageSelectionScreen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var selected by remember { mutableStateOf(ShizukuSettings.getLanguageTag()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(
            imageVector = Icons.Outlined.Language,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_language_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_language_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val languageList = listOf(
                Pair("SYSTEM", stringResource(R.string.onboarding_language_system)),
                Pair("en", stringResource(R.string.onboarding_language_english)),
                Pair("hi", stringResource(R.string.onboarding_language_hindi)),
                Pair("es", stringResource(R.string.onboarding_language_spanish)),
                Pair("fr", stringResource(R.string.onboarding_language_french)),
                Pair("de", stringResource(R.string.onboarding_language_german)),
                Pair("zh", stringResource(R.string.onboarding_language_chinese)),
                Pair("ja", stringResource(R.string.onboarding_language_japanese)),
                Pair("ar", stringResource(R.string.onboarding_language_arabic)),
                Pair("ru", stringResource(R.string.onboarding_language_russian)),
                Pair("pt", stringResource(R.string.onboarding_language_portuguese)),
                Pair("id", stringResource(R.string.onboarding_language_indonesian))
            )
            languageList.forEach { (code, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { selected = code },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tintColor = if (selected == code) MaterialTheme.colorScheme.primary else Color.Unspecified
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = tintColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.onboarding_back))
            }
            Button(
                onClick = {
                    ShizukuSettings.setLanguageTag(selected)
                    onNext()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.onboarding_next))
            }
        }
    }
}

@Composable
private fun ThemeSelectionScreen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var nightMode by remember { mutableStateOf(ShizukuSettings.getNightMode()) }
    var useSystemColor by remember {
        mutableStateOf(ShizukuSettings.getPreferences().getBoolean(ShizukuSettings.Keys.KEY_USE_SYSTEM_COLOR, true))
    }
    var blackNight by remember {
        mutableStateOf(ShizukuSettings.getPreferences().getBoolean(ShizukuSettings.Keys.KEY_BLACK_NIGHT_THEME, false))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_theme_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_theme_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = nightMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                onClick = { nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM }
            )
            Column {
                Text(text = stringResource(R.string.onboarding_theme_follow_system), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.onboarding_theme_follow_system_summary), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = nightMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES,
                onClick = { nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES }
            )
            Column {
                Text(text = stringResource(R.string.onboarding_theme_dark), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.onboarding_theme_dark_summary), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = nightMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO,
                onClick = { nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO }
            )
            Column {
                Text(text = stringResource(R.string.onboarding_theme_light), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.onboarding_theme_light_summary), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = blackNight,
                onClick = { blackNight = true; nightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES }
            )
            Column {
                Text(text = stringResource(R.string.onboarding_theme_black_night), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.onboarding_theme_black_night_summary), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = useSystemColor,
                onCheckedChange = { useSystemColor = it }
            )
            Column {
                Text(text = stringResource(R.string.onboarding_theme_use_system_color), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.onboarding_theme_use_system_color_summary), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.onboarding_back))
            }
            Button(
                onClick = {
                    ShizukuSettings.setNightMode(nightMode)
                    ShizukuSettings.getPreferences().edit()
                        .putBoolean(ShizukuSettings.Keys.KEY_USE_SYSTEM_COLOR, useSystemColor)
                        .apply()
                    ShizukuSettings.getPreferences().edit()
                        .putBoolean(ShizukuSettings.Keys.KEY_BLACK_NIGHT_THEME, blackNight)
                        .apply()
                    onNext()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.onboarding_next))
            }
        }
    }
}

@Composable
private fun ActivationMethodScreen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var method by remember { mutableStateOf(ShizukuSettings.getActivationMethod()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(
            imageVector = Icons.Outlined.PowerSettingsNew,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_activation_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_activation_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = method == ShizukuSettings.ACTIVATION_WIRELESS,
                onClick = { method = ShizukuSettings.ACTIVATION_WIRELESS }
            )
            Column {
                Text(text = stringResource(R.string.onboarding_activation_wireless), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.onboarding_activation_wireless_summary), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = method == ShizukuSettings.ACTIVATION_ROOT,
                onClick = { method = ShizukuSettings.ACTIVATION_ROOT }
            )
            Column {
                Text(text = stringResource(R.string.onboarding_activation_root), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.onboarding_activation_root_summary), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = method == ShizukuSettings.ACTIVATION_COMPUTER,
                onClick = { method = ShizukuSettings.ACTIVATION_COMPUTER }
            )
            Column {
                Text(text = stringResource(R.string.onboarding_activation_computer), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.onboarding_activation_computer_summary), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.onboarding_back))
            }
            Button(
                onClick = {
                    ShizukuSettings.setActivationMethod(method)
                    ShizukuSettings.setStartMethod(
                        when (method) {
                            ShizukuSettings.ACTIVATION_ROOT -> ShizukuSettings.StartMethod.ROOT
                            ShizukuSettings.ACTIVATION_COMPUTER -> ShizukuSettings.StartMethod.USB
                            else -> ShizukuSettings.StartMethod.WIRELESS
                        }
                    )
                    onNext()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.onboarding_finish))
            }
        }
    }
}
