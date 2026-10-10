package com.dyno.navi_noti.service.parser

import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.data.model.NavigationStep
import java.util.Locale

/// Bộ phân tích dữ liệu thông báo điều hướng từ Google Maps (hỗ trợ cả tiếng Việt và tiếng Anh)
object GoogleMapsNotificationParser {

    // (?!\p{L}) để "1 min" (thời gian trong subText) không bị hiểu là "1 m"
    private val distanceRegex = Regex("""(?:in\s+)?(\d+(?:[.,]\d+)?)\s*(km|mét|m|ft|mi)(?!\p{L})""", RegexOption.IGNORE_CASE)

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
        "Đi chếch sang trái vào" to "Rẽ chếch sang trái",
        "Đi chếch sang phải vào" to "Rẽ chếch sang phải",
        "Rẽ gắt sang trái vào" to "Rẽ gắt sang trái",
        "Rẽ gắt sang phải vào" to "Rẽ gắt sang phải",
        "Quay đầu tại" to "Quay đầu",
        "Quay đầu vào" to "Quay đầu",
        "Giữ bên trái vào" to "Giữ bên trái",
        "Giữ bên phải vào" to "Giữ bên phải",
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
        "Slight left onto" to "Slight left",
        "Slight right onto" to "Slight right",
        "Sharp left onto" to "Sharp left",
        "Sharp right onto" to "Sharp right",
        "Make a U-turn onto" to "U-turn",
        "Make a U-turn at" to "U-turn",
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

    private val towardPrefixes = listOf("về phía", "hướng về", "toward", "towards")

    /// Phân tích title và text từ thông báo để trích xuất bước điều hướng chuẩn cho đồng hồ.
    /// [iconManeuver] là hướng đọc được từ icon mũi tên của Maps; dùng khi text không ghi rõ hướng rẽ
    /// (Live Update của Maps thường chỉ có khoảng cách + tên đường, hướng nằm ở icon).
    fun parse(
        title: String?,
        text: String?,
        subText: String? = null,
        additionalTexts: List<String> = emptyList(),
        iconManeuver: ManeuverType? = null
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

        // subText là dòng ETA ("3 min · 900 m · 11:36 PM"): quãng đường cả chuyến, không phải tới điểm rẽ
        val distanceInfo = extractDistance(candidates - rawSubText)
        val textResult = extractActionAndStreet(candidates, distanceInfo?.rawMatch)
        val actionAndStreet = applyIconManeuver(textResult, candidates, distanceInfo?.rawMatch, iconManeuver)

        val distanceMeters = distanceInfo?.meters
        val isApproaching = distanceMeters != null && distanceMeters <= APPROACHING_DISTANCE_METERS

        return NavigationStep(
            action = actionAndStreet.action,
            distanceText = distanceInfo?.displayText,
            streetName = actionAndStreet.streetName?.let(::stripToward),
            distanceMeters = distanceMeters,
            maneuver = actionAndStreet.maneuver ?: ManeuverType.fromText(actionAndStreet.action),
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
        val explicitManeuver = parsedCandidates.firstOrNull { it.maneuver.isTurn }
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

    /// Icon chỉ thay thế khi text không có hướng rẽ rõ ràng: text rẽ > icon > text đi thẳng/không rõ
    private fun applyIconManeuver(
        textResult: ActionStreetResult,
        candidates: List<String>,
        distanceStr: String?,
        iconManeuver: ManeuverType?
    ): ActionStreetResult {
        if (iconManeuver == null || iconManeuver == ManeuverType.UNKNOWN) return textResult
        val textManeuver = ManeuverType.fromText(textResult.action)
        val canOverride = when (textManeuver) {
            ManeuverType.UNKNOWN -> true
            ManeuverType.STRAIGHT -> iconManeuver.isTurn
            else -> false
        }
        if (!canOverride) return textResult

        val street = if (textManeuver == ManeuverType.UNKNOWN) {
            // Text không có thao tác thì dòng đầu tiên còn lại chính là tên đường sắp đi vào
            candidates.asSequence()
                .map { candidate -> distanceStr?.let { candidate.replace(it, "") } ?: candidate }
                .map { it.trim(' ', '-', '·', ':') }
                .firstOrNull { it.isNotBlank() && ManeuverType.fromText(it) == ManeuverType.UNKNOWN }
        } else {
            textResult.streetName
        }
        return ActionStreetResult(iconManeuver.defaultActionName, street, iconManeuver)
    }

    private fun stripToward(street: String): String? {
        val prefix = towardPrefixes.firstOrNull { street.startsWith(it, ignoreCase = true) }
        val cleaned = prefix?.let { street.substring(it.length) } ?: street
        return cleaned.trim(' ', '-', '·', ':').ifBlank { null }
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
    private data class ActionStreetResult(
        val action: String,
        val streetName: String?,
        val maneuver: ManeuverType? = null
    )

    private const val APPROACHING_DISTANCE_METERS = 100
}
