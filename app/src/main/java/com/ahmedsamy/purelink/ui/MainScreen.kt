package com.ahmedsamy.purelink.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedsamy.purelink.MainViewModel
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

    // Handle toast messages
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        // Top App Bar
        PureLinkTopBar(
            cleanCount = uiState.cleanCount,
            inputText = uiState.inputText,
            onWhatsAppClick = onWhatsAppClick,
            onTelegramClick = onTelegramClick,
            onBase64Encode = viewModel::encodeBase64,
            onBase64Decode = viewModel::decodeBase64,
            onGenerateUuid = viewModel::generateUuid,
            onAboutClick = onAboutClick
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
        ) {
            // Status Card
            StatusCard(
                isActive = uiState.isMonitoringActive,
                onPauseResumeClick = viewModel::toggleMonitoring
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Input Card
            InputCard(
                inputText = uiState.inputText,
                isResolving = uiState.isResolving,
                onInputChange = viewModel::updateInputText,
                onPasteClick = viewModel::pasteFromClipboard,
                onExecuteClick = viewModel::executeClean
            )

            Spacer(modifier = Modifier.height(24.dp))

            // System Power Section
            Text(
                text = "SYSTEM_POWER",
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Accessibility Service Card
            ServiceCard(
                isEnabled = uiState.isServiceEnabled,
                onCardClick = onServiceClick,
                onSwitchClick = onServiceClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Settings Section
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
    onAboutClick: () -> Unit
) {
    var showMainMenu by remember { mutableStateOf(false) }
    var showSocialMenu by remember { mutableStateOf(false) }
    var showDevMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ">_ PureLink",
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
                            contentDescription = "Cleaned: $cleanCount items"
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showMainMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = TextPrimary
                    )
                }
                DropdownMenu(expanded = showMainMenu, onDismissRequest = { showMainMenu = false }) {
                    // Social Tools
                    DropdownMenuItem(
                        text = { Text("Social Tools") },
                        onClick = {
                            showMainMenu = false
                            showSocialMenu = true
                        }
                    )

                    // Dev Tools
                    DropdownMenuItem(
                        text = { Text("Dev Tools") },
                        onClick = {
                            showMainMenu = false
                            showDevMenu = true
                        }
                    )

                    // About
                    DropdownMenuItem(
                        text = { Text("About") },
                        onClick = {
                            showMainMenu = false
                            onAboutClick()
                        }
                    )
                }

                // Social Tools Menu
                DropdownMenu(
                    expanded = showSocialMenu,
                    onDismissRequest = { showSocialMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Open as WhatsApp") },
                        onClick = {
                            showSocialMenu = false
                            onWhatsAppClick(inputText)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Open as Telegram") },
                        onClick = {
                            showSocialMenu = false
                            onTelegramClick(inputText)
                        }
                    )
                }

                // Dev Tools Menu
                DropdownMenu(expanded = showDevMenu, onDismissRequest = { showDevMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Base64 Encode") },
                        onClick = {
                            showDevMenu = false
                            onBase64Encode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Base64 Decode") },
                        onClick = {
                            showDevMenu = false
                            onBase64Decode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Generate UUID") },
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
    TerminalCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isActive) "Status: Active 🛡️" else "Status: Paused ⏸️",
                    color = if (isActive) TerminalGreen else StatusPaused,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier =
                        Modifier.semantics {
                            contentDescription =
                                if (isActive) "Status: Active" else "Status: Paused"
                        }
                )
                Text(text = "Monitoring Clipboard", color = TextSecondary, fontSize = 12.sp)
            }
            Button(
                onClick = onPauseResumeClick,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (isActive) ButtonInactive else ButtonActive
                    )
            ) {
                Text(
                    text = if (isActive) "PAUSE" else "RESUME",
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
    TerminalCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                textStyle = TextStyle(color = TextPrimary, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(TerminalGreen),
                decorationBox = { innerTextField ->
                    Box {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Paste Input here...",
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
                ) { Text(text = "PASTE", color = TextLighter) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onExecuteClick,
                    modifier = Modifier.weight(1f),
                    enabled = !isResolving,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonActive)
                ) {
                    Text(text = if (isResolving) "RESOLVING..." else "EXECUTE", color = TextPrimary)
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
                    text = "Accessibility Service",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = "Enable Battery & Access", color = TextLighter, fontSize = 12.sp)
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
            label = "Unshorten URLs",
            checked = unshortenEnabled,
            onCheckedChange = onUnshortenChange
        )
        HorizontalDivider(color = DividerDark)
        SettingsSwitch(
            label = "Haptic Feedback",
            checked = vibrateEnabled,
            onCheckedChange = onVibrateChange
        )
        HorizontalDivider(color = DividerDark)
        SettingsSwitch(
            label = "Verbose Mode (Toasts)",
            checked = toastEnabled,
            onCheckedChange = onToastChange
        )
    }
}
