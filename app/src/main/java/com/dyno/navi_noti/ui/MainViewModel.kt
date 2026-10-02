package com.dyno.navi_noti.ui

import android.app.Application
import android.content.ComponentName
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dyno.navi_noti.data.model.AppLanguage
import com.dyno.navi_noti.data.model.NavigationState
import com.dyno.navi_noti.data.model.NavigationStep
import com.dyno.navi_noti.data.model.ThemeMode
import com.dyno.navi_noti.data.preference.PreferencesManager
import com.dyno.navi_noti.data.repository.NavigationRepository
import com.dyno.navi_noti.data.simulator.SampleJourneySimulator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

/// ViewModel quản lý dữ liệu và điều phối hành động cho màn hình chính Navi Noti
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val simulator = SampleJourneySimulator(application)

    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode
    val appLanguage: StateFlow<AppLanguage> = preferencesManager.appLanguage

    val currentState: StateFlow<NavigationState> = NavigationRepository.currentState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NavigationState.IDLE)

    val currentStep: StateFlow<NavigationStep?> = NavigationRepository.currentStep
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSimulatorRunning: StateFlow<Boolean> = NavigationRepository.isSimulatorRunning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isNotificationListenerGranted = MutableStateFlow(false)
    val isNotificationListenerGranted: StateFlow<Boolean> = _isNotificationListenerGranted.asStateFlow()

    init {
        checkPermission()
    }

    /// Cập nhật chế độ giao diện Sáng / Tối / Hệ thống
    fun setThemeMode(mode: ThemeMode) {
        preferencesManager.setThemeMode(mode)
    }

    /// Cập nhật ngôn ngữ Tiếng Việt / Tiếng Anh / Hệ thống
    fun setAppLanguage(lang: AppLanguage) {
        preferencesManager.setAppLanguage(lang)
    }

    /// Kiểm tra quyền truy cập thông báo hệ thống (Notification Listener Access)
    fun checkPermission() {
        val context = getApplication<Application>()
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val isGranted = flat?.split(":")?.any {
            val component = ComponentName.unflattenFromString(it)
            component?.packageName == packageName
        } == true
        _isNotificationListenerGranted.value = isGranted
    }

    /// Kích hoạt chạy thử kịch bản hành trình mẫu trên đồng hồ
    fun startSimulation() {
        simulator.startSimulation()
    }

    /// Dừng kịch bản mô phỏng và gỡ thông báo
    fun stopSimulation() {
        simulator.stopSimulation()
    }

    override fun onCleared() {
        super.onCleared()
        simulator.stopSimulation()
    }
}
