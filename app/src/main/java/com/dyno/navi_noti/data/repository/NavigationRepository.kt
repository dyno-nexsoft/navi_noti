package com.dyno.navi_noti.data.repository

import com.dyno.navi_noti.data.model.NavigationState
import com.dyno.navi_noti.data.model.NavigationStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/// Kho lưu trữ tập trung chia sẻ trạng thái điều hướng giữa Service, Simulator và UI
object NavigationRepository {

    private val _currentState = MutableStateFlow(NavigationState.IDLE)
    val currentState: StateFlow<NavigationState> = _currentState.asStateFlow()

    private val _currentStep = MutableStateFlow<NavigationStep?>(null)
    val currentStep: StateFlow<NavigationStep?> = _currentStep.asStateFlow()

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _isSimulatorRunning = MutableStateFlow(false)
    val isSimulatorRunning: StateFlow<Boolean> = _isSimulatorRunning.asStateFlow()

    /// Cập nhật bước điều hướng hiện tại và tự động suy luận trạng thái tương ứng
    fun updateStep(step: NavigationStep?) {
        _currentStep.value = step
        if (step == null) {
            _currentState.value = NavigationState.IDLE
        } else {
            _currentState.value = when {
                step.isDestination -> NavigationState.COMPLETED
                step.isWaiting -> NavigationState.WAITING
                step.isApproaching -> NavigationState.APPROACHING
                else -> NavigationState.NAVIGATING
            }
        }
    }

    /// Cập nhật rõ ràng trạng thái điều hướng
    fun setState(state: NavigationState) {
        _currentState.value = state
        if (state == NavigationState.COMPLETED || state == NavigationState.PAUSED || state == NavigationState.IDLE) {
            _currentStep.value = null
        }
    }

    /// Đánh dấu trạng thái kết nối của dịch vụ Notification Listener
    fun setServiceConnected(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    /// Đánh dấu trạng thái trình mô phỏng hành trình mẫu
    fun setSimulatorRunning(running: Boolean) {
        _isSimulatorRunning.value = running
    }
}
