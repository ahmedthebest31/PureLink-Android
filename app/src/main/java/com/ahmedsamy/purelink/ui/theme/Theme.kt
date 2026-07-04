package com.ahmedsamy.purelink.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MatrixColorScheme =
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

private val AmberColorScheme =
        darkColorScheme(
                primary = AmberAccent,
                onPrimary = AmberBackground,
                secondary = AmberAccent.copy(alpha = 0.3f),
                onSecondary = AmberAccent,
                tertiary = Color(0xFF332200),
                onTertiary = AmberAccent.copy(alpha = 0.7f),
                background = AmberBackground,
                onBackground = AmberAccent,
                surface = AmberBackground,
                onSurface = AmberAccent,
                surfaceVariant = Color(0xFF1A1500),
                onSurfaceVariant = AmberAccent.copy(alpha = 0.6f),
                outline = AmberAccent.copy(alpha = 0.5f),
                outlineVariant = Color(0xFF332200)
        )

private val DraculaColorScheme =
        darkColorScheme(
                primary = DraculaAccent,
                onPrimary = DraculaBackground,
                secondary = DraculaPurple,
                onSecondary = Color.White,
                tertiary = Color(0xFF3A3B4A),
                onTertiary = DraculaAccent.copy(alpha = 0.7f),
                background = DraculaBackground,
                onBackground = Color(0xFFF8F8F2),
                surface = DraculaBackground,
                onSurface = Color(0xFFF8F8F2),
                surfaceVariant = Color(0xFF2A2B38),
                onSurfaceVariant = Color(0xFFA0A0B0),
                outline = DraculaPurple.copy(alpha = 0.4f),
                outlineVariant = Color(0xFF3A3B4A)
        )

private val MonokaiColorScheme =
        darkColorScheme(
                primary = MonokaiAccent,
                onPrimary = MonokaiBackground,
                secondary = MonokaiYellow,
                onSecondary = MonokaiBackground,
                tertiary = Color(0xFF3A3D32),
                onTertiary = MonokaiAccent.copy(alpha = 0.7f),
                background = MonokaiBackground,
                onBackground = Color(0xFFF8F8F2),
                surface = MonokaiBackground,
                onSurface = Color(0xFFF8F8F2),
                surfaceVariant = Color(0xFF2D2E24),
                onSurfaceVariant = Color(0xFFA0A090),
                outline = MonokaiAccent.copy(alpha = 0.4f),
                outlineVariant = Color(0xFF3A3D32)
        )

private val LightColorScheme =
        lightColorScheme(
                primary = LightPrimary,
                onPrimary = LightOnPrimary,
                secondary = LightButtonActive,
                onSecondary = LightBackground,
                tertiary = LightButtonSecondary,
                onTertiary = LightTextPrimary,
                background = LightBackground,
                onBackground = LightTextPrimary,
                surface = LightSurface,
                onSurface = LightTextPrimary,
                surfaceVariant = LightButtonInactive,
                onSurfaceVariant = LightTextSecondary,
                outline = LightCardBorder,
                outlineVariant = LightDivider
        )

private val HighContrastColorScheme =
        lightColorScheme(
                primary = HCPrimary,
                onPrimary = HCBackground,
                secondary = HCButtonActive,
                onSecondary = HCBackground,
                tertiary = HCButtonSecondary,
                onTertiary = HCBackground,
                background = HCBackground,
                onBackground = HCTextPrimary,
                surface = HCSurface,
                onSurface = HCTextPrimary,
                surfaceVariant = HCButtonInactive,
                onSurfaceVariant = HCTextSecondary,
                outline = HCCardBorder,
                outlineVariant = HCDivider
        )

@Composable
fun PureLinkTheme(theme: String = "matrix", content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val scheme = when (theme) {
        "system" -> if (isDark) MatrixColorScheme else LightColorScheme
        "light" -> LightColorScheme
        "high_contrast" -> HighContrastColorScheme
        "matrix" -> MatrixColorScheme
        "amber" -> AmberColorScheme
        "dracula" -> DraculaColorScheme
        "monokai" -> MonokaiColorScheme
        else -> MatrixColorScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
