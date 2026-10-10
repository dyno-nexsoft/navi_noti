package com.dyno.navi_noti.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.dyno.navi_noti.data.model.AlertPreset
import com.dyno.navi_noti.data.model.AlertSettings
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

    private val _showEta = MutableStateFlow(prefs.getBoolean(KEY_SHOW_ETA, true))
    val showEta: StateFlow<Boolean> = _showEta.asStateFlow()

    fun setShowEta(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_ETA, show).apply()
        _showEta.value = show
    }

    private val _alertPreset = MutableStateFlow(AlertPreset.fromName(prefs.getString(KEY_ALERT_PRESET, null)))
    val alertPreset: StateFlow<AlertPreset> = _alertPreset.asStateFlow()

    private val _customAlertSettings = MutableStateFlow(loadCustomAlertSettings())
    val customAlertSettings: StateFlow<AlertSettings> = _customAlertSettings.asStateFlow()

    /// Cấu hình rung đang áp dụng: theo mức soạn sẵn hoặc tuỳ chỉnh
    val alertSettings: AlertSettings
        get() = _alertPreset.value.settings ?: _customAlertSettings.value

    fun setAlertPreset(preset: AlertPreset) {
        prefs.edit().putString(KEY_ALERT_PRESET, preset.name).apply()
        _alertPreset.value = preset
    }

    fun setCustomAlertSettings(settings: AlertSettings) {
        prefs.edit()
            .putBoolean(KEY_ALERT_FAR, settings.alertFar)
            .putBoolean(KEY_ALERT_NEAR, settings.alertNear)
            .putBoolean(KEY_ALERT_TURN_POINT, settings.alertTurnPoint)
            .putBoolean(KEY_ALERT_ARRIVAL, settings.alertArrival)
            .putBoolean(KEY_ALERT_TURNS_ONLY, settings.turnsOnly)
            .putInt(KEY_ALERT_COOLDOWN, settings.cooldownSeconds)
            .apply()
        _customAlertSettings.value = settings
    }

    private fun loadCustomAlertSettings(): AlertSettings {
        val defaults = AlertSettings()
        return AlertSettings(
            alertFar = prefs.getBoolean(KEY_ALERT_FAR, defaults.alertFar),
            alertNear = prefs.getBoolean(KEY_ALERT_NEAR, defaults.alertNear),
            alertTurnPoint = prefs.getBoolean(KEY_ALERT_TURN_POINT, defaults.alertTurnPoint),
            alertArrival = prefs.getBoolean(KEY_ALERT_ARRIVAL, defaults.alertArrival),
            turnsOnly = prefs.getBoolean(KEY_ALERT_TURNS_ONLY, defaults.turnsOnly),
            cooldownSeconds = prefs.getInt(KEY_ALERT_COOLDOWN, defaults.cooldownSeconds)
        )
    }

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
        private const val KEY_SHOW_ETA = "show_eta"
        private const val KEY_ALERT_PRESET = "alert_preset"
        private const val KEY_ALERT_FAR = "alert_far"
        private const val KEY_ALERT_NEAR = "alert_near"
        private const val KEY_ALERT_TURN_POINT = "alert_turn_point"
        private const val KEY_ALERT_ARRIVAL = "alert_arrival"
        private const val KEY_ALERT_TURNS_ONLY = "alert_turns_only"
        private const val KEY_ALERT_COOLDOWN = "alert_cooldown_seconds"
        private const val KEY_TEMPLATE_TITLE = "notification_template_title"
        private const val KEY_TEMPLATE_CONTENT = "notification_template_content"
    }
}
