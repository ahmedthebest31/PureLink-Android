package com.ahmedsamy.purelink.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PureLinkColorScheme =
        darkColorScheme(
                primary = TerminalGreen,
                onPrimary = TerminalBackground,
                secondary = ButtonActive,
                onSecondary = TextPrimary,
                tertiary = ButtonSecondary,
                onTertiary = TextLight,
                background = TerminalBackground,
                onBackground = TextPrimary,
                surface = TerminalCardBackground,
                onSurface = TextPrimary,
                surfaceVariant = ButtonInactive,
                onSurfaceVariant = TextSecondary,
                outline = TerminalBorder,
                outlineVariant = DividerMedium
        )

@Composable
fun PureLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PureLinkColorScheme, content = content)
}
