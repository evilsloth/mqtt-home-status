package io.github.evilsloth.mqtthomestatus.settings

import com.google.gson.Gson

data class Settings(
    var enabled: Boolean = false,
    var ssid: String = "",
    var brokerUrl: String = "",
    var brokerClientId: String = "",
    var brokerUsername: String = "",
    var brokerPassword: String = "",
    var topic: String = ""
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        const val SETTINGS_KEY = "SETTINGS"

        fun fromJson(json: String): Settings {
            return Gson().fromJson(json, Settings::class.java)
        }
    }
}
