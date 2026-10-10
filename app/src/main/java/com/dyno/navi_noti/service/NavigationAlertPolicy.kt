package com.dyno.navi_noti.service

import android.os.SystemClock
import com.dyno.navi_noti.data.model.AlertSettings
import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.data.model.NavigationStep

/// Quyết định cập nhật nào được rung. Mỗi mốc chỉ xảy ra một lần cho mỗi chỗ rẽ;
/// mốc bị tắt hoặc rơi vào khoảng nghỉ vẫn được đánh dấu để không rung bù về sau.
internal class NavigationAlertPolicy(
    private val settings: () -> AlertSettings = { AlertSettings() },
    private val clock: () -> Long = SystemClock::elapsedRealtime
) {

    private enum class Milestone { FAR, NEAR, TURN_POINT, ARRIVAL }

    private var lastManeuver: ManeuverType? = null
    private var lastDistanceMeters: Int? = null
    private var alerted100Meters = false
    private var alerted30Meters = false
    private var alertedTurnPoint = false
    private var alertedArrival = false
    private var lastAlertAt: Long? = null

    fun shouldAlert(step: NavigationStep): Boolean {
        val previousManeuver = lastManeuver
        val current = settings()
        val now = clock()
        val coolingDown = lastAlertAt?.let { now - it < current.cooldownSeconds * 1000L } == true

        // Một cập nhật có thể chạm nhiều mốc (vừa qua chỗ rẽ cũ vừa gần chỗ rẽ mới): chỉ rung một lần
        val alert = nextMilestones(step).any { milestone ->
            milestone.isEnabled(current) &&
                (!current.turnsOnly || milestone.isRelevant(step, previousManeuver)) &&
                // Khoảng nghỉ chỉ chặn mốc báo trước; mốc "rẽ ngay" và đến nơi luôn được rung
                (!coolingDown || milestone == Milestone.NEAR || milestone == Milestone.ARRIVAL)
        }
        if (alert) lastAlertAt = now
        return alert
    }

    private fun Milestone.isEnabled(settings: AlertSettings) = when (this) {
        Milestone.FAR -> settings.alertFar
        Milestone.NEAR -> settings.alertNear
        Milestone.TURN_POINT -> settings.alertTurnPoint
        Milestone.ARRIVAL -> settings.alertArrival
    }

    private fun Milestone.isRelevant(step: NavigationStep, previousManeuver: ManeuverType?) = when (this) {
        Milestone.FAR, Milestone.NEAR -> step.maneuver.isTurn || step.maneuver == ManeuverType.DESTINATION
        Milestone.TURN_POINT -> previousManeuver?.isTurn == true
        Milestone.ARRIVAL -> true
    }

    /// Cập nhật trạng thái theo bước mới và trả về các mốc vừa chạm tới, ưu tiên mốc quan trọng trước
    private fun nextMilestones(step: NavigationStep): List<Milestone> {
        if (step.isDestination) {
            if (alertedArrival) return emptyList()
            alertedArrival = true
            return listOf(Milestone.ARRIVAL)
        }

        if (step.isWaiting) {
            val reachedTurn = lastManeuver != null && !alertedTurnPoint
            lastManeuver = null
            lastDistanceMeters = null
            alerted100Meters = false
            alerted30Meters = false
            alertedTurnPoint = true
            return if (reachedTurn) listOf(Milestone.TURN_POINT) else emptyList()
        }

        val maneuver = step.maneuver
        val distance = step.distanceMeters
        var turnPoint: Milestone? = null

        if (lastManeuver != null && maneuver != lastManeuver) {
            val reachedTurnPoint = alerted100Meters ||
                alerted30Meters ||
                (lastDistanceMeters?.let { it <= TURN_DISTANCE_METERS } == true)
            if (reachedTurnPoint) turnPoint = Milestone.TURN_POINT
            lastDistanceMeters = null
            alerted100Meters = false
            alerted30Meters = false
            alertedTurnPoint = false
        }
        lastManeuver = maneuver

        val previousDistance = lastDistanceMeters
        if (distance != null) lastDistanceMeters = distance
        val milestone = distance?.let { distanceMilestone(it, previousDistance) }
        return listOfNotNull(milestone, turnPoint)
    }

    /// Dự đoán theo tốc độ: nếu lần cập nhật kế tiếp sẽ vượt qua mốc thì báo ngay bây giờ,
    /// để xe chạy nhanh (Maps nhảy từ 60 m xuống 0 m) vẫn được rung trước khi tới chỗ rẽ
    private fun distanceMilestone(distance: Int, previousDistance: Int?): Milestone? {
        val step = previousDistance?.let { (it - distance).coerceIn(0, MAX_PREDICTED_STEP_METERS) } ?: 0
        val projected = distance - step
        if (!alerted30Meters && (distance <= TURN_DISTANCE_METERS || projected < TURN_DISTANCE_METERS)) {
            // Gộp: nhảy thẳng xuống gần chỗ rẽ thì chỉ rung một lần
            alerted30Meters = true
            alerted100Meters = true
            return Milestone.NEAR
        }
        if (!alerted100Meters && (distance <= NEAR_TURN_DISTANCE_METERS || projected < NEAR_TURN_DISTANCE_METERS)) {
            alerted100Meters = true
            return Milestone.FAR
        }
        return null
    }

    fun reset() {
        lastManeuver = null
        lastDistanceMeters = null
        alerted100Meters = false
        alerted30Meters = false
        alertedTurnPoint = false
        alertedArrival = false
        lastAlertAt = null
    }

    private companion object {
        const val NEAR_TURN_DISTANCE_METERS = 100
        const val TURN_DISTANCE_METERS = 30
        /// Giới hạn quãng dự đoán để lần cập nhật thưa (sau khi tính lại tuyến) không báo quá sớm
        const val MAX_PREDICTED_STEP_METERS = 60
    }
}
