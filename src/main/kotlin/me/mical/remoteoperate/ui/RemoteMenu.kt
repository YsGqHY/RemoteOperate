package me.mical.remoteoperate.ui

import dev.lone.itemsadder.api.CustomStack
import me.mical.remoteoperate.ConfigReader
import me.mical.remoteoperate.api.RemoteAPI
import me.mical.remoteoperate.api.nms.NMS
import me.mical.remoteoperate.data.Remote
import me.mical.remoteoperate.data.Remote.Companion.getRemotes
import me.mical.remoteoperate.data.Remote.Companion.setRemote
import me.mical.remoteoperate.enums.RemoteType.*
import me.mical.remoteoperate.itemsAdderEnabled
import me.mical.remoteoperate.parseLocation
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.inventory.InventoryHolder
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.Slots
import taboolib.platform.util.buildItem
import taboolib.platform.util.sendLang
import java.lang.StringBuilder
import java.util.*

/**
 * @author xiaomu
 * @since 2022/9/6 09:37
 */
object RemoteMenu {

    fun open(viewer: Player) {
        viewer.openMenu<Linked<Remote>>(ConfigReader.getTitle(viewer)) {
            rows(6)
            handLocked(true)
            slots(Slots.CENTER)

            elements {
                viewer.getRemotes()
            }

            onGenerate { _, remote, _, _ ->
                buildItem(remote.type.toMaterial()) {
                    name = ConfigReader.getName(viewer).replaceWithOrder(
                        remote.type.name(viewer) to "type", remote.name to "name"
                    )
                    if (remote.type in listOf(VILLAGER, MINECART_HOPPER, MINECART_CHEST)) {
                        val entity = Bukkit.getEntity(UUID.fromString(remote.data))
                        if (entity == null || entity.isDead) {
                            lore.addAll(ConfigReader.getDied(viewer).map {
                                it.replaceWithOrder(
                                    remote.type.name(viewer) to "type", remote.name to "name"
                                )
                            })
                        } else {
                            lore.addAll(ConfigReader.getTemplate(viewer).map {
                                it.replaceWithOrder(
                                    remote.type.name(viewer) to "type",
                                    remote.name to "name",
                                    entity.location.world?.name to "world",
                                    entity.location.blockX to "x",
                                    entity.location.blockY to "y",
                                    entity.location.blockZ to "z"
                                )
                            })
                        }
                    } else {
                        val loc = remote.data.parseLocation()
                        lore.addAll(ConfigReader.getTemplate(viewer).map {
                            it.replaceWithOrder(
                                remote.type.name(viewer) to "type",
                                remote.name to "name",
                                loc.world?.name to "world",
                                loc.blockX to "x",
                                loc.blockY to "y",
                                loc.blockZ to "z"
                            )
                        })
                    }
                }
            }

            setNextPage(51) { _, hasNextPage ->
                val hasNext = buildItem(Material.SPECTRAL_ARROW) {
                    name = "&7--->".colored()
                }
                val next = buildItem(Material.ARROW) {
                    name = "&8--->".colored()
                }
                if (hasNextPage) {
                    if (itemsAdderEnabled) {
                        return@setNextPage CustomStack.getInstance(ConfigReader.iaHasNext)?.itemStack ?: hasNext
                    }
                    return@setNextPage hasNext
                } else {
                    if (itemsAdderEnabled) {
                        return@setNextPage CustomStack.getInstance(ConfigReader.iaNoNext)?.itemStack ?: next
                    }
                    return@setNextPage next
                }
            }

            setPreviousPage(47) { _, hasPreviousPage ->
                val hasPrevious = buildItem(Material.SPECTRAL_ARROW) {
                    name = "&7<---".colored()
                }
                val previous = buildItem(Material.ARROW) {
                    name = "&8<---".colored()
                }
                if (hasPreviousPage) {
                    if (itemsAdderEnabled) {
                        return@setPreviousPage CustomStack.getInstance(ConfigReader.iaHasBack)?.itemStack ?: hasPrevious
                    }
                    return@setPreviousPage hasPrevious
                } else {
                    if (itemsAdderEnabled) {
                        return@setPreviousPage CustomStack.getInstance(ConfigReader.iaNoBack)?.itemStack ?: previous
                    }
                    return@setPreviousPage previous
                }
            }

            onClick { e, element ->
                val all = ConfigReader.conditions["all"]?.get("open")
                val condition = ConfigReader.conditions[element.type.name]?.get("open")
                if (e.clickEvent().isLeftClick) {
                    if (RemoteAPI.check(viewer, "all", "open")) {
                        all?.actions?.forEach { RemoteAPI.eval(viewer, it.replacePlaceholder(viewer)) }
                        if (RemoteAPI.check(viewer, element.type.name, "open")) {
                            condition?.actions?.forEach { RemoteAPI.eval(viewer, it.replacePlaceholder(viewer)) }
                            when (element.type) {
                                CHEST, SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX, YELLOW_SHULKER_BOX, LIME_SHULKER_BOX, PINK_SHULKER_BOX, GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX, CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX, RED_SHULKER_BOX, BLACK_SHULKER_BOX, FURNACE, BLAST_FURNACE, HOPPER, DISPENSER, DROPPER, TRAPPED_CHEST, BARREL, SMOKER -> {
                                    val container = element.data.parseLocation().block
                                    if (container.state is Container) {
                                        viewer.openInventory((container.state as Container).inventory)
                                    }
                                }

                                CRAFTING_TABLE -> {
                                    viewer.openWorkbench(element.data.parseLocation(), true)
                                }

                                ENDER_CHEST -> {
                                    viewer.openInventory(viewer.enderChest)
                                }

                                VILLAGER -> {
                                    val villager = Bukkit.getEntity(UUID.fromString(element.data))
                                    if (villager == null || villager.isDead) {
                                        viewer.sendLang("VillagerDied", element.name)
                                        return@onClick
                                    }
                                    villager as Villager
                                    if (villager.isTrading) {
                                        viewer.sendLang("VillagerIsTrading", element.name)
                                        return@onClick
                                    }
                                    viewer.openMerchant(villager, false)
                                }

                                ENCHANTING_TABLE -> {
                                    viewer.openEnchanting(element.data.parseLocation(), true)
                                }

                                ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL, LOOM, GRINDSTONE, CARTOGRAPHY_TABLE, SMITHING_TABLE -> {
                                    NMS.INSTANCE.open(element.type, viewer)
                                }

                                MINECART_CHEST, MINECART_HOPPER -> {
                                    val entity = Bukkit.getEntity(UUID.fromString(element.data))
                                    if (entity is InventoryHolder) {
                                        viewer.openInventory(entity.inventory)
                                    }
                                }
                            }
                        }
                    } else {
                        all?.deny?.forEach { RemoteAPI.eval(viewer, it.replacePlaceholder(viewer)) }
                    }
                } else if (e.clickEvent().isRightClick) {
                    if (RemoteAPI.check(viewer, "all", "delete")) {
                        all?.actions?.forEach { RemoteAPI.eval(viewer, it.replacePlaceholder(viewer)) }
                        if (RemoteAPI.check(viewer, element.type.name, "delete")) {
                            condition?.actions?.forEach { RemoteAPI.eval(viewer, it.replacePlaceholder(viewer)) }
                            viewer.closeInventory()
                            val remotes = viewer.getRemotes()
                            if (remotes.size == 1) {
                                viewer.setRemote("")
                            } else {
                                val builder = StringBuilder()
                                remotes.remove(element)
                                remotes.forEach { builder.append("$it,") }
                                viewer.setRemote(builder.toString().removeSuffix(","))
                            }
                            viewer.sendLang("Deleted")
                        }
                    } else {
                        all?.deny?.forEach { RemoteAPI.eval(viewer, it.replacePlaceholder(viewer)) }
                    }
                }
            }
        }
    }
}