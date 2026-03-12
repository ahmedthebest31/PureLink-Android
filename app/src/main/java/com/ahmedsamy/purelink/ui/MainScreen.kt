package com.ahmedsamy.purelink.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedsamy.purelink.MainViewModel
import com.ahmedsamy.purelink.ToastMessage
import com.ahmedsamy.purelink.R
import com.ahmedsamy.purelink.UpdateStatus
import com.ahmedsamy.purelink.utils.FeedbackUtils
import com.ahmedsamy.purelink.ui.components.SettingsSwitch
import com.ahmedsamy.purelink.ui.components.TerminalCard
import com.ahmedsamy.purelink.ui.theme.ButtonActive
import com.ahmedsamy.purelink.ui.theme.ButtonInactive
import com.ahmedsamy.purelink.ui.theme.ButtonSecondary
import com.ahmedsamy.purelink.ui.theme.DividerDark
import com.ahmedsamy.purelink.ui.theme.DividerMedium
import com.ahmedsamy.purelink.ui.theme.StatusPaused
import com.ahmedsamy.purelink.ui.theme.TerminalGreen
import com.ahmedsamy.purelink.ui.theme.TextHint
import com.ahmedsamy.purelink.ui.theme.TextLighter
import com.ahmedsamy.purelink.ui.theme.TextMuted
import com.ahmedsamy.purelink.ui.theme.TextPrimary
import com.ahmedsamy.purelink.ui.theme.TextSecondary

// Enum for simple state-based navigation
enum class Screen {
    HOME, HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onServiceClick: () -> Unit,
    onWhatsAppClick: (String) -> Unit,
    onTelegramClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Simple Navigation State
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { toast ->
            val message = when (toast) {
                is ToastMessage.Resource -> {
                    if (toast.args.isEmpty()) {
                        context.getString(toast.resId)
                    } else {
                        // Handle plurals if necessary (e.g. for cleaning count)
                        if (toast.resId == R.string.toast_cleaned_plural || toast.resId == R.string.toast_unshortened_plural) {
                            val count = toast.args[0] as Int
                            context.resources.getQuantityString(toast.resId, count, *toast.args.toTypedArray())
                        } else {
                            context.getString(toast.resId, *toast.args.toTypedArray())
                        }
                    }
                }
                is ToastMessage.Literal -> toast.message
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Handle Update Status Dialogs
    when (uiState.updateStatus) {
        UpdateStatus.LOADING -> {
             LaunchedEffect(Unit) {
                 Toast.makeText(context, context.getString(R.string.checking_updates), Toast.LENGTH_SHORT).show()
             }
        }
        UpdateStatus.SUCCESS -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetUpdateStatus() },
                title = { Text(text = stringResource(R.string.update_success_title), fontFamily = FontFamily.Monospace, color = TerminalGreen) },
                text = { Text(stringResource(R.string.update_success_msg), color = TextPrimary) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetUpdateStatus() }) {
                        Text(stringResource(R.string.btn_ok), color = TerminalGreen)
                    }
                },
                containerColor = com.ahmedsamy.purelink.ui.theme.TerminalCardBackground,
                textContentColor = TextPrimary
            )
        }
        UpdateStatus.ERROR -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetUpdateStatus() },
                title = { Text(text = stringResource(R.string.update_failed_title), fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.error) },
                text = { Text(stringResource(R.string.update_failed_msg), color = TextPrimary) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetUpdateStatus() }) {
                        Text(stringResource(R.string.btn_ok), color = MaterialTheme.colorScheme.error)
                    }
                },
                containerColor = com.ahmedsamy.purelink.ui.theme.TerminalCardBackground,
                textContentColor = TextPrimary
            )
        }
        else -> {}
    }

    if (!uiState.isServiceEnabled && uiState.showOnboardingAlert) {
         AlertDialog(
            onDismissRequest = { viewModel.markOnboardingSeen() },
            title = { Text(text = stringResource(R.string.accessibility_title), fontFamily = FontFamily.Monospace, color = TextPrimary) },
            text = { Text(stringResource(R.string.accessibility_desc), color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = { 
                        FeedbackUtils.performHapticFeedback(context)
                        viewModel.markOnboardingSeen()
                        onServiceClick() 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                ) {
                    Text(stringResource(R.string.ok), color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    FeedbackUtils.performHapticFeedback(context)
                    viewModel.markOnboardingSeen() 
                }) {
                     Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            },
            containerColor = com.ahmedsamy.purelink.ui.theme.TerminalCardBackground,
            textContentColor = TextPrimary
        )
    }

    if (currentScreen == Screen.HISTORY) {
        HistoryScreen(
            viewModel = viewModel,
            onBackClick = { 
                FeedbackUtils.performHapticFeedback(context)
                currentScreen = Screen.HOME 
            },
            onCopyClick = { viewModel.copyToClipboard(it) },
            onOpenClick = { 
                FeedbackUtils.performHapticFeedback(context)
                viewModel.openUrl(it) 
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                PureLinkTopBar(
                    cleanCount = uiState.cleanCount,
                    inputText = uiState.inputText,
                    selectedLanguage = uiState.selectedLanguage,
                    onWhatsAppClick = onWhatsAppClick,
                    onTelegramClick = onTelegramClick,
                    onBase64Encode = viewModel::encodeBase64,
                    onBase64Decode = viewModel::decodeBase64,
                    onGenerateUuid = viewModel::generateUuid,
                    onUpdateRules = viewModel::updateRules,
                    onHistoryClick = { currentScreen = Screen.HISTORY },
                    onShareApp = viewModel::shareApp,
                    onRateApp = viewModel::rateApp,
                    onSetLanguage = viewModel::setLanguage,
                    onGitHubClick = viewModel::openRepo,
                    onDonatePayPal = viewModel::openPayPal,
                    onDonateInstaPay = viewModel::openInstaPay
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                StatusCard(
                    isActive = uiState.isMonitoringActive,
                    onPauseResumeClick = {
                        FeedbackUtils.performHapticFeedback(context)
                        viewModel.toggleMonitoring()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                InputCard(
                    inputText = uiState.inputText,
                    isResolving = uiState.isResolving,
                    onInputChange = viewModel::updateInputText,
                    onPasteClick = {
                        FeedbackUtils.performHapticFeedback(context)
                        viewModel.pasteFromClipboard()
                    },
                    onExecuteClick = {
                        FeedbackUtils.performHapticFeedback(context)
                        viewModel.executeClean()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.section_system_power),
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ServiceCard(
                    isEnabled = uiState.isServiceEnabled,
                    onCardClick = {
                        FeedbackUtils.performHapticFeedback(context)
                        onServiceClick()
                    },
                    onSwitchClick = {
                        FeedbackUtils.performHapticFeedback(context)
                        onServiceClick()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsSection(
                    unshortenEnabled = uiState.unshortenEnabled,
                    vibrateEnabled = uiState.vibrateEnabled,
                    toastEnabled = uiState.toastEnabled,
                    onUnshortenChange = {
                        FeedbackUtils.performHapticFeedback(context)
                        viewModel.setUnshortenEnabled(it)
                    },
                    onVibrateChange = {
                        FeedbackUtils.performHapticFeedback(context)
                        viewModel.setVibrateEnabled(it)
                    },
                    onToastChange = {
                        FeedbackUtils.performHapticFeedback(context)
                        viewModel.setToastEnabled(it)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PureLinkTopBar(
    cleanCount: Int,
    inputText: String,
    selectedLanguage: String,
    onWhatsAppClick: (String) -> Unit,
    onTelegramClick: (String) -> Unit,
    onBase64Encode: () -> Unit,
    onBase64Decode: () -> Unit,
    onGenerateUuid: () -> Unit,
    onUpdateRules: () -> Unit,
    onHistoryClick: () -> Unit,
    onShareApp: () -> Unit,
    onRateApp: () -> Unit,
    onSetLanguage: (String) -> Unit,
    onGitHubClick: () -> Unit,
    onDonatePayPal: () -> Unit,
    onDonateInstaPay: () -> Unit
) {
    var showMainMenu by remember { mutableStateOf(false) }
    var showSocialMenu by remember { mutableStateOf(false) }
    var showDevMenu by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }
    var showDonateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val cleanedDesc = stringResource(R.string.cleaned_count_desc, cleanCount)

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_title),
                    color = TerminalGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = cleanCount.toString(),
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier =
                        Modifier.semantics {
                            contentDescription = cleanedDesc
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        },
        actions = {
             // History Icon
            IconButton(onClick = {
                FeedbackUtils.performHapticFeedback(context)
                onHistoryClick()
            }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.desc_history_icon),
                    tint = TextPrimary
                )
            }

            Box {
                IconButton(onClick = { 
                    FeedbackUtils.performHapticFeedback(context)
                    showMainMenu = true 
                }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.menu_desc),
                        tint = TextPrimary
                    )
                }
                DropdownMenu(expanded = showMainMenu, onDismissRequest = { showMainMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_update)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showMainMenu = false
                            onUpdateRules()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_language)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showMainMenu = false
                            showLangMenu = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_social)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showMainMenu = false
                            showSocialMenu = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_dev)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showMainMenu = false
                            showDevMenu = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_share)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showMainMenu = false
                            onShareApp()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_rate)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showMainMenu = false
                            onRateApp()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_github)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showMainMenu = false
                            onGitHubClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_donate)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showMainMenu = false
                            showDonateDialog = true
                        }
                    )
                }

                if (showDonateDialog) {
                    DonateDialog(
                        onDismiss = { showDonateDialog = false },
                        onPayPalClick = { onDonatePayPal() },
                        onInstaPayClick = { onDonateInstaPay() }
                    )
                }

                DropdownMenu(
                    expanded = showLangMenu,
                    onDismissRequest = { showLangMenu = false }) {
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.lang_system), modifier = Modifier.weight(1f))
                                if (selectedLanguage == "") {
                                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.ok), tint = TerminalGreen)
                                }
                            }
                        },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showLangMenu = false
                            onSetLanguage("")
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.lang_english), modifier = Modifier.weight(1f))
                                if (selectedLanguage == "en") {
                                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.ok), tint = TerminalGreen)
                                }
                            }
                        },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showLangMenu = false
                            onSetLanguage("en")
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.lang_arabic), modifier = Modifier.weight(1f))
                                if (selectedLanguage == "ar") {
                                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.ok), tint = TerminalGreen)
                                }
                            }
                        },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showLangMenu = false
                            onSetLanguage("ar")
                        }
                    )
                }

                DropdownMenu(
                    expanded = showSocialMenu,
                    onDismissRequest = { showSocialMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_whatsapp)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showSocialMenu = false
                            onWhatsAppClick(inputText)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_telegram)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showSocialMenu = false
                            onTelegramClick(inputText)
                        }
                    )
                }

                DropdownMenu(expanded = showDevMenu, onDismissRequest = { showDevMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_base64_encode)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showDevMenu = false
                            onBase64Encode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_base64_decode)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showDevMenu = false
                            onBase64Decode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_uuid)) },
                        onClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            showDevMenu = false
                            onGenerateUuid()
                        }
                    )
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
    )
}

@Composable
private fun StatusCard(isActive: Boolean, onPauseResumeClick: () -> Unit) {
    val statusText = if (isActive) stringResource(R.string.status_active) else stringResource(R.string.status_paused)
    val statusDesc = if (isActive) stringResource(R.string.status_desc_active) else stringResource(R.string.status_desc_paused)

    TerminalCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    color = if (isActive) TerminalGreen else StatusPaused,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier =
                        Modifier.semantics {
                            contentDescription = statusDesc
                        }
                )
                Text(text = stringResource(R.string.monitoring_label), color = TextSecondary, fontSize = 12.sp)
            }
            Button(
                onClick = onPauseResumeClick,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (isActive) ButtonInactive else ButtonActive
                    )
            ) {
                Text(
                    text = if (isActive) stringResource(R.string.btn_pause) else stringResource(R.string.btn_resume),
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun InputCard(
    inputText: String,
    isResolving: Boolean,
    onInputChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onExecuteClick: () -> Unit
) {
    val inputDesc = stringResource(R.string.input_field_desc)

    TerminalCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics { contentDescription = inputDesc },
                textStyle = TextStyle(color = TextPrimary, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(TerminalGreen),
                decorationBox = { innerTextField ->
                    Box {
                        if (inputText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.input_hint),
                                color = TextHint,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        innerTextField()
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerMedium)

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onPasteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonSecondary)
                ) { Text(text = stringResource(R.string.btn_paste), color = TextLighter) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onExecuteClick,
                    modifier = Modifier.weight(1f),
                    enabled = !isResolving,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonActive)
                ) {
                    Text(
                        text = if (isResolving) stringResource(R.string.btn_resolving) else stringResource(R.string.btn_execute),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(isEnabled: Boolean, onCardClick: () -> Unit, onSwitchClick: () -> Unit) {
    TerminalCard(modifier = Modifier
        .fillMaxWidth()
        .clickable { onCardClick() }) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.service_card_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = stringResource(R.string.service_card_subtitle), color = TextLighter, fontSize = 12.sp)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { onSwitchClick() },
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = TerminalGreen,
                        checkedTrackColor = TerminalGreen.copy(alpha = 0.5f)
                    )
            )
        }
    }
}

@Composable
private fun SettingsSection(
    unshortenEnabled: Boolean,
    vibrateEnabled: Boolean,
    toastEnabled: Boolean,
    onUnshortenChange: (Boolean) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onToastChange: (Boolean) -> Unit
) {
    Column {
        SettingsSwitch(
            label = stringResource(R.string.setting_unshorten),
            checked = unshortenEnabled,
            onCheckedChange = onUnshortenChange
        )
        HorizontalDivider(color = DividerDark)
        SettingsSwitch(
            label = stringResource(R.string.setting_haptic),
            checked = vibrateEnabled,
            onCheckedChange = onVibrateChange
        )
        HorizontalDivider(color = DividerDark)
        SettingsSwitch(
            label = stringResource(R.string.setting_verbose),
            checked = toastEnabled,
            onCheckedChange = onToastChange
        )
    }
}

@Composable
fun DonateDialog(
    onDismiss: () -> Unit,
    onPayPalClick: () -> Unit,
    onInstaPayClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.donate_title), fontFamily = FontFamily.Monospace, color = TerminalGreen) },
        text = {
            Column {
                Text(text = stringResource(R.string.donate_message), color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onPayPalClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonActive)
                ) {
                    Text(text = stringResource(R.string.btn_paypal), color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onInstaPayClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonSecondary)
                ) {
                    Text(text = stringResource(R.string.btn_instapay), color = TextPrimary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.btn_ok), color = TerminalGreen)
            }
        },
        containerColor = com.ahmedsamy.purelink.ui.theme.TerminalCardBackground,
        textContentColor = TextPrimary
    )
}
