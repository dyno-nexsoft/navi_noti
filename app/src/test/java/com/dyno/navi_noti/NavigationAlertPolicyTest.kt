package com.dyno.navi_noti

import com.dyno.navi_noti.data.model.AlertPreset
import com.dyno.navi_noti.data.model.AlertSettings
import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.data.model.NavigationStep
import com.dyno.navi_noti.service.NavigationAlertPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationAlertPolicyTest {

    private var now = 0L
    private var settings = AlertPreset.FULL.settings!!
    private val policy = NavigationAlertPolicy(settings = { settings }, clock = { now })

    @Test
    fun alertsAt100m30mAndWhenManeuverChanges() {
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 500)))
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 300)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 100)))
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 80)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 30)))
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 20)))
        assertTrue(policy.shouldAlert(step(ManeuverType.STRAIGHT, 300)))
        assertFalse(policy.shouldAlert(step(ManeuverType.STRAIGHT, 250)))
        assertTrue(policy.shouldAlert(step(ManeuverType.STRAIGHT, 100)))
        assertTrue(policy.shouldAlert(step(ManeuverType.STRAIGHT, 30)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 20)))
    }

    @Test
    fun waitingStepAlertsTurnPointWithoutAlertingNextManeuverTwice() {
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 100)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 30)))
        assertTrue(policy.shouldAlert(NavigationStep.waiting()))
        assertFalse(policy.shouldAlert(NavigationStep.waiting()))
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 120)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 100)))
    }

    @Test
    fun maneuverChangeAfter100mAlertSignalsTurnPoint() {
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 100)))
        assertTrue(policy.shouldAlert(step(ManeuverType.STRAIGHT, 200)))
    }

    @Test
    fun streetNameChangesDoNotRepeatAlerts() {
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 90, "First Street")))
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 80, "Second Street")))
    }

    @Test
    fun alertsAgainAtNearTurnForNextManeuver() {
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 90)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 90)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 30)))
    }

    @Test
    fun arrivalAlertsOnlyOnce() {
        val arrival = NavigationStep.arrived()

        assertTrue(policy.shouldAlert(arrival))
        assertFalse(policy.shouldAlert(arrival))
    }

    @Test
    fun balancedPresetSkipsStraightSegmentsAndTurnPoint() {
        settings = AlertPreset.BALANCED.settings!!.copy(cooldownSeconds = 0)

        assertFalse(policy.shouldAlert(step(ManeuverType.STRAIGHT, 100)))
        assertFalse(policy.shouldAlert(step(ManeuverType.STRAIGHT, 30)))
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 300)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 100)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 30)))
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 400)))
    }

    @Test
    fun cooldownSuppressesEarlyWarningButNotTurnNow() {
        settings = AlertSettings(cooldownSeconds = 8)

        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 100)))
        now += 3_000
        // Mốc 30 m quan trọng nhất nên không bị khoảng nghỉ chặn
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 30)))
        now += 3_000
        // Chỗ rẽ kế tiếp sát ngay: mốc báo trước bị gộp vào khoảng nghỉ
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 90)))
        now += 10_000
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 80)))
        assertTrue(policy.shouldAlert(NavigationStep.arrived()))
    }

    @Test
    fun fastUpdatesPredictTurnBeforeSkippingPast30m() {
        settings = AlertSettings(cooldownSeconds = 0)

        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 90)))
        // 90 → 55: lần sau sẽ xuống ~20 m, báo "rẽ ngay" luôn thay vì để lỡ
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 55)))
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 20)))
    }

    @Test
    fun straightStepAfterTurnDoesNotSwallowNextTurnWarning() {
        settings = AlertPreset.BALANCED.settings!!.copy(cooldownSeconds = 0)

        // Maps hiện "0 m · toward X" ngay sau khi rẽ, rồi mới tới chỗ rẽ kế tiếp
        assertFalse(policy.shouldAlert(step(ManeuverType.STRAIGHT, 0)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 80)))
    }

    @Test
    fun jumpingStraightBelow30mVibratesOnce() {
        settings = AlertSettings(cooldownSeconds = 0)

        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 20)))
        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_RIGHT, 10)))
    }

    @Test
    fun quietPresetOnlyVibratesNearTurn() {
        settings = AlertPreset.QUIET.settings!!.copy(cooldownSeconds = 0)

        assertFalse(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 100)))
        assertTrue(policy.shouldAlert(step(ManeuverType.TURN_LEFT, 30)))
    }

    private fun step(
        maneuver: ManeuverType,
        distance: Int,
        street: String? = "Test Street"
    ) = NavigationStep(
        action = maneuver.defaultActionName,
        streetName = street,
        distanceMeters = distance,
        maneuver = maneuver
    )
}
