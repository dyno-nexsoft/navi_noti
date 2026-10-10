package com.dyno.navi_noti.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dyno.navi_noti.R

/// Checklist các quyền bắt buộc; khi đủ chỉ còn một dòng xác nhận gọn
@Composable
fun SetupCard(
    isListenerGranted: Boolean,
    isPostGranted: Boolean,
    onOpenListenerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    val ready = isListenerGranted && isPostGranted
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ready) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (ready) {
                SetupRow(
                    done = true,
                    title = stringResource(R.string.setup_ready),
                    description = null,
                    onAction = null
                )
                return@Column
            }
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            SetupRow(
                done = isListenerGranted,
                title = stringResource(R.string.setup_listener_title),
                description = stringResource(R.string.setup_listener_desc),
                onAction = onOpenListenerSettings
            )
            SetupRow(
                done = isPostGranted,
                title = stringResource(R.string.setup_post_title),
                description = stringResource(R.string.setup_post_desc),
                onAction = onOpenNotificationSettings
            )
        }
    }
}

@Composable
private fun SetupRow(
    done: Boolean,
    title: String,
    description: String?,
    onAction: (() -> Unit)?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (done) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!done && onAction != null) {
            Button(onClick = onAction) { Text(stringResource(R.string.setup_open_btn)) }
        }
    }
}
