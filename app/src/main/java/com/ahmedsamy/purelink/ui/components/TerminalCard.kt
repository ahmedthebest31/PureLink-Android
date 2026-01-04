package com.ahmedsamy.purelink.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * A reusable card component with terminal-style green border. Matches the bg_terminal_border.xml
 * drawable aesthetics.
 */
@Composable
fun TerminalCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(8.dp)

    Surface(
            modifier =
                    modifier.clip(shape)
                            .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = shape
                            ),
            color = MaterialTheme.colorScheme.surface,
            shape = shape
    ) { Box(modifier = Modifier.padding(16.dp)) { content() } }
}
