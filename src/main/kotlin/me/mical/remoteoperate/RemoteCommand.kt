package me.mical.remoteoperate

import com.bekvon.bukkit.residence.containers.Flags
import me.mical.remoteoperate.api.RemoteAPI
import me.mical.remoteoperate.data.Remote
import me.mical.remoteoperate.data.Remote.Companion.addRemote
import me.mical.remoteoperate.enums.RemoteType
import me.mical.remoteoperate.ui.RemoteMenu
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.entity.minecart.HopperMinecart
import org.bukkit.entity.minecart.StorageMinecart
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.sendLang

/**
 * @author xiaomu
 * @since 2022/9/6 10:19
 */
@CommandHeader(name = "remote")
object RemoteCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val open = subCommand {
        execute<Player> { user, _, _ ->
            RemoteMenu.open(user)
        }
    }

    @CommandBody
    val create = subCommand {
        dynamic(commit = "name") {
            execute<Player> { user, _, name ->
                if (name.contains("~") || name.contains(",")) {
                    user.sendLang("IllegalName")
                    return@execute
                }
                val rayTraceResult = user.world.rayTraceEntities(
                    user.eyeLocation, user.eyeLocation.direction, 5.0
                ) {
                    !it.isDead && (it is Villager || it is StorageMinecart || it is HopperMinecart)
                }
                if (rayTraceResult == null || rayTraceResult.hitEntity == null) {
                    val block = user.getTargetBlockExact(5)
                    if (block == null || block.type !in RemoteType.get()) {
                        user.sendLang("NullTarget")
                        return@execute
                    }
                    val resBypass = when {
                        block.state is Container -> block.location.checkResidence(user, Flags.container)
                        block.type == Material.ENCHANTED_BOOK -> block.location.checkResidence(user, Flags.enchant)
                        block.type.name.contains("ANVIL") -> block.location.checkResidence(user, Flags.anvil)
                        block.type.name in listOf(
                            "CRAFTING_TABLE", "CARTOGRAPHY_TABLE", "SMITHING_TABLE"
                        ) -> block.location.checkResidence(user, Flags.table)

                        else -> block.location.checkResidence(user, Flags.admin)
                    }
                    if (resBypass) {
                        if (block.checkLocker(user)) {
                            if (RemoteAPI.check(user, block.type.name, "create")) {
                                ConfigReader.conditions[block.type.name]?.get("create")?.actions?.forEach {
                                    RemoteAPI.eval(
                                        user, it.replacePlaceholder(user)
                                    )
                                }
                                user.addRemote(
                                    Remote(
                                        name, RemoteType.valueOf(block.type.name), block.location.parseString()
                                    )
                                )
                                user.sendLang("Success")
                            } else {
                                ConfigReader.conditions[block.type.name]?.get("create")?.deny?.forEach {
                                    RemoteAPI.eval(
                                        user, it.replacePlaceholder(user)
                                    )
                                }
                            }
                        } else {
                            user.sendLang("BlockLocker")
                        }
                    } else {
                        user.sendLang("Residence")
                    }
                } else {
                    val entity = rayTraceResult.hitEntity!!
                    val resBypass = when (entity.type) {
                        EntityType.VILLAGER -> entity.location.checkResidence(user, Flags.trade)
                        EntityType.MINECART_CHEST, EntityType.MINECART_HOPPER -> entity.location.checkResidence(
                            user, Flags.container
                        )

                        else -> entity.location.checkResidence(user, Flags.admin)
                    }
                    if (resBypass) {
                        user.addRemote(
                            Remote(
                                name,
                                RemoteType.valueOf(entity.type.name),
                                rayTraceResult.hitEntity!!.uniqueId.toString()
                            )
                        )
                        user.sendLang("Success")
                    } else {
                        user.sendLang("Residence")
                    }
                }
            }
        }
    }
}