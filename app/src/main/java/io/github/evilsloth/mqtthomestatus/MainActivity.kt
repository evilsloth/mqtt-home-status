package io.github.evilsloth.mqtthomestatus

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.*
import io.github.evilsloth.mqtthomestatus.databinding.ActivityMainBinding
import io.github.evilsloth.mqtthomestatus.homestatus.HomeStatusWorker
import io.github.evilsloth.mqtthomestatus.homestatus.PERIODIC_WORKER_TAG
import io.github.evilsloth.mqtthomestatus.settings.Settings
import io.github.evilsloth.mqtthomestatus.settings.SettingsRepository
import io.github.evilsloth.mqtthomestatus.settings.SettingsSaveListener
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SettingsSaveListener {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        settingsRepository = SettingsRepository(this)

        var settings = settingsRepository.read()
        if (settings == null) {
            settings = Settings()
        }

        binding.settings = settings
        binding.settingsSaveListener = this
    }

    override fun onSettingsSaved(settings: Settings) {
        settingsRepository.save(settings)
        if (settings.enabled) {
            startHomeStatusWorker(settings)
        } else {
            stopHomeStatusWorker()
        }
        Toast.makeText(this, getString(R.string.settings_saved_message), Toast.LENGTH_LONG).show()
    }

    private fun startHomeStatusWorker(settings: Settings) {
        val workRequest = PeriodicWorkRequestBuilder<HomeStatusWorker>(15, TimeUnit.MINUTES)
            .setInputData(Data.Builder().putString(Settings.SETTINGS_KEY, settings.toJson()).build())
            .build()

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(PERIODIC_WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
    }

    private fun stopHomeStatusWorker() {
        WorkManager
            .getInstance(this)
            .cancelAllWork()
    }

}
