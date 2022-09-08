package me.mical.remoteoperate.api

import me.mical.remoteoperate.ConfigReader
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common5.Coerce
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.KetherShell
import java.util.concurrent.CompletableFuture

/**
 * @author xiaomu
 * @since 2022/9/7 17:47
 */
object RemoteAPI {

    @JvmStatic
    fun eval(player: Player, script: String) {
        try {
            KetherShell.eval(script, namespace = listOf("aghstreasure")) {
                sender = adaptPlayer(player)
            }
        } catch (e: LocalizedException) {
            println("§c[RemoteOperate] §8Unexpected exception while parsing kether shell:")
            e.localizedMessage.split("\n").forEach {
                println("         §8$it")
            }
        }
    }

    private fun check(player: Player, script: String?): CompletableFuture<Boolean> {
        return if (script.isNullOrEmpty()) CompletableFuture.completedFuture(true) else {
            try {
                KetherShell.eval(script, sender = adaptPlayer(player)).thenApply {
                    Coerce.toBoolean(it)
                }
            } catch (e: Throwable) {
                println("§c[RemoteOperate] §8Unexpected exception while parsing kether shell:")
                e.localizedMessage.split("\n").forEach {
                    println("         §8$it")
                }
                CompletableFuture.completedFuture(false)
            }
        }
    }

    fun check(player: Player, name: String, operate: String): Boolean {
        return check(player, ConfigReader.conditions[name]?.get(operate)?.condition).get()
    }
}