package com.ahmedsamy.purelink.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedsamy.purelink.MainViewModel
import com.ahmedsamy.purelink.ToastMessage
import com.ahmedsamy.purelink.R
import com.ahmedsamy.purelink.UpdateStatus
import com.ahmedsamy.purelink.ui.components.SettingsSwitch
import com.ahmedsamy.purelink.ui.components.TerminalCard
import com.ahmedsamy.purelink.ui.theme.ButtonActive
import com.ahmedsamy.purelink.ui.theme.ButtonInactive
import com.ahmedsamy.purelink.ui.theme.ButtonSecondary
import com.ahmedsamy.purelink.ui.theme.DividerDark
import com.ahmedsamy.purelink.ui.theme.DividerMedium
import com.ahmedsamy.purelink.ui.theme.StatusPaused
import com.ahmedsamy.purelink.ui.theme.TerminalCardBackground
import com.ahmedsamy.purelink.ui.theme.TerminalGreen
import com.ahmedsamy.purelink.ui.theme.TextHint
import com.ahmedsamy.purelink.ui.theme.TextLighter
import com.ahmedsamy.purelink.ui.theme.TextMuted
import com.ahmedsamy.purelink.ui.theme.TextPrimary
import com.ahmedsamy.purelink.ui.theme.TextSecondary
import com.ahmedsamy.purelink.utils.FeedbackUtils

enum class Screen {
    HOME, HISTORY, IGNORE_LIST
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
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { toast ->
            val message = when (toast) {
                is ToastMessage.Resource -> {
                    if (toast.args.isEmpty()) {
                        context.getString(toast.resId)
                    } else {
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
                containerColor = TerminalCardBackground,
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
                containerColor = TerminalCardBackground,
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
            containerColor = TerminalCardBackground,
            textContentColor = TextPrimary
        )
    }

    if (uiState.showSmartCommandsHelp) {
        SmartCommandsHelpDialog(
            onDismiss = {
                FeedbackUtils.performHapticFeedback(context)
                viewModel.dismissSmartCommandsHelp()
            }
        )
    }

    if (currentScreen == Screen.IGNORE_LIST) {
        IgnoreListScreen(
            viewModel = viewModel,
            onBackClick = {
                FeedbackUtils.performHapticFeedback(context)
                currentScreen = Screen.HOME
            }
        )
    } else if (currentScreen == Screen.HISTORY) {
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
                    onHistoryClick = {
                        FeedbackUtils.performHapticFeedback(context)
                        currentScreen = Screen.HISTORY
                    }
                )
            },
            bottomBar = {
                PureLinkBottomBar(
                    selectedTab = uiState.selectedTab,
                    vibrateEnabled = uiState.vibrateEnabled,
                    onTabSelected = { viewModel.setSelectedTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                when (uiState.selectedTab) {
                    0 -> DashboardTab(
                        isMonitoringActive = uiState.isMonitoringActive,
                        cleanCount = uiState.cleanCount,
                        unshortenCount = uiState.unshortenCount,
                        filterCount = uiState.filterCount,
                        ignoredCount = uiState.ignoreList.size,
                        inputText = uiState.inputText,
                        isResolving = uiState.isResolving,
                        onPauseResumeClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.toggleMonitoring()
                        },
                        onInputChange = viewModel::updateInputText,
                        onPasteClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.pasteFromClipboard()
                        },
                        onExecuteClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.executeClean()
                        },
                        onClearInput = {
                            viewModel.updateInputText("")
                        }
                    )
                    1 -> ToolsTab(
                        inputText = uiState.inputText,
                        outputText = uiState.toolsOutputText,
                        onInputChange = viewModel::updateInputText,
                        onPasteClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.pasteFromClipboard()
                        },
                        onClearInput = {
                            viewModel.updateInputText("")
                        },
                        onCopyOutput = {
                            viewModel.copyToClipboard(uiState.toolsOutputText)
                        },
                        onClearOutput = {
                            viewModel.clearToolsOutput()
                        },
                        onWhatsAppClick = {
                            onWhatsAppClick(uiState.inputText)
                        },
                        onTelegramClick = {
                            onTelegramClick(uiState.inputText)
                        },
                        onBase64Encode = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.encodeBase64()
                        },
                        onBase64Decode = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.decodeBase64()
                        },
                        onGenerateUuid = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.generateUuid()
                        }
                    )
                    2 -> SettingsTab(
                        isServiceEnabled = uiState.isServiceEnabled,
                        unshortenEnabled = uiState.unshortenEnabled,
                        youtubeShortsEnabled = uiState.youtubeShortsEnabled,
                        smartCommandsEnabled = uiState.smartCommandsEnabled,
                        ignoreList = uiState.ignoreList,
                        vibrateEnabled = uiState.vibrateEnabled,
                        toastEnabled = uiState.toastEnabled,
                        selectedLanguage = uiState.selectedLanguage,
                        selectedTheme = uiState.selectedTheme,
                        onServiceClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            onServiceClick()
                        },
                        onUnshortenChange = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.setUnshortenEnabled(it)
                        },
                        onYoutubeShortsChange = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.setYoutubeShortsEnabled(it)
                        },
                        onSmartCommandsChange = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.setSmartCommandsEnabled(it)
                        },
                        onSmartCommandsHelpClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.showSmartCommandsHelp()
                        },
                        onIgnoreListClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            currentScreen = Screen.IGNORE_LIST
                        },
                        onVibrateChange = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.setVibrateEnabled(it)
                        },
                        onToastChange = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.setToastEnabled(it)
                        },
                        onLanguageChange = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.setLanguage(it)
                        },
                        onThemeChange = {
                            viewModel.setTheme(it)
                        },
                        onUpdateFilters = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.updateRules()
                        },
                        onGitHubClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.openRepo()
                        },
                        onRateClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.rateApp()
                        },
                        onDonateClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.openPayPal()
                        },
                        onShareClick = {
                            FeedbackUtils.performHapticFeedback(context)
                            viewModel.shareApp()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PureLinkTopBar(cleanCount: Int, onHistoryClick: () -> Unit) {
    val context = LocalContext.current
    val cleanedDesc = stringResource(R.string.cleaned_count_desc, cleanCount)

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ">_ " + stringResource(R.string.app_title),
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
                    modifier = Modifier.semantics { contentDescription = cleanedDesc }
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        },
        actions = {
            IconButton(onClick = {
                FeedbackUtils.performHapticFeedback(context)
                onHistoryClick()
            }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.desc_history_icon),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
private fun PureLinkBottomBar(selectedTab: Int, vibrateEnabled: Boolean, onTabSelected: (Int) -> Unit) {
    val context = LocalContext.current
    val tabs = listOf(
        Triple(Icons.Default.Home, R.string.tab_dashboard, R.string.tab_dashboard),
        Triple(Icons.Default.Build, R.string.tab_tools, R.string.tab_tools),
        Triple(Icons.Default.Settings, R.string.tab_settings, R.string.tab_settings)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        tabs.forEachIndexed { index, (icon, labelRes, descRes) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = {
                    if (vibrateEnabled) FeedbackUtils.performHapticFeedback(context)
                    onTabSelected(index)
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(descRes),
                        tint = if (selectedTab == index) MaterialTheme.colorScheme.primary else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = stringResource(labelRes),
                        color = if (selectedTab == index) MaterialTheme.colorScheme.primary else TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
private fun DashboardTab(
    isMonitoringActive: Boolean,
    cleanCount: Int,
    unshortenCount: Int,
    filterCount: Int,
    ignoredCount: Int,
    inputText: String,
    isResolving: Boolean,
    onPauseResumeClick: () -> Unit,
    onInputChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onExecuteClick: () -> Unit,
    onClearInput: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PowerButton(
                isActive = isMonitoringActive,
                onClick = onPauseResumeClick
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        StatsGrid(
            cleanCount = cleanCount,
            unshortenCount = unshortenCount,
            filterCount = filterCount,
            ignoredCount = ignoredCount
        )

        Spacer(modifier = Modifier.height(20.dp))

        InputCard(
            inputText = inputText,
            isResolving = isResolving,
            onInputChange = onInputChange,
            onPasteClick = onPasteClick,
            onExecuteClick = onExecuteClick,
            onClearInput = onClearInput,
            showClearButton = true
        )
    }
}

@Composable
private fun PowerButton(isActive: Boolean, onClick: () -> Unit) {
    val statusText = if (isActive) stringResource(R.string.system_online) else stringResource(R.string.system_offline)
    val statusColor = if (isActive) MaterialTheme.colorScheme.primary else StatusPaused

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    color = statusColor,
                    shape = CircleShape
                ),
            color = MaterialTheme.colorScheme.surface,
            shape = CircleShape,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isActive) statusColor.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isActive) "ON" else "OFF",
                    color = statusColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = stringResource(R.string.monitoring_label),
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun StatsGrid(cleanCount: Int, unshortenCount: Int, filterCount: Int, ignoredCount: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = stringResource(R.string.stat_cleaned),
                value = cleanCount.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.stat_unshortened),
                value = unshortenCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = stringResource(R.string.stat_filters),
                value = filterCount.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.stat_ignored),
                value = ignoredCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    TerminalCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun InputCard(
    inputText: String,
    isResolving: Boolean,
    onInputChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onExecuteClick: () -> Unit,
    onClearInput: () -> Unit = {},
    showClearButton: Boolean = false
) {
    val inputDesc = stringResource(R.string.input_field_desc)

    TerminalCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = inputDesc },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
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
                if (showClearButton && inputText.isNotEmpty()) {
                    TextButton(onClick = onClearInput) {
                        Text(
                            text = stringResource(R.string.btn_clear),
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerMedium)

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onPasteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonSecondary)
                ) {
                    Text(
                        text = stringResource(R.string.btn_paste),
                        color = TextLighter,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onExecuteClick,
                    modifier = Modifier.weight(1f),
                    enabled = !isResolving,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonActive)
                ) {
                    Text(
                        text = if (isResolving) stringResource(R.string.btn_resolving) else stringResource(R.string.btn_execute),
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolsTab(
    inputText: String,
    outputText: String,
    onInputChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onClearInput: () -> Unit,
    onCopyOutput: () -> Unit,
    onClearOutput: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onBase64Encode: () -> Unit,
    onBase64Decode: () -> Unit,
    onGenerateUuid: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        InputCard(
            inputText = inputText,
            isResolving = false,
            onInputChange = onInputChange,
            onPasteClick = onPasteClick,
            onExecuteClick = {},
            onClearInput = onClearInput,
            showClearButton = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        TerminalCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                val outputDesc = stringResource(R.string.output_field_desc)
                Text(
                    text = outputDesc,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.semantics { contentDescription = outputDesc }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (outputText.isEmpty()) stringResource(R.string.input_hint) else outputText,
                    color = if (outputText.isEmpty()) TextHint else MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onCopyOutput,
                        modifier = Modifier.weight(1f),
                        enabled = outputText.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonActive)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_copy),
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onClearOutput,
                        modifier = Modifier.weight(1f),
                        enabled = outputText.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonSecondary)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_clear),
                            color = TextLighter,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.section_social_utilities),
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onWhatsAppClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonActive)
            ) {
                Text(
                    text = stringResource(R.string.btn_wa),
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Button(
                onClick = onTelegramClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonSecondary)
            ) {
                Text(
                    text = stringResource(R.string.btn_tg),
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.section_dev_tools),
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onBase64Encode,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonActive)
            ) {
                Text(
                    text = stringResource(R.string.btn_b64e),
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = onBase64Decode,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonSecondary)
            ) {
                Text(
                    text = stringResource(R.string.btn_b64d),
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = onGenerateUuid,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonInactive)
            ) {
                Text(
                    text = stringResource(R.string.btn_uuid),
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTab(
    isServiceEnabled: Boolean,
    unshortenEnabled: Boolean,
    youtubeShortsEnabled: Boolean,
    smartCommandsEnabled: Boolean,
    ignoreList: Set<String>,
    vibrateEnabled: Boolean,
    toastEnabled: Boolean,
    selectedLanguage: String,
    selectedTheme: String,
    onServiceClick: () -> Unit,
    onUnshortenChange: (Boolean) -> Unit,
    onYoutubeShortsChange: (Boolean) -> Unit,
    onSmartCommandsChange: (Boolean) -> Unit,
    onSmartCommandsHelpClick: () -> Unit,
    onIgnoreListClick: () -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onToastChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onUpdateFilters: () -> Unit,
    onGitHubClick: () -> Unit,
    onRateClick: () -> Unit,
    onDonateClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.section_permissions),
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TerminalCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onServiceClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.service_card_title),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.service_card_subtitle),
                        color = TextLighter,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = isServiceEnabled,
                    onCheckedChange = { onServiceClick() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.section_filter_engine),
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TerminalCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingsSwitch(
                    label = stringResource(R.string.setting_unshorten),
                    checked = unshortenEnabled,
                    onCheckedChange = onUnshortenChange
                )
                HorizontalDivider(color = DividerDark)
                SettingsSwitch(
                    label = stringResource(R.string.setting_youtube_shorts),
                    checked = youtubeShortsEnabled,
                    onCheckedChange = onYoutubeShortsChange
                )
                HorizontalDivider(color = DividerDark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val smartCmdDesc = stringResource(R.string.desc_smart_commands_help)
                    Text(
                        text = stringResource(R.string.setting_smart_commands),
                        color = TextLighter,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onSmartCommandsHelpClick,
                        modifier = Modifier.semantics { contentDescription = smartCmdDesc }
                    ) {
                        Text(
                            text = "?",
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Switch(
                        checked = smartCommandsEnabled,
                        onCheckedChange = onSmartCommandsChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                }
                HorizontalDivider(color = DividerDark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIgnoreListClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.setting_ignore_list),
                        color = TextLighter,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "(${ignoreList.size})",
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.section_appearance),
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TerminalCard(modifier = Modifier.fillMaxWidth()) {
            Column {
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
                HorizontalDivider(color = DividerDark)
                LanguageSelector(
                    selectedLanguage = selectedLanguage,
                    onLanguageChange = onLanguageChange
                )
                HorizontalDivider(color = DividerDark)
                ThemeSelector(
                    selectedTheme = selectedTheme,
                    onThemeChange = onThemeChange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.section_app_info),
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TerminalCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateFilters() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.menu_update),
                        color = TextLighter,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ">",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                HorizontalDivider(color = DividerDark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGitHubClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.btn_github),
                        color = TextLighter,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ">",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                HorizontalDivider(color = DividerDark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRateClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.btn_rate),
                        color = TextLighter,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ">",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                HorizontalDivider(color = DividerDark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShareClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.btn_share),
                        color = TextLighter,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ">",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                HorizontalDivider(color = DividerDark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDonateClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.btn_donate),
                        color = TextLighter,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ">",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(selectedTheme: String, onThemeChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val themes = listOf(
        "matrix" to R.string.theme_matrix,
        "amber" to R.string.theme_amber,
        "dracula" to R.string.theme_dracula,
        "monokai" to R.string.theme_monokai
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.theme_label),
                color = TextLighter,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(themes.find { it.first == selectedTheme }?.second ?: R.string.theme_matrix),
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            themes.forEach { (key, labelRes) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(labelRes),
                            fontFamily = FontFamily.Monospace,
                            color = if (key == selectedTheme) MaterialTheme.colorScheme.primary else TextPrimary
                        )
                    },
                    onClick = {
                        onThemeChange(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(selectedLanguage: String, onLanguageChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf(
        "" to R.string.lang_system,
        "en" to R.string.lang_english,
        "ar" to R.string.lang_arabic
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.language_label),
                color = TextLighter,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(languages.find { it.first == selectedLanguage }?.second ?: R.string.lang_system),
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (key, labelRes) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(labelRes),
                            fontFamily = FontFamily.Monospace,
                            color = if (key == selectedLanguage) MaterialTheme.colorScheme.primary else TextPrimary
                        )
                    },
                    onClick = {
                        onLanguageChange(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SmartCommandsHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.smart_commands_help_title),
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.smart_commands_help_desc),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                val commands = listOf(
                    "wa <number>" to stringResource(R.string.smart_cmd_wa),
                    "tg <user>" to stringResource(R.string.smart_cmd_tg),
                    "b64e <text>" to stringResource(R.string.smart_cmd_b64e),
                    "b64d <data>" to stringResource(R.string.smart_cmd_b64d),
                    "uuid" to stringResource(R.string.smart_cmd_uuid),
                    "upper [text]" to stringResource(R.string.smart_cmd_upper),
                    "lower [text]" to stringResource(R.string.smart_cmd_lower),
                    "capitalize [text]" to stringResource(R.string.smart_cmd_capitalize),
                    "reverse [text]" to stringResource(R.string.smart_cmd_reverse),
                    "clear [text]" to stringResource(R.string.smart_cmd_clear),
                    "trim [text]" to stringResource(R.string.smart_cmd_trim)
                )
                commands.forEach { (cmd, desc) ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "/$cmd",
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(text = desc, color = TextSecondary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_got_it), color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = TerminalCardBackground,
        textContentColor = TextPrimary
    )
}
