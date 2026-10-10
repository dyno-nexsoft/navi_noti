package com.dyno.navi_noti.ui.screens.components

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dyno.navi_noti.R
import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.data.model.NavigationState
import com.dyno.navi_noti.data.model.NavigationStep
import com.dyno.navi_noti.data.model.NotificationFormatter
import com.dyno.navi_noti.data.model.NotificationStyle
import com.dyno.navi_noti.data.model.NotificationTemplate

/// Thẻ chính: hướng rẽ kế tiếp nổi bật nhất, kèm bản xem trước đúng nội dung đồng hồ nhận
@Composable
fun JourneyCard(
    state: NavigationState,
    step: NavigationStep?,
    localizedContext: Context,
    notificationStyle: NotificationStyle,
    notificationTemplate: NotificationTemplate,
    isSimulatorRunning: Boolean,
    isTripMuted: Boolean,
    onUnmuteTrip: () -> Unit,
    onOpenMaps: () -> Unit,
    onStartSimulation: () -> Unit,
    onStopSimulation: () -> Unit
) {
    val activeStep = step?.takeIf { state.shouldShowNotification }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(state)
                if (isTripMuted && state.shouldShowNotification) {
                    AssistChip(
                        onClick = onUnmuteTrip,
                        label = { Text(stringResource(R.string.trip_muted)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.NotificationsOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
            ManeuverHeader(activeStep, localizedContext)
            DistanceProgress(activeStep?.distanceMeters)
            WatchPreview(
                step = activeStep ?: sampleStep(localizedContext),
                isSample = activeStep == null,
                localizedContext = localizedContext,
                style = notificationStyle,
                template = notificationTemplate
            )
            JourneyActions(isSimulatorRunning, onOpenMaps, onStartSimulation, onStopSimulation)
        }
    }
}

@Composable
private fun StatusPill(state: NavigationState) {
    val active = state.shouldShowNotification
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
        )
        Text(
            text = stringResource(state.titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ManeuverHeader(step: NavigationStep?, localizedContext: Context) {
    AnimatedContent(
        targetState = step,
        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith fadeOut() },
        contentKey = { it?.maneuver to it?.streetName },
        label = "maneuver"
    ) { current ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource((current?.maneuver ?: ManeuverType.WAITING).iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                current?.distanceText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = current?.formatTitle(localizedContext)
                        ?: stringResource(R.string.no_noti_on_watch),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = current?.streetName ?: if (current == null) {
                        stringResource(R.string.start_maps_or_demo)
                    } else {
                        current.formatContent(localizedContext)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/// Thanh tiến độ tới điểm rẽ trong 300 m cuối, các mốc rung là 100 m và 30 m
@Composable
private fun DistanceProgress(distanceMeters: Int?) {
    val visible = distanceMeters != null && distanceMeters <= PROGRESS_RANGE_METERS
    val progress by animateFloatAsState(
        targetValue = distanceMeters?.let { 1f - it.coerceIn(0, PROGRESS_RANGE_METERS) / PROGRESS_RANGE_METERS.toFloat() } ?: 0f,
        label = "distanceProgress"
    )
    AnimatedVisibility(visible = visible) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    }
}

/// Mô phỏng màn hình thông báo trên đồng hồ: nền tối, chữ lớn, đúng tiêu đề/nội dung sẽ gửi
@Composable
private fun WatchPreview(
    step: NavigationStep,
    isSample: Boolean,
    localizedContext: Context,
    style: NotificationStyle,
    template: NotificationTemplate
) {
    val text = NotificationFormatter.format(step, localizedContext, style, template)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.watch_preview_label) +
                if (isSample) " · " + stringResource(R.string.preview_sample) else "",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF111316))
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(step.maneuver.iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = text.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = text.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun JourneyActions(
    isSimulatorRunning: Boolean,
    onOpenMaps: () -> Unit,
    onStartSimulation: () -> Unit,
    onStopSimulation: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilledTonalButton(
            onClick = onOpenMaps,
            modifier = Modifier.weight(1f),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(stringResource(R.string.open_maps_btn), modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(
            onClick = if (isSimulatorRunning) onStopSimulation else onStartSimulation,
            modifier = Modifier.weight(1f),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Icon(
                imageVector = if (isSimulatorRunning) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(
                    if (isSimulatorRunning) R.string.stop_short_btn else R.string.demo_short_btn
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

private fun sampleStep(context: Context) = NavigationStep(
    action = ManeuverType.TURN_LEFT.defaultActionName,
    distanceText = "200 m",
    streetName = context.getString(R.string.preview_sample_street),
    distanceMeters = 200,
    maneuver = ManeuverType.TURN_LEFT
)

private const val PROGRESS_RANGE_METERS = 300
