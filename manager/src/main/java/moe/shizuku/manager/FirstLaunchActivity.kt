package moe.shizuku.manager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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

private data class ThemeOption(
    val mode: Int,
    val titleRes: Int,
    val summaryRes: Int,
    val iconRes: Int
)

private data class ActivationOption(
    val mode: Int,
    val titleRes: Int,
    val summaryRes: Int,
    val iconRes: Int
)

private val DisclaimerIcons = listOf(
    R.drawable.ic_shield_24dp,
    R.drawable.ic_device_owner_24dp,
    R.drawable.ic_root_24dp,
    R.drawable.ic_warning_24,
    R.drawable.ic_wadb_24,
    R.drawable.ic_agreement
)

@Composable
private fun FirstLaunchOnboarding(onFinish: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(OnboardingStep.DISCLAIMER_1.ordinal) }
    var agreed by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(5) }

    val current = OnboardingStep.values()[step]

    val pages = listOf(
        DisclaimerPage(R.string.onboarding_title_1, R.string.onboarding_body_1, DisclaimerIcons[0]),
        DisclaimerPage(R.string.onboarding_title_2, R.string.onboarding_body_2, DisclaimerIcons[1]),
        DisclaimerPage(R.string.onboarding_title_3, R.string.onboarding_body_3, DisclaimerIcons[2]),
        DisclaimerPage(R.string.onboarding_title_4, R.string.onboarding_body_4, DisclaimerIcons[3]),
        DisclaimerPage(R.string.onboarding_title_5, R.string.onboarding_body_5, DisclaimerIcons[4]),
        DisclaimerPage(R.string.onboarding_title_6, R.string.onboarding_body_6, DisclaimerIcons[5])
    )

    val showTimer = current == OnboardingStep.AGREEMENT || current.ordinal < 6

    LaunchedEffect(current) {
        if (showTimer) {
            timerSeconds = 5
            agreed = false
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
            val progress = (step + 1) / 11f

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = progress
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${step + 1}/11",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (current) {
                        OnboardingStep.DISCLAIMER_1 -> stringResource(R.string.onboarding_step_disclaimer_1)
                        OnboardingStep.DISCLAIMER_2 -> stringResource(R.string.onboarding_step_disclaimer_2)
                        OnboardingStep.DISCLAIMER_3 -> stringResource(R.string.onboarding_step_disclaimer_3)
                        OnboardingStep.DISCLAIMER_4 -> stringResource(R.string.onboarding_step_disclaimer_4)
                        OnboardingStep.DISCLAIMER_5 -> stringResource(R.string.onboarding_step_disclaimer_5)
                        OnboardingStep.DISCLAIMER_6 -> stringResource(R.string.onboarding_step_disclaimer_6)
                        OnboardingStep.AGREEMENT -> stringResource(R.string.onboarding_step_agreement)
                        OnboardingStep.LANGUAGE -> stringResource(R.string.onboarding_step_language)
                        OnboardingStep.THEME -> stringResource(R.string.onboarding_step_theme)
                        OnboardingStep.ACTIVATION -> stringResource(R.string.onboarding_step_activation)
                        OnboardingStep.DONE -> stringResource(R.string.onboarding_step_done)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            when (current) {
                OnboardingStep.DISCLAIMER_1, OnboardingStep.DISCLAIMER_2, OnboardingStep.DISCLAIMER_3,
                OnboardingStep.DISCLAIMER_4, OnboardingStep.DISCLAIMER_5, OnboardingStep.DISCLAIMER_6 -> {
                    val page = pages[current.ordinal]
                    val accentColor = MaterialTheme.colorScheme.primary
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = page.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = accentColor
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = stringResource(page.title),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(page.body),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreed,
                                onCheckedChange = { agreed = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = accentColor,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = stringResource(R.string.onboarding_i_have_read_and_agree),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        if (showTimer) {
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
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            TextButton(onClick = { if (step > 0) step = step - 1 }) {
                                Text(stringResource(R.string.onboarding_back))
                            }
                            Button(
                                onClick = {
                                    if (agreed && (!showTimer || timerSeconds <= 0)) {
                                        step = step + 1
                                    }
                                },
                                enabled = agreed && (!showTimer || timerSeconds <= 0),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(if (current == OnboardingStep.DISCLAIMER_6) R.string.onboarding_i_agree_proceed else R.string.onboarding_next))
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
                        Spacer(modifier = Modifier.height(32.dp))
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

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = languageList,
                key = { it.code }
            ) { option ->
                val isSelected = selected == option.code
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = option.code },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(option.nameRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
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

private val languageList = listOf(
    LanguageOption("SYSTEM", R.string.onboarding_language_system),
    LanguageOption("en", R.string.onboarding_language_english),
    LanguageOption("hi", R.string.onboarding_language_hindi),
    LanguageOption("es", R.string.onboarding_language_spanish),
    LanguageOption("fr", R.string.onboarding_language_french),
    LanguageOption("de", R.string.onboarding_language_german),
    LanguageOption("zh", R.string.onboarding_language_chinese),
    LanguageOption("ja", R.string.onboarding_language_japanese),
    LanguageOption("ar", R.string.onboarding_language_arabic),
    LanguageOption("ru", R.string.onboarding_language_russian),
    LanguageOption("pt", R.string.onboarding_language_portuguese),
    LanguageOption("id", R.string.onboarding_language_indonesian)
)

private data class LanguageOption(val code: String, val nameRes: Int)

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

        val themeOptions = listOf(
            ThemeOption(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.onboarding_theme_follow_system, R.string.onboarding_theme_follow_system_summary, R.drawable.ic_system_icon),
            ThemeOption(AppCompatDelegate.MODE_NIGHT_YES, R.string.onboarding_theme_dark, R.string.onboarding_theme_dark_summary, R.drawable.ic_outline_dark_mode_24),
            ThemeOption(AppCompatDelegate.MODE_NIGHT_NO, R.string.onboarding_theme_light, R.string.onboarding_theme_light_summary, R.drawable.ic_outline_light_mode_24),
            ThemeOption(-1, R.string.onboarding_theme_black_night, R.string.onboarding_theme_black_night_summary, R.drawable.ic_outline_dark_mode_24)
        )

        themeOptions.forEach { option ->
            val isSelected = when {
                option.mode == -1 -> blackNight
                else -> nightMode == option.mode
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable {
                        if (option.mode == -1) {
                            blackNight = true
                            nightMode = AppCompatDelegate.MODE_NIGHT_YES
                        } else {
                            nightMode = option.mode
                            blackNight = false
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = option.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(option.titleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = stringResource(option.summaryRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            if (option.mode == -1) {
                                blackNight = true
                                nightMode = AppCompatDelegate.MODE_NIGHT_YES
                            } else {
                                nightMode = option.mode
                                blackNight = false
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = useSystemColor,
                    onCheckedChange = { useSystemColor = it }
                )
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_theme_use_system_color),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.onboarding_theme_use_system_color_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

        val activationOptions = listOf(
            ActivationOption(0, R.string.onboarding_activation_wireless, R.string.onboarding_activation_wireless_summary, R.drawable.ic_wadb_24),
            ActivationOption(1, R.string.onboarding_activation_root, R.string.onboarding_activation_root_summary, R.drawable.ic_root_24dp),
            ActivationOption(2, R.string.onboarding_activation_computer, R.string.onboarding_activation_computer_summary, R.drawable.ic_adb_24dp)
        )

        activationOptions.forEach { option ->
            val isSelected = method == option.mode
            val accentColor = when (option.mode) {
                0 -> Color(59, 130, 246, 255)
                1 -> Color(245, 158, 11, 255)
                else -> Color(16, 185, 129, 255)
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { method = option.mode },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = option.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = accentColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(option.titleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(option.summaryRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    RadioButton(
                        selected = isSelected,
                        onClick = { method = option.mode }
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
                    ShizukuSettings.setActivationMethod(method)
                    ShizukuSettings.setStartMethod(
                        when (method) {
                            1 -> ShizukuSettings.StartMethod.ROOT.toInt()
                            2 -> ShizukuSettings.StartMethod.USB.toInt()
                            else -> ShizukuSettings.StartMethod.WIRELESS.toInt()
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
