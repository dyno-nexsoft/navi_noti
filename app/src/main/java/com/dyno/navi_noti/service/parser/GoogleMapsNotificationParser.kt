package com.dyno.navi_noti.service.parser

import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.data.model.NavigationStep
import java.util.Locale

/// Bộ phân tích dữ liệu thông báo điều hướng từ Google Maps (hỗ trợ cả tiếng Việt và tiếng Anh)
object GoogleMapsNotificationParser {

    private val distanceRegex = Regex("""(?:in\s+)?(\d+(?:[.,]\d+)?)\s*(m|km|mét|km/h|ft|mi)""", RegexOption.IGNORE_CASE)

    private val roundaboutRegex = Regex(
        """(Đi theo lối ra(?:\s+thứ\s+\d+)?|Take exit(?:\s+\d+)?|Take the\s+\w+\s+exit)(?:\s+(?:vào|onto)\s+(.+))?""",
        RegexOption.IGNORE_CASE
    )

    private val actionPrefixes = listOf(
        // Tiếng Việt
        "Rẽ trái vào" to "Rẽ trái",
        "Rẽ phải vào" to "Rẽ phải",
        "Rẽ chếch sang trái vào" to "Rẽ chếch sang trái",
        "Rẽ chếch sang phải vào" to "Rẽ chếch sang phải",
        "Đi thẳng về phía" to "Đi thẳng",
        "Đi tiếp trên" to "Đi thẳng",
        "Đi thẳng trên" to "Đi thẳng",
        "Đi về hướng" to "Đi thẳng",
        "Nhập làn vào" to "Nhập làn",
        "Đã đến nơi" to "Đã đến nơi",
        "Bạn đã đến" to "Đã đến nơi",
        // Tiếng Anh
        "Turn left onto" to "Turn left",
        "Turn right onto" to "Turn right",
        "Turn left" to "Turn left",
        "Turn right" to "Turn right",
        "Keep left onto" to "Keep left",
        "Keep right onto" to "Keep right",
        "Continue onto" to "Continue straight",
        "Continue on" to "Continue straight",
        "Head south on" to "Continue straight",
        "Head north on" to "Continue straight",
        "Head east on" to "Continue straight",
        "Head west on" to "Continue straight",
        "Merge onto" to "Merge",
        "You have arrived" to "Arrived",
        "Arrived at" to "Arrived"
    )

    /// Phân tích title và text từ thông báo để trích xuất bước điều hướng chuẩn cho đồng hồ
    fun parse(title: String?, text: String?, subText: String? = null): NavigationStep? {
        val rawTitle = title?.trim() ?: ""
        val rawText = text?.trim() ?: ""
        if (rawTitle.isEmpty() && rawText.isEmpty()) return null

        val combined = "$rawTitle $rawText".lowercase(Locale.ROOT)
        if (isArrival(combined)) {
            return NavigationStep.arrived(extractDestination(rawTitle, rawText))
        }

        val distanceInfo = extractDistance(rawTitle, rawText)
        val actionAndStreet = extractActionAndStreet(rawTitle, rawText, distanceInfo?.rawMatch)

        val distanceMeters = distanceInfo?.meters
        val isApproaching = distanceMeters != null && distanceMeters <= 35

        return NavigationStep(
            action = actionAndStreet.action,
            distanceText = distanceInfo?.displayText,
            streetName = actionAndStreet.streetName,
            distanceMeters = distanceMeters,
            maneuver = ManeuverType.fromText(actionAndStreet.action),
            isApproaching = isApproaching
        )
    }

    /// Kiểm tra xem thông báo có biểu thị sự kiện đã đến đích hay không
    private fun isArrival(text: String): Boolean {
        return text.contains("đã đến nơi") || 
               text.contains("bạn đã đến") || 
               text.contains("đến đích") ||
               text.contains("you have arrived") ||
               text.contains("arrived at")
    }

    /// Trích xuất tên điểm đến nếu có trong thông báo hoàn thành
    private fun extractDestination(title: String, text: String): String? {
        val full = "$title $text"
        val regex = Regex("""(?:đến|đích|tại|at)\s+(.+)$""", RegexOption.IGNORE_CASE)
        return regex.find(full)?.groupValues?.get(1)?.trim()
    }

    /// Trích xuất khoảng cách hiển thị và quy đổi sang mét để phát hiện mốc quan trọng
    private fun extractDistance(title: String, text: String): DistanceResult? {
        val match = distanceRegex.find(title) ?: distanceRegex.find(text) ?: return null
        val rawVal = match.groupValues[1].replace(',', '.')
        val unit = match.groupValues[2].lowercase(Locale.ROOT)
        val num = rawVal.toDoubleOrNull() ?: return null

        val meters = when (unit) {
            "km" -> (num * 1000).toInt()
            "mi" -> (num * 1609.34).toInt()
            "ft" -> (num * 0.3048).toInt()
            else -> num.toInt()
        }
        val displayText = "${match.groupValues[1]} $unit"
        return DistanceResult(displayText = displayText, meters = meters, rawMatch = match.value)
    }

    /// Bóc tách hành vi điều hướng cốt lõi và tên đường, tránh nhầm lẫn giữa tên đường cũ và mới
    private fun extractActionAndStreet(title: String, text: String, distanceStr: String?): ActionStreetResult {
        var cleanTitle = title
        var cleanText = text
        if (distanceStr != null) {
            cleanTitle = cleanTitle.replace(distanceStr, "").trim()
            cleanText = cleanText.replace(distanceStr, "").trim()
        }

        val candidate = if (cleanTitle.length > 3 && !cleanTitle.all { it.isDigit() || it == '.' || it == ',' }) {
            cleanTitle
        } else {
            cleanText
        }

        // Xử lý lối ra vòng xoay (hỗ trợ cả tiếng Việt và tiếng Anh)
        val roundaboutMatch = roundaboutRegex.find(candidate)
        if (roundaboutMatch != null) {
            val act = roundaboutMatch.groupValues[1].trim()
            val str = roundaboutMatch.groupValues.getOrNull(2)?.trim(' ', '-', '·', ':')
            return ActionStreetResult(
                action = act,
                streetName = str?.ifBlank { null }
            )
        }

        // Kiểm tra tiền tố hành động kèm tên đường
        for ((prefix, simplifiedAction) in actionPrefixes) {
            val idx = candidate.indexOf(prefix, ignoreCase = true)
            if (idx != -1) {
                val street = candidate.substring(idx + prefix.length).trim(' ', '-', '·', ':')
                return ActionStreetResult(
                    action = simplifiedAction,
                    streetName = street.ifBlank { null }
                )
            }
        }

        // Kiểm tra nếu candidate chính là một hành động đơn thuần
        val candidateManeuver = ManeuverType.fromText(candidate)
        if (candidateManeuver != ManeuverType.UNKNOWN) {
            val remaining = if (candidate == cleanTitle) cleanText else cleanTitle
            val remainingManeuver = ManeuverType.fromText(remaining)
            val street = if (remaining.isNotBlank() && remainingManeuver == ManeuverType.UNKNOWN) {
                remaining.trim(' ', '-', '·', ':')
            } else {
                null
            }
            return ActionStreetResult(
                action = candidateManeuver.defaultActionName,
                streetName = street?.ifBlank { null }
            )
        }

        return ActionStreetResult(
            action = cleanTitle.ifBlank { "Tiếp tục" },
            streetName = cleanText.ifBlank { null }
        )
    }

    private data class DistanceResult(val displayText: String, val meters: Int, val rawMatch: String)
    private data class ActionStreetResult(val action: String, val streetName: String?)
}
