package com.dyno.navi_noti.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import com.dyno.navi_noti.R
import com.dyno.navi_noti.data.model.AppLanguage
import com.dyno.navi_noti.data.model.ThemeMode

/// Thẻ cài đặt giao diện (Sáng/Tối/Hệ thống) và ngôn ngữ (Tiếng Việt/Tiếng Anh/Hệ thống)
@Composable
fun SettingsCard(
    currentThemeMode: ThemeMode,
    currentLanguage: AppLanguage,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ThemeSelectorSection(
                currentThemeMode = currentThemeMode,
                onThemeModeSelected = onThemeModeSelected
            )

            LanguageSelectorSection(
                currentLanguage = currentLanguage,
                onLanguageSelected = onLanguageSelected
            )
        }
    }
}

/// Khu vực chọn chế độ giao diện Sáng / Tối / Hệ thống
@Composable
private fun ThemeSelectorSection(
    currentThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.theme_mode_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == currentThemeMode,
                    onClick = { onThemeModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemeMode.entries.size
                    ),
                    label = {
                        SegmentedOptionLabel(
                            icon = when (mode) {
                                ThemeMode.SYSTEM -> Icons.Outlined.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Outlined.LightMode
                                ThemeMode.DARK -> Icons.Outlined.DarkMode
                            },
                            text = stringResource(mode.titleRes)
                        )
                    }
                )
            }
        }
    }
}

/// Khu vực chọn ngôn ngữ Tiếng Việt / English / Hệ thống
@Composable
private fun LanguageSelectorSection(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.language_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            AppLanguage.entries.forEachIndexed { index, lang ->
                SegmentedButton(
                    selected = lang == currentLanguage,
                    onClick = { onLanguageSelected(lang) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = AppLanguage.entries.size
                    ),
                    label = {
                        SegmentedOptionLabel(
                            icon = Icons.Outlined.Language,
                            text = stringResource(lang.titleRes)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SegmentedOptionLabel(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(text)
    }
}
