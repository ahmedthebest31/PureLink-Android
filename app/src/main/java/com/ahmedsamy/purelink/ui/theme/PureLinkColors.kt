package com.ahmedsamy.purelink.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// -- Fixed button label colors (never theme-adaptive) --
val ButtonLabel = Color(0xFFFFFFFF)
val ButtonSecondaryLabel = Color(0xFFDDDDDD)
val ButtonInactiveLabel = Color(0xFFDDDDDD)

data class PureLinkColors(
    val accent: Color,
    val background: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textHint: Color,
    val textLight: Color,
    val textLighter: Color,
    val buttonActive: Color,
    val buttonInactive: Color,
    val buttonSecondary: Color,
    val divider: Color,
    val dividerSoft: Color,
    val statusPaused: Color
)

val MatrixColors = PureLinkColors(
    accent = MatrixGreen,
    background = MatrixBackground,
    card = MatrixCard,
    textPrimary = MatrixTextPrimary,
    textSecondary = MatrixTextSecondary,
    textMuted = MatrixTextMuted,
    textHint = MatrixTextMuted,
    textLight = MatrixTextLight,
    textLighter = MatrixTextLighter,
    buttonActive = MatrixButtonActive,
    buttonInactive = MatrixButtonInactive,
    buttonSecondary = MatrixButtonSecondary,
    divider = MatrixDividerDark,
    dividerSoft = MatrixDividerMedium,
    statusPaused = MatrixStatusPaused
)

val AmberColors = PureLinkColors(
    accent = AmberAccent,
    background = AmberBackground,
    card = AmberBackground,
    textPrimary = AmberAccent,
    textSecondary = AmberSecondary,
    textMuted = AmberMuted,
    textHint = AmberMuted,
    textLight = AmberTextLight,
    textLighter = AmberTextLighter,
    buttonActive = MatrixButtonActive,
    buttonInactive = MatrixButtonInactive,
    buttonSecondary = MatrixButtonSecondary,
    divider = Color(0xFF332200),
    dividerSoft = Color(0xFF332200),
    statusPaused = AmberSecondary
)

val DraculaColors = PureLinkColors(
    accent = DraculaAccent,
    background = DraculaBackground,
    card = DraculaBackground,
    textPrimary = DraculaTextPrimary,
    textSecondary = DraculaTextSecondary,
    textMuted = DraculaTextMuted,
    textHint = DraculaTextMuted,
    textLight = DraculaTextLight,
    textLighter = DraculaTextLighter,
    buttonActive = MatrixButtonActive,
    buttonInactive = DraculaButtonInactive,
    buttonSecondary = Color(0xFF222222),
    divider = DraculaButtonInactive,
    dividerSoft = DraculaButtonInactive,
    statusPaused = DraculaTextSecondary
)

val MonokaiColors = PureLinkColors(
    accent = MonokaiAccent,
    background = MonokaiBackground,
    card = MonokaiBackground,
    textPrimary = MonokaiTextPrimary,
    textSecondary = MonokaiTextSecondary,
    textMuted = MonokaiTextMuted,
    textHint = MonokaiTextMuted,
    textLight = MonokaiTextLight,
    textLighter = MonokaiTextLighter,
    buttonActive = MatrixButtonActive,
    buttonInactive = MonokaiButtonInactive,
    buttonSecondary = MonokaiButtonSecondary,
    divider = MonokaiButtonInactive,
    dividerSoft = MonokaiButtonInactive,
    statusPaused = MonokaiTextSecondary
)

val LightColors = PureLinkColors(
    accent = LightPrimary,
    background = LightBackground,
    card = LightSurface,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    textHint = LightTextMuted,
    textLight = Color(0xFF333333),
    textLighter = LightTextLighter,
    buttonActive = LightButtonActive,
    buttonInactive = Color(0xFF333333),
    buttonSecondary = Color(0xFF222222),
    divider = LightDivider,
    dividerSoft = LightDivider,
    statusPaused = LightTextLighter
)

val HighContrastColors = PureLinkColors(
    accent = HCPrimary,
    background = HCBackground,
    card = HCSurface,
    textPrimary = HCTextPrimary,
    textSecondary = HCTextSecondary,
    textMuted = HCTextMuted,
    textHint = HCTextMuted,
    textLight = Color(0xFF333333),
    textLighter = HCTextLighter,
    buttonActive = HCButtonActive,
    buttonInactive = Color(0xFF333333),
    buttonSecondary = Color(0xFF222222),
    divider = HCDivider,
    dividerSoft = Color(0xFF666666),
    statusPaused = HCTextLighter
)

val LocalPureLinkColors = staticCompositionLocalOf { MatrixColors }