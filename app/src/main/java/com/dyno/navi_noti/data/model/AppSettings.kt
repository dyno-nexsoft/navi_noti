package com.dyno.navi_noti.data.model

import androidx.annotation.StringRes
import com.dyno.navi_noti.R

/// Chế độ hiển thị giao diện của ứng dụng
enum class ThemeMode(@get:StringRes val titleRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}

/// Ngôn ngữ giao diện và định dạng thông báo
enum class AppLanguage(val code: String, @get:StringRes val titleRes: Int) {
    SYSTEM("", R.string.lang_system),
    VI("vi", R.string.lang_vi),
    EN("en", R.string.lang_en);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
        }
    }
}
