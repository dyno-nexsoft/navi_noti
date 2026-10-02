package com.dyno.navi_noti.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.dyno.navi_noti.data.model.AppLanguage
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
    }
}
