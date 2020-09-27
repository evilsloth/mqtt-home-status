package io.github.evilsloth.mqtthomestatus.homestatus

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import io.github.evilsloth.mqtthomestatus.settings.Settings

class HomeStatusProvider(private val context: Context, ssid: String) {

    private val networkName = "\"" + ssid + "\"";

    @Suppress("DEPRECATION")
    fun getHomeStatus(): HomeStatus {
        val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
            throw CannotDetermineHomeStatusException("Network info not available!")
        }

        if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (wifiManager.connectionInfo.ssid == networkName) {
                return HomeStatus.AT_HOME
            }
        }

        return HomeStatus.AWAY_FROM_HOME
    }

}
