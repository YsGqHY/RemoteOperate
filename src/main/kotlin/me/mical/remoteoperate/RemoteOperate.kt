package me.mical.remoteoperate

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.warning
import taboolib.module.nms.MinecraftVersion

object RemoteOperate : Plugin() {

    override fun onLoad() {
        if (MinecraftVersion.majorLegacy < 11400) {
            warning("RemoteOperate need Minecraft 1.14 or higher.")
            disablePlugin()
        }
    }

    override fun onEnable() {
        if (ConfigReader.first) {
            warning("For first running, you have to go to the plugin config to configure database settings.")
            return
        }
    }
}