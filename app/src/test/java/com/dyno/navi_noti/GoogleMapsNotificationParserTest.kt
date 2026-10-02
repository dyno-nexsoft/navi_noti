package com.dyno.navi_noti

import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.service.parser.GoogleMapsNotificationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleMapsNotificationParserTest {

    @Test
    fun parse_turnLeftWithStreetAndDistance() {
        val step = GoogleMapsNotificationParser.parse(
            title = "250 m",
            text = "Rẽ trái vào Đường Nguyễn Huệ"
        )
        assertNotNull(step)
        assertEquals("Rẽ trái", step?.action)
        assertEquals("Đường Nguyễn Huệ", step?.streetName)
        assertEquals("250 m", step?.distanceText)
        assertEquals("Rẽ trái", step?.formattedTitle)
        assertEquals("Còn 250 m · Đường Nguyễn Huệ", step?.formattedContent)
    }

    @Test
    fun parse_turnRightWithoutStreet() {
        val step = GoogleMapsNotificationParser.parse(
            title = "50 m",
            text = "Rẽ phải"
        )
        assertNotNull(step)
        assertEquals("Rẽ phải", step?.action)
        assertEquals("Còn 50 m", step?.formattedContent)
    }

    @Test
    fun parse_approachingTurnPoint() {
        val step = GoogleMapsNotificationParser.parse(
            title = "20 m",
            text = "Rẽ phải vào Đường Trần Hưng Đạo"
        )
        assertNotNull(step)
        assertTrue(step!!.isApproaching)
        assertEquals("Đang đến điểm rẽ · Đường Trần Hưng Đạo", step.formattedContent)
    }

    @Test
    fun parse_arrival() {
        val step = GoogleMapsNotificationParser.parse(
            title = "Bạn đã đến nơi",
            text = "Điểm đến của bạn ở bên phải"
        )
        assertNotNull(step)
        assertTrue(step!!.isDestination)
        assertEquals(ManeuverType.DESTINATION, step.maneuver)
        assertEquals("Đã đến nơi", step.formattedTitle)
    }

    @Test
    fun parse_roundaboutExit() {
        val step = GoogleMapsNotificationParser.parse(
            title = "300 m",
            text = "Đi theo lối ra thứ 2 vào Vòng xoay Dân Chủ"
        )
        assertNotNull(step)
        assertEquals("Đi theo lối ra thứ 2", step?.action)
        assertEquals(ManeuverType.ROUNDABOUT, step?.maneuver)
        assertEquals("Vòng xoay Dân Chủ", step?.streetName)
        assertEquals("Còn 300 m · Vòng xoay Dân Chủ", step?.formattedContent)
    }

    @Test
    fun parse_englishTurnLeftWithStreet() {
        val step = GoogleMapsNotificationParser.parse(
            title = "In 500 ft",
            text = "Turn left onto Market Street"
        )
        assertNotNull(step)
        assertEquals("Turn left", step?.action)
        assertEquals(ManeuverType.TURN_LEFT, step?.maneuver)
        assertEquals("Market Street", step?.streetName)
        assertEquals("500 ft", step?.distanceText)
    }

    @Test
    fun parse_englishArrival() {
        val step = GoogleMapsNotificationParser.parse(
            title = "You have arrived",
            text = "Your destination is on the right"
        )
        assertNotNull(step)
        assertTrue(step!!.isDestination)
        assertEquals(ManeuverType.DESTINATION, step.maneuver)
    }

    @Test
    fun parse_shortArrivalInText() {
        val step = GoogleMapsNotificationParser.parse(
            title = "Ho Chi Minh's Mausoleum",
            text = "Arrived"
        )

        assertNotNull(step)
        assertTrue(step!!.isDestination)
        assertEquals(ManeuverType.DESTINATION, step.maneuver)
    }

    @Test
    fun parse_arrivalInSubText() {
        val step = GoogleMapsNotificationParser.parse(
            title = "Ho Chi Minh's Mausoleum",
            text = null,
            subText = "Destination reached"
        )

        assertNotNull(step)
        assertTrue(step!!.isDestination)
    }
}

