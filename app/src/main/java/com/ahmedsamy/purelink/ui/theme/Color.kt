package com.ahmedsamy.purelink.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// -- Matrix Green (Default) raw palette --
val MatrixGreen = Color(0xFF00FF00)
val MatrixBackground = Color(0xFF000000)
val MatrixCard = Color(0xFF000000)
val MatrixBorder = Color(0xFF00FF00)

val MatrixTextPrimary = Color(0xFFFFFFFF)
val MatrixTextSecondary = Color(0xFF888888)
val MatrixTextMuted = Color(0xFF7A7A7A)
val MatrixTextLight = Color(0xFFDDDDDD)
val MatrixTextLighter = Color(0xFFAAAAAA)

val MatrixButtonActive = Color(0xFF006600)
val MatrixButtonInactive = Color(0xFF333333)
val MatrixButtonSecondary = Color(0xFF222222)

val MatrixDividerDark = Color(0xFF222222)
val MatrixDividerMedium = Color(0xFF333333)
val MatrixStatusPaused = Color(0xFFAAAAAA)

// -- Amber CRT --
val AmberAccent = Color(0xFFFFB000)
val AmberBackground = Color(0xFF0A0800)
val AmberSecondary = Color(0xFFC99900)
val AmberMuted = Color(0xFFB08000)
val AmberTextLight = Color(0xFFFFD04D)
val AmberTextLighter = Color(0xFFFFE9A8)

// -- Dracula --
val DraculaAccent = Color(0xFFFF79C6)
val DraculaPurple = Color(0xFFBD93F9)
val DraculaBackground = Color(0xFF1E1F2B)
val DraculaTextPrimary = Color(0xFFF8F8F2)
val DraculaTextSecondary = Color(0xFFA0A0B0)
val DraculaTextMuted = Color(0xFF9494A8)
val DraculaTextLight = Color(0xFFF1F1F7)
val DraculaTextLighter = Color(0xFFCFCFDA)
val DraculaButtonInactive = Color(0xFF34353F)

// -- Monokai --
val MonokaiAccent = Color(0xFFA6E22E)
val MonokaiYellow = Color(0xFFE6DB74)
val MonokaiBackground = Color(0xFF272822)
val MonokaiTextPrimary = Color(0xFFF8F8F2)
val MonokaiTextSecondary = Color(0xFFA0A08F)
val MonokaiTextMuted = Color(0xFF91917F)
val MonokaiTextLight = Color(0xFFF1F1E6)
val MonokaiTextLighter = Color(0xFFCDCDBD)
val MonokaiButtonInactive = Color(0xFF3A3D32)
val MonokaiButtonSecondary = Color(0xFF2D2E24)

// -- Light Theme --
val LightBackground = Color(0xFFF5F5F5)
val LightSurface = Color(0xFFFFFFFF)
val LightPrimary = Color(0xFF006600)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightTextPrimary = Color(0xFF1A1A1A)
val LightTextSecondary = Color(0xFF666666)
val LightTextMuted = Color(0xFF5F5F5F)
val LightTextLighter = Color(0xFF4F4F4F)
val LightDivider = Color(0xFFE0E0E0)
val LightCardBorder = Color(0xFF006600)
val LightButtonActive = Color(0xFF006600)
val LightButtonInactive = Color(0xFFCCCCCC)
val LightButtonSecondary = Color(0xFFDDDDDD)

// -- High Contrast --
val HCBackground = Color(0xFFFFFFFF)
val HCSurface = Color(0xFFFFFFFF)
val HCPrimary = Color(0xFF000000)
val HCTextPrimary = Color(0xFF000000)
val HCTextSecondary = Color(0xFF333333)
val HCTextMuted = Color(0xFF555555)
val HCTextLighter = Color(0xFF222222)
val HCDivider = Color(0xFF000000)
val HCCardBorder = Color(0xFF000000)
val HCButtonActive = Color(0xFF000000)
val HCButtonInactive = Color(0xFF888888)
val HCButtonSecondary = Color(0xFF666666)

// -- Theme-aware semantic colors (follow the active theme) --
val TerminalGreen: Color
    @Composable get() = LocalPureLinkColors.current.accent

val TerminalCardBackground: Color
    @Composable get() = LocalPureLinkColors.current.card

val TextPrimary: Color
    @Composable get() = LocalPureLinkColors.current.textPrimary

val TextSecondary: Color
    @Composable get() = LocalPureLinkColors.current.textSecondary

val TextMuted: Color
    @Composable get() = LocalPureLinkColors.current.textMuted

val TextHint: Color
    @Composable get() = LocalPureLinkColors.current.textHint

val TextLight: Color
    @Composable get() = LocalPureLinkColors.current.textLight

val TextLighter: Color
    @Composable get() = LocalPureLinkColors.current.textLighter

val ButtonActive: Color
    @Composable get() = LocalPureLinkColors.current.buttonActive

val ButtonInactive: Color
    @Composable get() = LocalPureLinkColors.current.buttonInactive

val ButtonSecondary: Color
    @Composable get() = LocalPureLinkColors.current.buttonSecondary

val DividerDark: Color
    @Composable get() = LocalPureLinkColors.current.divider

val DividerMedium: Color
    @Composable get() = LocalPureLinkColors.current.dividerSoft

val StatusPaused: Color
    @Composable get() = LocalPureLinkColors.current.statusPaused