package com.dyno.navi_noti.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.dyno.navi_noti.R
import com.dyno.navi_noti.service.GoogleMapsNotificationListenerService
import com.dyno.navi_noti.ui.MainViewModel
import com.dyno.navi_noti.ui.screens.components.AlertCard
import com.dyno.navi_noti.ui.screens.components.GuideCard
import com.dyno.navi_noti.ui.screens.components.JourneyCard
import com.dyno.navi_noti.ui.screens.components.SettingsCard
import com.dyno.navi_noti.ui.screens.components.SetupCard
import com.dyno.navi_noti.util.withAppLanguage
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentState by viewModel.currentState.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val isSimulatorRunning by viewModel.isSimulatorRunning.collectAsState()
    val isListenerGranted by viewModel.isNotificationListenerGranted.collectAsState()
    val isPostGranted by viewModel.isPostNotificationGranted.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val notificationStyle by viewModel.notificationStyle.collectAsState()
    val notificationTemplate by viewModel.notificationTemplate.collectAsState()
    val alertPreset by viewModel.alertPreset.collectAsState()
    val showEta by viewModel.showEta.collectAsState()
    val isTripMuted by viewModel.isTripMuted.collectAsState()
    val customAlertSettings by viewModel.customAlertSettings.collectAsState()
    val context = LocalContext.current
    val hingeWidth = rememberVerticalHingeWidth(context)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compactHeight = maxHeight < 480.dp
        Scaffold(
            topBar = { HomeTopBar(showSubtitle = !compactHeight) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                val wideLayout = maxWidth >= 600.dp
                // Điện thoại nhỏ (dưới 360 dp) hoặc màn thấp (nằm ngang): thu gọn khoảng cách và kích thước
                val narrow = maxWidth < 360.dp
                val compactCards = narrow || compactHeight
                val spacing = if (compactHeight) 12.dp else 16.dp
                val localizedContext = context.withAppLanguage(appLanguage)
                val setupReady = isListenerGranted && isPostGranted

                val journey: @Composable () -> Unit = {
                    JourneyCard(
                        state = currentState,
                        step = currentStep,
                        localizedContext = localizedContext,
                        notificationStyle = notificationStyle,
                        notificationTemplate = notificationTemplate,
                        isSimulatorRunning = isSimulatorRunning,
                        compact = compactCards,
                        isTripMuted = isTripMuted,
                        onUnmuteTrip = { viewModel.setTripMuted(false) },
                        onOpenMaps = { context.openGoogleMaps() },
                        onStartSimulation = viewModel::startSimulation,
                        onStopSimulation = viewModel::stopSimulation
                    )
                }
                val setup: @Composable () -> Unit = {
                    SetupCard(
                        isListenerGranted = isListenerGranted,
                        isPostGranted = isPostGranted,
                        onOpenListenerSettings = { context.openListenerSettings() },
                        onOpenNotificationSettings = { context.openAppNotificationSettings() }
                    )
                }
                val settings: @Composable () -> Unit = {
                    SettingsCard(
                        notificationStyle = notificationStyle,
                        notificationTemplate = notificationTemplate,
                        showEta = showEta,
                        onShowEtaChanged = viewModel::setShowEta,
                        themeMode = themeMode,
                        appLanguage = appLanguage,
                        onNotificationStyleSelected = viewModel::setNotificationStyle,
                        onNotificationTemplateChanged = viewModel::setNotificationTemplate,
                        onThemeModeSelected = viewModel::setThemeMode,
                        onLanguageSelected = viewModel::setAppLanguage
                    )
                }
                // Chưa cấp đủ quyền thì việc cần làm đầu tiên là thiết lập, đưa lên trên cùng
                val primary = if (setupReady) listOf(journey, setup) else listOf(setup, journey)
                val alerts: @Composable () -> Unit = {
                    AlertCard(
                        preset = alertPreset,
                        customSettings = customAlertSettings,
                        onPresetSelected = viewModel::setAlertPreset,
                        onCustomSettingsChanged = viewModel::setCustomAlertSettings
                    )
                }
                val secondary = listOf(alerts, settings, { GuideCard() })

                Column(
                    modifier = Modifier
                        .widthIn(max = if (wideLayout) 1200.dp else 680.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = if (compactCards) 12.dp else 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    if (wideLayout) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(hingeWidth?.plus(spacing) ?: spacing),
                            verticalAlignment = Alignment.Top
                        ) {
                            CardColumn(primary, spacing, Modifier.weight(1f))
                            CardColumn(secondary, spacing, Modifier.weight(1f))
                        }
                    } else {
                        (primary + secondary).forEach { it() }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(showSubtitle: Boolean) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showSubtitle) {
                    Text(
                        text = stringResource(R.string.subtitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}

@Composable
private fun CardColumn(cards: List<@Composable () -> Unit>, spacing: Dp, modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        cards.forEach { it() }
    }
}

/// Độ rộng bản lề dọc của máy gập, để hai cột không bị chia cắt bởi nếp gập
@Composable
private fun rememberVerticalHingeWidth(context: Context): Dp? {
    val density = LocalDensity.current
    val activity = context.findActivity()
    val hingeWidth by produceState<Dp?>(null, activity, density) {
        if (activity == null) return@produceState
        activity.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                .map { layoutInfo ->
                    layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull { feature ->
                            feature.orientation == FoldingFeature.Orientation.VERTICAL &&
                                (feature.isSeparating ||
                                    feature.occlusionType == FoldingFeature.OcclusionType.FULL)
                        }
                        ?.bounds
                        ?.width()
                        ?.let { with(density) { it.toDp() } }
                }
                .collect { value = it }
        }
    }
    return hingeWidth
}

private fun Context.openGoogleMaps() {
    val intent = packageManager.getLaunchIntentForPackage(
        GoogleMapsNotificationListenerService.GOOGLE_MAPS_PACKAGE
    ) ?: return
    launch(intent)
}

private fun Context.openListenerSettings() {
    launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
}

private fun Context.openAppNotificationSettings() {
    launch(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    )
}

/// LocalContext có thể là context đã đổi ngôn ngữ (không phải Activity) nên cần NEW_TASK
private fun Context.launch(intent: Intent) {
    runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
