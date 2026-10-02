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
    fun parse(
        title: String?,
        text: String?,
        subText: String? = null,
        additionalTexts: List<String> = emptyList()
    ): NavigationStep? {
        val rawTitle = title?.trim() ?: ""
        val rawText = text?.trim() ?: ""
        val rawSubText = subText?.trim() ?: ""
        val rawAdditionalTexts = additionalTexts.map(String::trim).filter(String::isNotEmpty)
        if (rawTitle.isEmpty() && rawText.isEmpty() && rawSubText.isEmpty() &&
            rawAdditionalTexts.isEmpty()
        ) return null

        val candidates = listOf(rawTitle, rawText, rawSubText) + rawAdditionalTexts
        val combined = candidates.joinToString(" ").lowercase(Locale.ROOT)
        if (isArrival(combined)) {
            return NavigationStep.arrived(extractDestination(rawTitle, rawText, rawSubText))
        }

        val distanceInfo = extractDistance(candidates)
        val actionAndStreet = extractActionAndStreet(candidates, distanceInfo?.rawMatch)

        val distanceMeters = distanceInfo?.meters
        val isApproaching = distanceMeters != null && distanceMeters <= APPROACHING_DISTANCE_METERS

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
               text.contains("đã tới nơi") ||
               Regex("""\b(arrived|you've arrived|you arrived|destination reached)\b""")
                   .containsMatchIn(text)
    }

    /// Trích xuất tên điểm đến nếu có trong thông báo hoàn thành
    private fun extractDestination(title: String, text: String, subText: String): String? {
        val full = "$title $text $subText"
        val regex = Regex("""(?:đến|đích|tại|at)\s+(.+)$""", RegexOption.IGNORE_CASE)
        return regex.find(full)?.groupValues?.get(1)?.trim()
    }

    /// Trích xuất khoảng cách hiển thị và quy đổi sang mét để phát hiện mốc quan trọng
    private fun extractDistance(candidates: List<String>): DistanceResult? {
        val match = candidates.firstNotNullOfOrNull(distanceRegex::find) ?: return null
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
    private fun extractActionAndStreet(candidates: List<String>, distanceStr: String?): ActionStreetResult {
        val cleanedCandidates = candidates.map { candidate ->
            distanceStr?.let { candidate.replace(it, "") }?.trim() ?: candidate.trim()
        }
        val parsedCandidates = cleanedCandidates.mapNotNull(::parseActionCandidate)
        val explicitManeuver = parsedCandidates.firstOrNull {
            it.maneuver in setOf(
                ManeuverType.TURN_LEFT,
                ManeuverType.TURN_RIGHT,
                ManeuverType.ROUNDABOUT
            )
        }
        if (explicitManeuver != null) {
            val street = explicitManeuver.result.streetName
                ?: cleanedCandidates.firstNotNullOfOrNull { candidate ->
                    if (ManeuverType.fromText(candidate) != ManeuverType.UNKNOWN) {
                        null
                    } else {
                        candidate.trim(' ', '-', '·', ':').takeIf(String::isNotBlank)
                    }
                }
            return explicitManeuver.result.copy(streetName = street)
        }

        val recognizedManeuver = parsedCandidates.firstOrNull { it.maneuver != ManeuverType.UNKNOWN }
        if (recognizedManeuver != null) return recognizedManeuver.result

        val cleanTitle = cleanedCandidates.firstOrNull().orEmpty()
        val cleanText = cleanedCandidates.getOrNull(1).orEmpty()
        return ActionStreetResult(
            action = cleanTitle.ifBlank { cleanText.ifBlank { "Tiếp tục" } },
            streetName = cleanText.ifBlank { null }
        )
    }

    private fun parseActionCandidate(candidate: String): ParsedAction? {
        if (candidate.isBlank()) return null

        val roundaboutMatch = roundaboutRegex.find(candidate)
        if (roundaboutMatch != null) {
            val action = roundaboutMatch.groupValues[1].trim()
            val street = roundaboutMatch.groupValues.getOrNull(2)?.trim(' ', '-', '·', ':')
            return ParsedAction(
                maneuver = ManeuverType.ROUNDABOUT,
                result = ActionStreetResult(action, street?.ifBlank { null })
            )
        }

        for ((prefix, simplifiedAction) in actionPrefixes) {
            val index = candidate.indexOf(prefix, ignoreCase = true)
            if (index != -1) {
                val street = candidate.substring(index + prefix.length).trim(' ', '-', '·', ':')
                val maneuver = ManeuverType.fromText(simplifiedAction)
                return ParsedAction(
                    maneuver = maneuver,
                    result = ActionStreetResult(simplifiedAction, street.ifBlank { null })
                )
            }
        }

        val maneuver = ManeuverType.fromText(candidate)
        if (maneuver == ManeuverType.UNKNOWN) return null
        return ParsedAction(
            maneuver = maneuver,
            result = ActionStreetResult(maneuver.defaultActionName, null)
        )
    }

    private data class ParsedAction(val maneuver: ManeuverType, val result: ActionStreetResult)
    private data class DistanceResult(val displayText: String, val meters: Int, val rawMatch: String)
    private data class ActionStreetResult(val action: String, val streetName: String?)

    private const val APPROACHING_DISTANCE_METERS = 100
}
