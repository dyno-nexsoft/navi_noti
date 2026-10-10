package com.dyno.navi_noti.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.dyno.navi_noti.data.model.AppLanguage
import com.dyno.navi_noti.data.model.NotificationStyle
import com.dyno.navi_noti.data.model.NotificationTemplate
import com.dyno.navi_noti.data.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/// Quản lý cấu hình giao diện và ngôn ngữ được lưu trữ trên thiết bị
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(loadAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _notificationStyle = MutableStateFlow(
        NotificationStyle.fromName(prefs.getString(KEY_NOTIFICATION_STYLE, null))
    )
    val notificationStyle: StateFlow<NotificationStyle> = _notificationStyle.asStateFlow()

    private val _notificationTemplate = MutableStateFlow(
        NotificationTemplate(
            title = prefs.getString(KEY_TEMPLATE_TITLE, null) ?: NotificationTemplate.DEFAULT_TITLE,
            content = prefs.getString(KEY_TEMPLATE_CONTENT, null) ?: NotificationTemplate.DEFAULT_CONTENT
        )
    )
    val notificationTemplate: StateFlow<NotificationTemplate> = _notificationTemplate.asStateFlow()

    /// Cập nhật và lưu lại kiểu trình bày notification trên đồng hồ
    fun setNotificationStyle(style: NotificationStyle) {
        prefs.edit().putString(KEY_NOTIFICATION_STYLE, style.name).apply()
        _notificationStyle.value = style
    }

    /// Cập nhật và lưu lại mẫu notification tuỳ chỉnh
    fun setNotificationTemplate(template: NotificationTemplate) {
        prefs.edit()
            .putString(KEY_TEMPLATE_TITLE, template.title)
            .putString(KEY_TEMPLATE_CONTENT, template.content)
            .apply()
        _notificationTemplate.value = template
    }

    /// Cập nhật và lưu lại chế độ giao diện
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    /// Cập nhật và lưu lại ngôn ngữ ứng dụng
    fun setAppLanguage(lang: AppLanguage) {
        prefs.edit().putString(KEY_APP_LANGUAGE, lang.code).apply()
        _appLanguage.value = lang
    }

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return runCatching { ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name) }.getOrDefault(ThemeMode.SYSTEM)
    }

    private fun loadAppLanguage(): AppLanguage {
        val code = prefs.getString(KEY_APP_LANGUAGE, "") ?: ""
        return AppLanguage.fromCode(code)
    }

    companion object {
        private const val PREFS_NAME = "navi_noti_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_NOTIFICATION_STYLE = "notification_style"
        private const val KEY_TEMPLATE_TITLE = "notification_template_title"
        private const val KEY_TEMPLATE_CONTENT = "notification_template_content"
    }
}
