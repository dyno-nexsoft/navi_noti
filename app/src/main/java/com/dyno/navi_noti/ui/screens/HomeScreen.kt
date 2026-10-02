package com.dyno.navi_noti.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.dyno.navi_noti.R
import com.dyno.navi_noti.ui.MainViewModel
import com.dyno.navi_noti.ui.screens.components.NavigationStatusCard
import com.dyno.navi_noti.ui.screens.components.PermissionCard
import com.dyno.navi_noti.ui.screens.components.SettingsCard
import com.dyno.navi_noti.ui.screens.components.SimulatorControlsCard

/// Màn hình chính điều khiển và giám sát trạng thái đồng bộ thông báo với đồng hồ
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(stringResource(R.string.app_name))
                        Text(stringResource(R.string.subtitle))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PermissionCard(
                isGranted = isPermissionGranted,
                onRefresh = { viewModel.checkPermission() }
            )

            NavigationStatusCard(
                state = currentState,
                step = currentStep,
                appLanguage = appLanguage
            )

            SimulatorControlsCard(
                isSimulatorRunning = isSimulatorRunning,
                onStartSimulation = { viewModel.startSimulation() },
                onStopSimulation = { viewModel.stopSimulation() }
            )

            SettingsCard(
                currentThemeMode = themeMode,
                currentLanguage = appLanguage,
                onThemeModeSelected = { viewModel.setThemeMode(it) },
                onLanguageSelected = { viewModel.setAppLanguage(it) }
            )

            UserGuideSection()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/// Phần tóm tắt cách hoạt động của ứng dụng
@Composable
private fun UserGuideSection() {
    Card {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            Text(stringResource(R.string.how_it_works_title))
            Text(
                text = stringResource(R.string.how_it_works_desc),
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
