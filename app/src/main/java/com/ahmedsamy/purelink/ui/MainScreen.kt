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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
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
import com.ahmedsamy.purelink.R
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
    onTelegramClick: (String) -> Unit,
    onAboutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Simple Navigation State
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (currentScreen == Screen.HISTORY) {
        HistoryScreen(
            viewModel = viewModel,
            onBackClick = { currentScreen = Screen.HOME },
            onCopyClick = { viewModel.copyToClipboard(it) },
            onOpenClick = { viewModel.openUrl(it) }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                PureLinkTopBar(
                    cleanCount = uiState.cleanCount,
                    inputText = uiState.inputText,
                    onWhatsAppClick = onWhatsAppClick,
                    onTelegramClick = onTelegramClick,
                    onBase64Encode = viewModel::encodeBase64,
                    onBase64Decode = viewModel::decodeBase64,
                    onGenerateUuid = viewModel::generateUuid,
                    onAboutClick = onAboutClick,
                    onHistoryClick = { currentScreen = Screen.HISTORY }
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
                    onPauseResumeClick = viewModel::toggleMonitoring
                )

                Spacer(modifier = Modifier.height(24.dp))

                InputCard(
                    inputText = uiState.inputText,
                    isResolving = uiState.isResolving,
                    onInputChange = viewModel::updateInputText,
                    onPasteClick = viewModel::pasteFromClipboard,
                    onExecuteClick = viewModel::executeClean
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
                    onCardClick = onServiceClick,
                    onSwitchClick = onServiceClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsSection(
                    unshortenEnabled = uiState.unshortenEnabled,
                    vibrateEnabled = uiState.vibrateEnabled,
                    toastEnabled = uiState.toastEnabled,
                    onUnshortenChange = viewModel::setUnshortenEnabled,
                    onVibrateChange = viewModel::setVibrateEnabled,
                    onToastChange = viewModel::setToastEnabled
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
    onWhatsAppClick: (String) -> Unit,
    onTelegramClick: (String) -> Unit,
    onBase64Encode: () -> Unit,
    onBase64Decode: () -> Unit,
    onGenerateUuid: () -> Unit,
    onAboutClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    var showMainMenu by remember { mutableStateOf(false) }
    var showSocialMenu by remember { mutableStateOf(false) }
    var showDevMenu by remember { mutableStateOf(false) }
    
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
            IconButton(onClick = onHistoryClick) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.desc_history_icon),
                    tint = TextPrimary
                )
            }

            Box {
                IconButton(onClick = { showMainMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.menu_desc),
                        tint = TextPrimary
                    )
                }
                DropdownMenu(expanded = showMainMenu, onDismissRequest = { showMainMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_social)) },
                        onClick = {
                            showMainMenu = false
                            showSocialMenu = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_dev)) },
                        onClick = {
                            showMainMenu = false
                            showDevMenu = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_about)) },
                        onClick = {
                            showMainMenu = false
                            onAboutClick()
                        }
                    )
                }

                DropdownMenu(
                    expanded = showSocialMenu,
                    onDismissRequest = { showSocialMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_whatsapp)) },
                        onClick = {
                            showSocialMenu = false
                            onWhatsAppClick(inputText)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_telegram)) },
                        onClick = {
                            showSocialMenu = false
                            onTelegramClick(inputText)
                        }
                    )
                }

                DropdownMenu(expanded = showDevMenu, onDismissRequest = { showDevMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_base64_encode)) },
                        onClick = {
                            showDevMenu = false
                            onBase64Encode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_base64_decode)) },
                        onClick = {
                            showDevMenu = false
                            onBase64Decode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_uuid)) },
                        onClick = {
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