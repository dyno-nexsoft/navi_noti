package com.dyno.navi_noti.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.TurnRight
import com.dyno.navi_noti.R
import com.dyno.navi_noti.data.model.NavigationState
import com.dyno.navi_noti.data.model.NavigationStep

/// Thẻ hiển thị trạng thái hiện tại và xem trước giao diện thông báo trên mặt đồng hồ
@Composable
fun NavigationStatusCard(
    state: NavigationState,
    step: NavigationStep?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.journey_status_title))
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Navigation,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    StatusChip(state = state)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            WatchNotificationPreview(step = step, state = state)
        }
    }
}

/// Chip Material 3 hiển thị trạng thái hành trình mà không gợi ý thao tác
@Composable
private fun StatusChip(state: NavigationState) {
    val icon = when (state) {
        NavigationState.IDLE -> Icons.Outlined.RadioButtonUnchecked
        NavigationState.NAVIGATING -> Icons.Outlined.Navigation
        NavigationState.APPROACHING -> Icons.Outlined.TurnRight
        NavigationState.WAITING -> Icons.Outlined.HourglassTop
        NavigationState.PAUSED -> Icons.Outlined.PauseCircle
        NavigationState.COMPLETED -> Icons.Outlined.CheckCircle
    }

    SuggestionChip(
        onClick = {},
        enabled = false,
        label = { Text(stringResource(state.titleRes)) },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    )
}

/// Khung xem trước thông báo như hiển thị trên màn hình đồng hồ thông minh
@Composable
private fun WatchNotificationPreview(step: NavigationStep?, state: NavigationState) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .padding(18.dp)
    ) {
        if (step != null && state.shouldShowNotification) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = step.maneuver.iconRes),
                        contentDescription = step.action,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = step.formatTitle(context),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    Text(
                        text = step.formatContent(context),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.85f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_noti_on_watch),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(R.string.start_maps_or_demo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
