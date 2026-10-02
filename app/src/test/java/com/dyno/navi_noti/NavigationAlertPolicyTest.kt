package com.dyno.navi_noti

import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.data.model.NavigationStep
import com.dyno.navi_noti.service.NavigationAlertPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationAlertPolicyTest {

    private val policy = NavigationAlertPolicy()

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
