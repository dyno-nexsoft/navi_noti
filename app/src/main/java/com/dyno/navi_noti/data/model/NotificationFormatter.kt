package com.dyno.navi_noti.data.model

import android.content.Context

/// Định dạng bước điều hướng theo kiểu notification người dùng chọn.
/// Dùng chung cho notification thật và phần xem trước trong ứng dụng để hai nơi luôn khớp nhau.
object NotificationFormatter {

    fun format(
        step: NavigationStep,
        context: Context,
        style: NotificationStyle,
        template: NotificationTemplate = NotificationTemplate()
    ): NotificationText {
        val values = TemplateValues(
            arrow = step.maneuver.notificationArrow.orEmpty(),
            action = step.formatTitle(context),
            distance = step.distanceText.orEmpty(),
            street = step.streetName.orEmpty()
        )
        val detailed = NotificationText(values.action, step.formatContent(context))
        // Trạng thái chờ/đến nơi không có khoảng cách và tên đường; giữ câu mô tả đầy đủ
        val isStatus = step.isWaiting || step.isDestination

        return when (style) {
            NotificationStyle.DETAILED -> detailed
            NotificationStyle.ARROW -> NotificationText(
                title = join(" ", values.arrow, values.action),
                content = if (isStatus) detailed.content
                else join(" · ", values.distance, values.street).ifBlank { detailed.content }
            )
            NotificationStyle.COMPACT -> NotificationText(
                title = if (isStatus) join(" ", values.arrow, values.action)
                else join(" ", values.arrow, values.distance).ifBlank { values.action },
                content = if (isStatus) detailed.content else join(" · ", values.action, values.street)
            )
            NotificationStyle.CUSTOM -> NotificationText(
                title = render(template.title, values).ifBlank { detailed.title },
                content = render(template.content, values).ifBlank { detailed.content }
            )
        }
    }

    /// Thay biến trong mẫu và dọn các dấu phân cách bị thừa khi biến rỗng
    fun render(template: String, values: TemplateValues): String {
        val filled = template
            .replace("{arrow}", values.arrow)
            .replace("{action}", values.action)
            .replace("{distance}", values.distance)
            .replace("{street}", values.street)
        return filled
            .replace(Regex("""\s*([·•|,:])(?:\s*[·•|,:])+"""), " $1")
            .replace(Regex("""\s+"""), " ")
            .trim(' ', '·', '•', '|', ',', ':')
    }

    private fun join(separator: String, vararg parts: String) =
        parts.filter(String::isNotBlank).joinToString(separator)

    data class TemplateValues(
        val arrow: String,
        val action: String,
        val distance: String,
        val street: String
    )
}
