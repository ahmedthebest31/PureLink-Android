package com.ahmedsamy.purelink.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedsamy.purelink.MainViewModel
import com.ahmedsamy.purelink.R
import com.ahmedsamy.purelink.data.HistoryItem
import com.ahmedsamy.purelink.ui.theme.DividerMedium
import com.ahmedsamy.purelink.ui.theme.TerminalGreen
import com.ahmedsamy.purelink.ui.theme.TextPrimary
import com.ahmedsamy.purelink.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onCopyClick: (String) -> Unit,
    onOpenClick: (String) -> Unit
) {
    BackHandler(onBack = onBackClick)
    val historyItems by viewModel.historyList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history_title),
                        color = TerminalGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (historyItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.history_empty),
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(historyItems) { item ->
                    HistoryRow(
                        item = item,
                        onCopyClick = onCopyClick,
                        onOpenClick = onOpenClick
                    )
                    HorizontalDivider(color = DividerMedium)
                }
            }
        }
    }
}

@Composable
fun HistoryRow(
    item: HistoryItem,
    onCopyClick: (String) -> Unit,
    onOpenClick: (String) -> Unit
) {
    val copyDesc = stringResource(R.string.desc_copy_link, item.url)
    val openDesc = stringResource(R.string.desc_open_link, item.url)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.url,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = { onCopyClick(item.url) },
            modifier = Modifier.semantics { contentDescription = copyDesc }
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null, // Handled by parent
                tint = TerminalGreen
            )
        }
        IconButton(
            onClick = { onOpenClick(item.url) },
            modifier = Modifier.semantics { contentDescription = openDesc }
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null, // Handled by parent
                tint = TextPrimary
            )
        }
    }
}
