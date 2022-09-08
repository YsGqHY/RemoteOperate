package me.mical.remoteoperate.enums

import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.module.nms.getI18nName
import taboolib.platform.util.buildItem

/**
 * @author xiaomu
 * @since 2022/9/6 09:38
 */
enum class RemoteType {

    CHEST, CRAFTING_TABLE, ENDER_CHEST, VILLAGER, FURNACE, ENCHANTING_TABLE, ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL, SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX, YELLOW_SHULKER_BOX, LIME_SHULKER_BOX, PINK_SHULKER_BOX, GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX, CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX, RED_SHULKER_BOX, BLACK_SHULKER_BOX, HOPPER, DISPENSER, DROPPER, TRAPPED_CHEST, MINECART_CHEST, MINECART_HOPPER, LOOM, BARREL, SMOKER, BLAST_FURNACE, CARTOGRAPHY_TABLE, GRINDSTONE, SMITHING_TABLE;

    fun toMaterial(): Material {
        return when (this) {
            MINECART_CHEST -> Material.CHEST_MINECART
            MINECART_HOPPER -> Material.HOPPER_MINECART
            VILLAGER -> Material.VILLAGER_SPAWN_EGG
            else -> Material.valueOf(name)
        }
    }

    fun name(player: Player?): String {
        return when (this) {
            MINECART_CHEST, MINECART_HOPPER -> {
                val (left, right) = name.split("_", limit = 2)
                buildItem(Material.valueOf("${right}_$left")).getI18nName(player)
            }

            VILLAGER -> if (player == null) "村民" else if (player.locale == "en_us") "Villager" else "村民" // 等待坏黑的黑奴
            else -> buildItem(toMaterial()).getI18nName()
        }
    }

    companion object {

        fun get(): List<Material> {
            return values().map { it.toMaterial() }
        }
    }
}