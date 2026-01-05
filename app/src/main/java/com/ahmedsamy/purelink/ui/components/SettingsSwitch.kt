package com.ahmedsamy.purelink.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ahmedsamy.purelink.ui.theme.TerminalGreen
import com.ahmedsamy.purelink.ui.theme.TextLight

/**
 * A reusable settings toggle row with label and switch.
 * Accessible with proper touch target and semantics.
 */
@Composable
fun SettingsSwitch(
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier
) {
    Row(
            modifier = modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.Switch
                )
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = label,
                color = TextLight,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
        )
        Switch(
                checked = checked,
                onCheckedChange = null, // Handled by parent Row toggleable
                colors =
                        SwitchDefaults.colors(
                                checkedThumbColor = TerminalGreen,
                                checkedTrackColor = TerminalGreen.copy(alpha = 0.5f)
                        ),
                modifier = Modifier.clearAndSetSemantics { } // Clear switch semantics to avoid duplication
        )
    }
}