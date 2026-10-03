package com.dyno.navi_noti.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.TurnRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.dyno.navi_noti.R
import com.dyno.navi_noti.data.model.AppLanguage
import com.dyno.navi_noti.data.model.NavigationState
import com.dyno.navi_noti.data.model.NavigationStep
import com.dyno.navi_noti.data.model.ThemeMode
import com.dyno.navi_noti.ui.MainViewModel
import com.dyno.navi_noti.util.withAppLanguage
import kotlinx.coroutines.flow.collect
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
    val isPermissionGranted by viewModel.isNotificationListenerGranted.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val context = LocalContext.current
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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compactHeight = maxHeight < 480.dp
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!compactHeight) {
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
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                val wideLayout = maxWidth >= 600.dp
                val spacing = if (compactHeight) 12.dp else 18.dp
                val horizontalPadding = if (compactHeight) 12.dp else 20.dp
                val localizedContext = context.withAppLanguage(appLanguage)

                Column(
                    modifier = Modifier
                        .widthIn(max = if (wideLayout) 1200.dp else 680.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    val permissionPanel: @Composable () -> Unit = {
                        PermissionPanel(
                            isGranted = isPermissionGranted,
                            onOpenSettings = {
                                context.startActivity(
                                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                )
                            }
                        )
                    }
                    val preferencesPanel: @Composable () -> Unit = {
                        PreferencesPanel(
                            themeMode = themeMode,
                            appLanguage = appLanguage,
                            onThemeModeSelected = viewModel::setThemeMode,
                            onLanguageSelected = viewModel::setAppLanguage
                        )
                    }
                    val guidePanel: @Composable () -> Unit = { GuidePanel() }

                    if (wideLayout) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                hingeWidth?.plus(spacing) ?: spacing
                            ),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(spacing)
                            ) {
                                JourneyPanel(
                                    state = currentState,
                                    step = currentStep,
                                    localizedContext = localizedContext,
                                    isSimulatorRunning = isSimulatorRunning,
                                    onStartSimulation = viewModel::startSimulation,
                                    onStopSimulation = viewModel::stopSimulation
                                )
                                permissionPanel()
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(spacing)
                            ) {
                                preferencesPanel()
                                guidePanel()
                            }
                        }
                    } else {
                        JourneyPanel(
                            state = currentState,
                            step = currentStep,
                            localizedContext = localizedContext,
                            isSimulatorRunning = isSimulatorRunning,
                            onStartSimulation = viewModel::startSimulation,
                            onStopSimulation = viewModel::stopSimulation
                        )
                        permissionPanel()
                        preferencesPanel()
                        guidePanel()
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun JourneyPanel(
    state: NavigationState,
    step: NavigationStep?,
    localizedContext: Context,
    isSimulatorRunning: Boolean,
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = navigationIcon(state),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(state.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activeStep?.formatNotificationTitle(localizedContext)
                            ?: stringResource(R.string.no_noti_on_watch),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = activeStep?.formatContent(localizedContext)
                    ?: stringResource(R.string.start_maps_or_demo),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            if (isSimulatorRunning) {
                OutlinedButton(
                    onClick = onStopSimulation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.demo_stop_btn))
                }
            } else {
                Button(
                    onClick = onStartSimulation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.demo_start_btn))
                }
            }
        }
    }
}

@Composable
private fun PermissionPanel(
    isGranted: Boolean,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) {
                    Icons.Outlined.NotificationsActive
                } else {
                    Icons.Outlined.NotificationsOff
                },
                contentDescription = null,
                tint = if (isGranted) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(
                        if (isGranted) R.string.perm_ready_title else R.string.perm_needed_title
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        if (isGranted) R.string.perm_ready_desc else R.string.perm_needed_desc
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isGranted) {
                    Button(onClick = onOpenSettings) {
                        Text(stringResource(R.string.perm_grant_btn))
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferencesPanel(
    themeMode: ThemeMode,
    appLanguage: AppLanguage,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
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
                PreferenceSelector(
                    title = stringResource(R.string.theme_mode_label),
                    options = ThemeMode.entries,
                    selected = themeMode,
                    optionLabel = { mode ->
                        stringResource(
                            if (compactLabels) {
                                when (mode) {
                                    ThemeMode.SYSTEM -> R.string.theme_system_short
                                    ThemeMode.LIGHT -> R.string.theme_light
                                    ThemeMode.DARK -> R.string.theme_dark
                                }
                            } else {
                                mode.titleRes
                            }
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

@Composable
private fun GuidePanel() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.how_it_works_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(R.string.how_it_works_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

private fun navigationIcon(state: NavigationState) = when (state) {
    NavigationState.IDLE -> Icons.Outlined.RadioButtonUnchecked
    NavigationState.NAVIGATING -> Icons.Outlined.Navigation
    NavigationState.APPROACHING -> Icons.Outlined.TurnRight
    NavigationState.WAITING -> Icons.Outlined.HourglassTop
    NavigationState.PAUSED -> Icons.Outlined.PauseCircle
    NavigationState.COMPLETED -> Icons.Outlined.CheckCircle
}

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
