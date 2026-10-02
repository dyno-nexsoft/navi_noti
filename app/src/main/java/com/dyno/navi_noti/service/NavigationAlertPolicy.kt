package com.dyno.navi_noti.service

import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.data.model.NavigationStep

internal class NavigationAlertPolicy {

    private var lastManeuver: ManeuverType? = null
    private var lastDistanceMeters: Int? = null
    private var alerted100Meters = false
    private var alerted30Meters = false
    private var alertedTurnPoint = false
    private var alertedArrival = false

    fun shouldAlert(step: NavigationStep): Boolean {
        if (step.isDestination) {
            if (alertedArrival) return false
            alertedArrival = true
            return true
        }

        if (step.isWaiting) {
            val shouldAlertAtTurn = lastManeuver != null && !alertedTurnPoint
            lastManeuver = null
            lastDistanceMeters = null
            alerted100Meters = false
            alerted30Meters = false
            alertedTurnPoint = true
            return shouldAlertAtTurn
        }

        val maneuver = step.maneuver
        val distance = step.distanceMeters

        if (lastManeuver != null && maneuver != lastManeuver) {
            val reachedTurnPoint = alerted100Meters ||
                alerted30Meters ||
                (lastDistanceMeters != null && lastDistanceMeters!! <= TURN_DISTANCE_METERS)
            lastManeuver = maneuver
            lastDistanceMeters = distance
            alerted100Meters = false
            alerted30Meters = false
            alertedTurnPoint = false
            if (reachedTurnPoint) return true
        } else if (lastManeuver == null) {
            lastManeuver = maneuver
            alertedTurnPoint = false
        }

        if (distance != null) lastDistanceMeters = distance

        if (distance != null && distance <= NEAR_TURN_DISTANCE_METERS && !alerted100Meters) {
            alerted100Meters = true
            return true
        }

        if (distance != null && distance <= TURN_DISTANCE_METERS && !alerted30Meters) {
            alerted30Meters = true
            return true
        }

        return false
    }

    fun reset() {
        lastManeuver = null
        lastDistanceMeters = null
        alerted100Meters = false
        alerted30Meters = false
        alertedTurnPoint = false
        alertedArrival = false
    }

    private companion object {
        const val NEAR_TURN_DISTANCE_METERS = 100
        const val TURN_DISTANCE_METERS = 30
    }
}
