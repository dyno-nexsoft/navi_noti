package com.dyno.navi_noti.util

import android.content.Context
import android.content.res.Configuration
import com.dyno.navi_noti.data.model.AppLanguage
import java.util.Locale

fun Context.withAppLanguage(language: AppLanguage): Context {
    if (language == AppLanguage.SYSTEM) return this

    val locale = Locale.forLanguageTag(language.code)
    Locale.setDefault(locale)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}
