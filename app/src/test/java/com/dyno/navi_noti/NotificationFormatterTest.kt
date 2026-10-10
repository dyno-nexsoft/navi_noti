package com.dyno.navi_noti

import com.dyno.navi_noti.data.model.NotificationFormatter
import com.dyno.navi_noti.data.model.NotificationFormatter.TemplateValues
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationFormatterTest {

    private val full = TemplateValues(arrow = "←", action = "Rẽ trái", distance = "200 m", street = "Nguyễn Huệ")

    @Test
    fun rendersAllVariables() {
        assertEquals("← Rẽ trái", NotificationFormatter.render("{arrow} {action}", full))
        assertEquals("200 m · Nguyễn Huệ", NotificationFormatter.render("{distance} · {street}", full))
    }

    @Test
    fun dropsDanglingSeparatorsWhenValuesMissing() {
        val noStreet = full.copy(street = "")
        assertEquals("200 m", NotificationFormatter.render("{distance} · {street}", noStreet))

        val onlyAction = full.copy(arrow = "", distance = "", street = "")
        assertEquals("Rẽ trái", NotificationFormatter.render("{arrow} {distance} · {action} · {street}", onlyAction))
    }
}
