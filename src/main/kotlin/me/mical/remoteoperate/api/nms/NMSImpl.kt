package me.mical.remoteoperate.api.nms

import me.mical.remoteoperate.enums.RemoteType
import org.bukkit.entity.Player
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.nmsClass
import taboolib.module.nms.obcClass

/**
 * @author xiaomu
 * @since 2022/9/7 22:08
 */
class NMSImpl : NMS() {

    private val anvil = when {
        MinecraftVersion.majorLegacy >= 11900 -> "fT"
        MinecraftVersion.majorLegacy >= 11700 -> "fB"
        MinecraftVersion.majorLegacy >= 11400 -> "ANVIL"
        else -> error("not supported yet")
    }

    private val loom = when {
        MinecraftVersion.majorLegacy >= 11900 -> "mE"
        MinecraftVersion.majorLegacy >= 11700 -> "mf"
        MinecraftVersion.majorLegacy >= 11400 -> "LOOM"
        else -> error("not supported yet")
    }

    private val grindstone = when {
        MinecraftVersion.majorLegacy >= 11900 -> "mK"
        MinecraftVersion.majorLegacy >= 11700 -> "ml"
        MinecraftVersion.majorLegacy >= 11400 -> "GRINDSTONE"
        else -> error("not supported yet")
    }

    private val cartographyTable = when {
        MinecraftVersion.majorLegacy >= 11900 -> "mI"
        MinecraftVersion.majorLegacy >= 11700 -> "mj"
        MinecraftVersion.majorLegacy >= 11400 -> "CARTOGRAPHY_TABLE"
        else -> error("not supported yet")
    }

    private val smithingTable = when {
        MinecraftVersion.majorLegacy >= 11900 -> "mM"
        MinecraftVersion.majorLegacy >= 11700 -> "mn"
        MinecraftVersion.majorLegacy >= 11400 -> "SMITHING_TABLE"
        else -> error("not supported yet")
    }

    private val activeContainer = when {
        MinecraftVersion.majorLegacy >= 11900 -> "bU"
        MinecraftVersion.majorLegacy >= 11700 -> "bV"
        MinecraftVersion.majorLegacy >= 11400 -> "activeContainer"
        else -> error("not supported yet")
    }

    override fun open(type: RemoteType, user: Player) {
        val humanEntity = obcClass("entity.CraftHumanEntity").cast(user).invokeMethod<Any>("getHandle")
        val world = obcClass("CraftWorld").cast(user.world).invokeMethod<Any>("getHandle")
        val blockClass = when (type) {
            RemoteType.ANVIL, RemoteType.CHIPPED_ANVIL, RemoteType.DAMAGED_ANVIL -> nmsClass("BlockAnvil").cast(
                nmsClass(
                    "Blocks"
                ).getField(anvil).get(Any())
            )

            RemoteType.LOOM -> nmsClass("BlockLoom").cast(nmsClass("Blocks").getField(loom).get(Any()))

            RemoteType.GRINDSTONE -> nmsClass("BlockGrindstone").cast(
                nmsClass("Blocks").getField(grindstone).get(Any())
            )

            RemoteType.CARTOGRAPHY_TABLE -> nmsClass("BlockCartographyTable").cast(
                nmsClass("Blocks").getField(cartographyTable).get(Any())
            )

            RemoteType.SMITHING_TABLE -> nmsClass("BlockSmithingTable").cast(
                nmsClass("Blocks").getField(smithingTable).get(Any())
            )

            else -> error("not supported yet")
        }
        val inv = blockClass.invokeMethod<Any>(
            if (MinecraftVersion.majorLegacy >= 11800) "b" else "getInventory",
            null,
            world,
            nmsClass("BlockPosition").invokeConstructor(user.location.x, user.location.y, user.location.z)
        )
        humanEntity!!.invokeMethod<Any>(
            if (MinecraftVersion.majorLegacy >= 11800) "a" else "openContainer", inv
        )
        humanEntity.getProperty<Any>(activeContainer, isStatic = false)!!.setProperty("checkReachable", false)
    }
}