package io.github.evilsloth.mqtthomestatus.homestatus

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.work.*
import io.github.evilsloth.mqtthomestatus.settings.Settings
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

private const val TAG = "HomeStatusWorker"

const val PERIODIC_WORKER_TAG = "PERIODIC_HOME_STATUS_WORKER"
const val NETWORK_CHANGE_WORKER_TAG = "NETWORK_CHANGE_HOME_STATUS_WORKER"

class HomeStatusWorker(private val appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    private val settings = Settings.fromJson(workerParams.inputData.getString(Settings.SETTINGS_KEY)!!)
    private val homeStatusProvider = HomeStatusProvider(appContext, settings.ssid)

    override fun doWork(): Result {
        try {
            val homeStatus = homeStatusProvider.getHomeStatus()
            sendStatusToBroker(homeStatus)
            Log.d(TAG, "HOME STATUS = $homeStatus")
        } catch (e: CannotDetermineHomeStatusException) {
            Log.e(TAG, "Home status not received", e)
            return Result.retry()
        } catch (e: MqttException) {
            Log.e(TAG, "Failed to connect to mqqt broker", e)
            return Result.retry()
        }

        scheduleWorkerOnConnectionStatusChange()
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        Log.d(TAG, "Worker stopped")
    }

    private fun sendStatusToBroker(homeStatus: HomeStatus) {
        val mqttClient = MqttClient(settings.brokerUrl, settings.brokerClientId, MemoryPersistence())
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.userName = settings.brokerUsername
        mqttConnectOptions.password = settings.brokerPassword.toCharArray()

        mqttClient.connect(mqttConnectOptions)
        mqttClient.publish(settings.topic, homeStatus.toString().toByteArray(), 1, true)
        mqttClient.disconnect()
        mqttClient.close()
    }

    @Suppress("DEPRECATION")
    private fun scheduleWorkerOnConnectionStatusChange() {
        val connectivityManager = appContext.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (connectivityManager.isActiveNetworkMetered) {
            scheduleWorkerOnNetworkType(NetworkType.UNMETERED)
        } else {
            scheduleWorkerOnNetworkType(NetworkType.METERED)
        }
    }

    private fun scheduleWorkerOnNetworkType(networkType: NetworkType) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<HomeStatusWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager
            .getInstance(appContext)
            .enqueueUniqueWork(NETWORK_CHANGE_WORKER_TAG, ExistingWorkPolicy.REPLACE, workRequest)
    }

}
