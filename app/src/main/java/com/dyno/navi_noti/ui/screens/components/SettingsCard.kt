package com.dyno.navi_noti.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dyno.navi_noti.R
import com.dyno.navi_noti.data.model.AppLanguage
import com.dyno.navi_noti.data.model.NotificationStyle
import com.dyno.navi_noti.data.model.NotificationTemplate
import com.dyno.navi_noti.data.model.ThemeMode

@Composable
fun SettingsCard(
    notificationStyle: NotificationStyle,
    notificationTemplate: NotificationTemplate,
    themeMode: ThemeMode,
    appLanguage: AppLanguage,
    onNotificationStyleSelected: (NotificationStyle) -> Unit,
    onNotificationTemplateChanged: (NotificationTemplate) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        BoxWithConstraints {
            val compactLabels = maxWidth < 340.dp
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                NotificationStyleSection(
                    style = notificationStyle,
                    template = notificationTemplate,
                    onStyleSelected = onNotificationStyleSelected,
                    onTemplateChanged = onNotificationTemplateChanged
                )
                HorizontalDivider()
                PreferenceSelector(
                    title = stringResource(R.string.theme_mode_label),
                    options = ThemeMode.entries,
                    selected = themeMode,
                    optionLabel = { mode ->
                        stringResource(
                            if (compactLabels && mode == ThemeMode.SYSTEM) R.string.theme_system_short
                            else mode.titleRes
                        )
                    },
                    onSelected = onThemeModeSelected
                )
                PreferenceSelector(
                    title = stringResource(R.string.language_label),
                    options = AppLanguage.entries,
                    selected = appLanguage,
                    optionLabel = { language ->
                        stringResource(
                            if (compactLabels) {
                                when (language) {
                                    AppLanguage.SYSTEM -> R.string.lang_system_short
                                    AppLanguage.VI -> R.string.lang_vi_short
                                    AppLanguage.EN -> R.string.lang_en_short
                                }
                            } else {
                                language.titleRes
                            }
                        )
                    },
                    onSelected = onLanguageSelected
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotificationStyleSection(
    style: NotificationStyle,
    template: NotificationTemplate,
    onStyleSelected: (NotificationStyle) -> Unit,
    onTemplateChanged: (NotificationTemplate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.notification_style_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NotificationStyle.entries.forEach { option ->
                FilterChip(
                    selected = option == style,
                    onClick = { onStyleSelected(option) },
                    label = { Text(stringResource(option.titleRes)) }
                )
            }
        }
        Text(
            text = stringResource(style.descRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnimatedVisibility(visible = style == NotificationStyle.CUSTOM) {
            TemplateEditor(template, onTemplateChanged)
        }
    }
}

/// Soạn mẫu tuỳ chỉnh; chạm vào biến để chèn vào ô đang chọn tại vị trí con trỏ
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateEditor(
    template: NotificationTemplate,
    onTemplateChanged: (NotificationTemplate) -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue(template.title)) }
    var content by remember { mutableStateOf(TextFieldValue(template.content)) }
    var editingTitle by remember { mutableStateOf(true) }

    fun publish() = onTemplateChanged(NotificationTemplate(title.text, content.text))

    Column(
        modifier = Modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it; publish() },
            label = { Text(stringResource(R.string.template_title_label)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) editingTitle = true }
        )
        OutlinedTextField(
            value = content,
            onValueChange = { content = it; publish() },
            label = { Text(stringResource(R.string.template_content_label)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) editingTitle = false }
        )
        Text(
            text = stringResource(R.string.template_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NotificationTemplate.TOKENS.forEach { token ->
                AssistChip(
                    onClick = {
                        if (editingTitle) title = title.insert(token) else content = content.insert(token)
                        publish()
                    },
                    label = { Text(token) }
                )
            }
            TextButton(onClick = {
                title = TextFieldValue(NotificationTemplate.DEFAULT_TITLE)
                content = TextFieldValue(NotificationTemplate.DEFAULT_CONTENT)
                publish()
            }) { Text(stringResource(R.string.template_reset)) }
        }
    }
}

private fun TextFieldValue.insert(token: String): TextFieldValue {
    val start = selection.min
    val newText = text.substring(0, start) + token + text.substring(selection.max)
    return TextFieldValue(newText, TextRange(start + token.length))
}

@Composable
private fun <T> PreferenceSelector(
    title: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    label = {
                        Text(
                            text = optionLabel(option),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}
