package com.dyno.navi_noti.service

import com.dyno.navi_noti.data.model.NavigationStep

internal class NavigationAlertPolicy {

    private var lastManeuver: String? = null
    private var lastDistanceMeters: Int? = null
    private var alertedNearTurn = false
    private var alertedAtTurn = false
    private var alertedArrival = false

    fun shouldAlert(step: NavigationStep): Boolean {
        if (step.isDestination) {
            if (alertedArrival) return false
            alertedArrival = true
            return true
        }

        val maneuver = step.maneuver.name
        val distance = step.distanceMeters
        val isNewManeuver = lastManeuver != null && maneuver != lastManeuver
        val isNextSameManeuver = !isNewManeuver &&
            alertedAtTurn &&
            lastDistanceMeters != null &&
            lastDistanceMeters!! <= TURN_DISTANCE_METERS &&
            distance != null &&
            distance > NEAR_TURN_DISTANCE_METERS

        if (isNewManeuver || isNextSameManeuver) {
            alertedNearTurn = false
            alertedAtTurn = false
        }

        lastManeuver = maneuver
        if (distance != null) lastDistanceMeters = distance

        if (distance != null && distance <= TURN_DISTANCE_METERS && !alertedAtTurn) {
            alertedNearTurn = true
            alertedAtTurn = true
            return true
        }

        if (distance != null && distance <= NEAR_TURN_DISTANCE_METERS && !alertedNearTurn) {
            alertedNearTurn = true
            return true
        }

        return false
    }

    fun reset() {
        lastManeuver = null
        lastDistanceMeters = null
        alertedNearTurn = false
        alertedAtTurn = false
        alertedArrival = false
    }

    private companion object {
        const val NEAR_TURN_DISTANCE_METERS = 100
        const val TURN_DISTANCE_METERS = 35
    }
}
