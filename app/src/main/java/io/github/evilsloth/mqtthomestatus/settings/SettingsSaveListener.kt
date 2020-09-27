package io.github.evilsloth.mqtthomestatus.settings

@FunctionalInterface
interface SettingsSaveListener {

    fun onSettingsSaved(settings: Settings)

}
