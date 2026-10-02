package com.dyno.navi_noti.data.simulator

import android.content.Context
import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.data.model.NavigationState
import com.dyno.navi_noti.data.model.NavigationStep
import com.dyno.navi_noti.data.repository.NavigationRepository
import com.dyno.navi_noti.service.NavigationNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/// Trình mô phỏng hành trình mẫu giúp người dùng kiểm tra thông báo trên đồng hồ
class SampleJourneySimulator(private val context: Context) {

    private val notificationManager = NavigationNotificationManager(context)
    private val scope = CoroutineScope(Dispatchers.Default)
    private var simulationJob: Job? = null

    private val sampleRoute = listOf(
        // Bước 1: Bắt đầu đi thẳng
        NavigationStep(
            action = "Đi thẳng",
            distanceText = "500 m",
            streetName = "Đường Lê Lợi",
            distanceMeters = 500,
            maneuver = ManeuverType.STRAIGHT
        ) to 3500L,

        // Bước 2: Chuẩn bị rẽ phải (mốc ~300m - Alert)
        NavigationStep(
            action = "Rẽ phải",
            distanceText = "300 m",
            streetName = "Đường Nguyễn Huệ",
            distanceMeters = 300,
            maneuver = ManeuverType.TURN_RIGHT
        ) to 3000L,

        // Bước 3: Cập nhật khoảng cách 180m (Silent update)
        NavigationStep(
            action = "Rẽ phải",
            distanceText = "180 m",
            streetName = "Đường Nguyễn Huệ",
            distanceMeters = 180,
            maneuver = ManeuverType.TURN_RIGHT
        ) to 3000L,

        // Bước 4: Đến gần điểm rẽ 90m (mốc ~100m - Alert)
        NavigationStep(
            action = "Rẽ phải",
            distanceText = "90 m",
            streetName = "Đường Nguyễn Huệ",
            distanceMeters = 90,
            maneuver = ManeuverType.TURN_RIGHT
        ) to 3000L,

        // Bước 5: Ngay tại điểm rẽ (< 35m - Alert)
        NavigationStep(
            action = "Rẽ phải",
            distanceText = "20 m",
            streetName = "Đường Nguyễn Huệ",
            distanceMeters = 20,
            isApproaching = true,
            maneuver = ManeuverType.TURN_RIGHT
        ) to 3000L,

        // Bước 6: Đang chờ hướng dẫn sau khi rẽ
        NavigationStep.waiting() to 2500L,

        // Bước 7: Hướng dẫn mới - Rẽ trái vào Phố đi bộ
        NavigationStep(
            action = "Rẽ trái",
            distanceText = "120 m",
            streetName = "Phố đi bộ",
            distanceMeters = 120,
            maneuver = ManeuverType.TURN_LEFT
        ) to 3000L,

        // Bước 8: Đã đến nơi
        NavigationStep.arrived("Nhà hát Thành phố") to 4000L
    )

    /// Bắt đầu phát kịch bản hành trình mẫu lên thông báo đồng hồ
    fun startSimulation() {
        stopSimulation()
        NavigationRepository.setSimulatorRunning(true)

        simulationJob = scope.launch {
            for ((step, delayMs) in sampleRoute) {
                notificationManager.showOrUpdateNotification(step)
                NavigationRepository.updateStep(step)
                delay(delayMs)
            }

            // Kết thúc hành trình, tự động dọn dẹp thông báo
            notificationManager.dismissNotification()
            NavigationRepository.setState(NavigationState.COMPLETED)
            NavigationRepository.setSimulatorRunning(false)
        }
    }

    /// Tạm dừng hoặc kết thúc giả lập và gỡ bỏ thông báo ngay tức thì
    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        notificationManager.dismissNotification()
        NavigationRepository.setState(NavigationState.IDLE)
        NavigationRepository.setSimulatorRunning(false)
    }
}
