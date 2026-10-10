package com.dyno.navi_noti.data.model

import androidx.annotation.StringRes
import com.dyno.navi_noti.R

/// Kiểu trình bày notification trên đồng hồ; mỗi dòng đồng hồ hiển thị được số ký tự khác nhau
enum class NotificationStyle(
    @get:StringRes val titleRes: Int,
    @get:StringRes val descRes: Int
) {
    /// Tiêu đề: thao tác · Nội dung: khoảng cách + thao tác + tên đường
    DETAILED(R.string.style_detailed, R.string.style_detailed_desc),

    /// Tiêu đề: mũi tên + thao tác · Nội dung: khoảng cách + tên đường
    ARROW(R.string.style_arrow, R.string.style_arrow_desc),

    /// Tiêu đề: mũi tên + khoảng cách · Nội dung: thao tác + tên đường (vừa màn hình band nhỏ)
    COMPACT(R.string.style_compact, R.string.style_compact_desc),

    /// Người dùng tự đặt mẫu tiêu đề/nội dung bằng các biến {arrow} {action} {distance} {street}
    CUSTOM(R.string.style_custom, R.string.style_custom_desc);

    companion object {
        fun fromName(name: String?): NotificationStyle =
            entries.firstOrNull { it.name == name } ?: DETAILED
    }
}

/// Mẫu tuỳ chỉnh cho kiểu [NotificationStyle.CUSTOM]
data class NotificationTemplate(
    val title: String = DEFAULT_TITLE,
    val content: String = DEFAULT_CONTENT
) {
    companion object {
        const val DEFAULT_TITLE = "{arrow} {action}"
        const val DEFAULT_CONTENT = "{distance} · {street}"
        val TOKENS = listOf("{arrow}", "{action}", "{distance}", "{street}")
    }
}

/// Tiêu đề và nội dung đã định dạng của một notification điều hướng
data class NotificationText(val title: String, val content: String)
