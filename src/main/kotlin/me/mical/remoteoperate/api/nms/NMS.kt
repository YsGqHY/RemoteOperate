package me.mical.remoteoperate.api.nms

import me.mical.remoteoperate.enums.RemoteType
import org.bukkit.entity.Player
import taboolib.module.nms.nmsProxy

/**
 * @author xiaomu
 * @since 2022/9/7 22:07
 */
abstract class NMS {

    abstract fun open(type: RemoteType, user: Player)

    companion object {

        val INSTANCE by lazy {
            nmsProxy<NMS>()
        }
    }
}