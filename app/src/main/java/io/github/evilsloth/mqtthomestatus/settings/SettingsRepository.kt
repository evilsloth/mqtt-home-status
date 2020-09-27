package io.github.evilsloth.mqtthomestatus.settings

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

private const val PREFERENCES_NAME = "HS_PREFS"
private const val SETTINGS_KEY = "SETTINGS"

class SettingsRepository(private val context: Context) {

    fun save(settings: Settings) {
        val settingsJson = Gson().toJson(settings)
        val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, 0)
        preferences.edit().putString(SETTINGS_KEY, settingsJson).apply()
    }

    fun read(): Settings? {
        val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, 0)
        val settingsJson = preferences.getString(SETTINGS_KEY, null)
        return Gson().fromJson(settingsJson, Settings::class.java)
    }

}