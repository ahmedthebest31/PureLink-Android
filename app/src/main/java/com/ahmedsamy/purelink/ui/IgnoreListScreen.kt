package com.ahmedsamy.purelink.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.ahmedsamy.purelink.MainViewModel
import com.ahmedsamy.purelink.R
import com.ahmedsamy.purelink.ui.theme.ButtonActive
import com.ahmedsamy.purelink.ui.theme.DividerMedium
import com.ahmedsamy.purelink.ui.theme.TerminalCardBackground
import com.ahmedsamy.purelink.ui.theme.TerminalGreen
import com.ahmedsamy.purelink.ui.theme.TextHint
import com.ahmedsamy.purelink.ui.theme.TextLighter
import com.ahmedsamy.purelink.ui.theme.TextMuted
import com.ahmedsamy.purelink.ui.theme.TextPrimary
import com.ahmedsamy.purelink.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IgnoreListScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var newDomain by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.setting_ignore_list),
                        color = TerminalGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        com.ahmedsamy.purelink.utils.FeedbackUtils.performHapticFeedback(context)
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        com.ahmedsamy.purelink.utils.FeedbackUtils.performHapticFeedback(context)
                        showAddDialog = true
                        newDomain = ""
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.ignore_list_add),
                            tint = TerminalGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (uiState.ignoreList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.ignore_list_empty),
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(uiState.ignoreList.toList()) { domain ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = domain,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                com.ahmedsamy.purelink.utils.FeedbackUtils.performHapticFeedback(context)
                                val updated = uiState.ignoreList - domain
                                viewModel.setIgnoreList(updated)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.ignore_list_delete, domain),
                                tint = TextSecondary
                            )
                        }
                    }
                    HorizontalDivider(color = DividerMedium)
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.ignore_list_add),
                    fontFamily = FontFamily.Monospace,
                    color = TerminalGreen
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.ignore_list_add_hint),
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BasicTextField(
                        value = newDomain,
                        onValueChange = { newDomain = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        textStyle = TextStyle(color = TextPrimary, fontFamily = FontFamily.Monospace),
                        cursorBrush = SolidColor(TerminalGreen),
                        decorationBox = { innerTextField ->
                            Box {
                                if (newDomain.isEmpty()) {
                                    Text(
                                        text = "example.com",
                                        color = TextHint,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleaned = newDomain.trim().lowercase()
                    if (cleaned.isNotEmpty()) {
                        val updated = uiState.ignoreList + cleaned
                        viewModel.setIgnoreList(updated)
                    }
                    showAddDialog = false
                }) {
                    Text(stringResource(R.string.btn_add), color = TerminalGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            },
            containerColor = TerminalCardBackground,
            textContentColor = TextPrimary
        )
    }
}
