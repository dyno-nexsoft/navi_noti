package com.dyno.navi_noti.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dyno.navi_noti.R
import com.dyno.navi_noti.data.model.AlertPreset
import com.dyno.navi_noti.data.model.AlertSettings

/// Chọn mức rung để đồng hồ chỉ rung ở những mốc thật sự cần
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertCard(
    preset: AlertPreset,
    customSettings: AlertSettings,
    onPresetSelected: (AlertPreset) -> Unit,
    onCustomSettingsChanged: (AlertSettings) -> Unit
) {
    val active = preset.settings ?: customSettings
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(
                        text = stringResource(R.string.alert_section_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.alert_summary, active.maxAlertsPerTurn),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlertPreset.entries.forEach { option ->
                    FilterChip(
                        selected = option == preset,
                        onClick = { onPresetSelected(option) },
                        label = { Text(stringResource(option.titleRes)) }
                    )
                }
            }
            Text(
                text = stringResource(preset.descRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedVisibility(visible = preset == AlertPreset.CUSTOM) {
                CustomAlertEditor(customSettings, onCustomSettingsChanged)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomAlertEditor(settings: AlertSettings, onChanged: (AlertSettings) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        SwitchRow(stringResource(R.string.alert_far), settings.alertFar) { onChanged(settings.copy(alertFar = it)) }
        SwitchRow(stringResource(R.string.alert_near), settings.alertNear) { onChanged(settings.copy(alertNear = it)) }
        SwitchRow(stringResource(R.string.alert_turn_point), settings.alertTurnPoint) {
            onChanged(settings.copy(alertTurnPoint = it))
        }
        SwitchRow(stringResource(R.string.alert_arrival), settings.alertArrival) {
            onChanged(settings.copy(alertArrival = it))
        }
        SwitchRow(stringResource(R.string.alert_turns_only), settings.turnsOnly) {
            onChanged(settings.copy(turnsOnly = it))
        }
        Text(
            text = stringResource(R.string.alert_cooldown),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AlertSettings.COOLDOWN_OPTIONS.forEach { seconds ->
                FilterChip(
                    selected = settings.cooldownSeconds == seconds,
                    onClick = { onChanged(settings.copy(cooldownSeconds = seconds)) },
                    label = {
                        Text(
                            if (seconds == 0) stringResource(R.string.alert_cooldown_off)
                            else stringResource(R.string.alert_cooldown_value, seconds)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}
