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
        assertEquals("Đường Nguyễn Huệ", step?.formattedNotificationTitle)
        assertEquals("Còn 250 m · Rẽ trái", step?.formattedContent)
    }

    @Test
    fun parse_turnRightWithoutStreet() {
        val step = GoogleMapsNotificationParser.parse(
            title = "50 m",
            text = "Rẽ phải"
        )
        assertNotNull(step)
        assertEquals("Rẽ phải", step?.action)
        assertEquals("Còn 50 m · Rẽ phải", step?.formattedContent)
    }

    @Test
    fun parse_approachingTurnPoint() {
        val step = GoogleMapsNotificationParser.parse(
            title = "20 m",
            text = "Rẽ phải vào Đường Trần Hưng Đạo"
        )
        assertNotNull(step)
        assertTrue(step!!.isApproaching)
        assertEquals("→", step.maneuver.notificationArrow)
        assertEquals("Đường Trần Hưng Đạo", step.formattedNotificationTitle)
        assertEquals("Còn 20 m · Rẽ phải", step.formattedContent)
    }

    @Test
    fun parse_prefersUpcomingTurnOverStraightTitle() {
        val step = GoogleMapsNotificationParser.parse(
            title = "Đi thẳng",
            text = "30 m · P. Ngọc Hà",
            additionalTexts = listOf("Rẽ phải")
        )

        assertNotNull(step)
        assertEquals(ManeuverType.TURN_RIGHT, step?.maneuver)
        assertTrue(step!!.isApproaching)
        assertEquals("P. Ngọc Hà", step.streetName)
    }

    @Test
    fun parse_unknownManeuverDoesNotInventStraightArrow() {
        val step = GoogleMapsNotificationParser.parse(
            title = "Điện Biên Phủ",
            text = "40 m"
        )

        assertNotNull(step)
        assertEquals(ManeuverType.UNKNOWN, step?.maneuver)
        assertEquals(null, step?.maneuver?.notificationArrow)
        assertEquals("Điện Biên Phủ", step?.formattedNotificationTitle)
        assertEquals("Còn 40 m", step?.formattedContent)
    }

    @Test
    fun parse_marksStepsWithin100MetersAsApproaching() {
        val step = GoogleMapsNotificationParser.parse(
            title = "In 90 m",
            text = "Turn left onto Market Street"
        )

        assertNotNull(step)
        assertTrue(step!!.isApproaching)
        assertEquals(ManeuverType.TURN_LEFT, step.maneuver)
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
        assertEquals("Vòng xoay Dân Chủ", step?.formattedNotificationTitle)
        assertEquals("Còn 300 m · Đi theo lối ra thứ 2", step?.formattedContent)
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
