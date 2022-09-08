package me.mical.remoteoperate

import com.bekvon.bukkit.residence.api.ResidenceApi
import com.bekvon.bukkit.residence.containers.Flags
import com.bekvon.bukkit.residence.protection.FlagPermissions
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import taboolib.platform.BukkitPlugin

/**
 * @author xiaomu
 * @since 2022/9/6 10:15
 */
fun String.parseLocation(): Location {
    val (world, x, y, z) = split("&", limit = 4)

    return Location(plugin.server.getWorld(world), x.toDouble(), y.toDouble(), z.toDouble())
}

fun Location.parseString(): String {
    val world = this.world!!.name
    val x = blockX
    val y = blockY
    val z = blockZ
    return "$world&$x&$y&$z"
}

private val plugin by lazy {
    BukkitPlugin.getInstance()
}

val blockLockerEnabled = plugin.server.pluginManager.getPlugin("BlockLocker") != null

val residenceEnabled = plugin.server.pluginManager.getPlugin("Residence") != null

val itemsAdderEnabled = plugin.server.pluginManager.getPlugin("ItemsAdder") != null

fun Location.checkResidence(user: Player, flags: Flags): Boolean {
    val residence = ResidenceApi.getResidenceManager().getByLoc(this)
    return residenceEnabled && (residence.ownerUUID == user.uniqueId || residence.permissions.playerHas(
        user, flags, FlagPermissions.FlagCombo.OnlyTrue
    ) || residence.permissions.playerHas(user, Flags.admin, FlagPermissions.FlagCombo.OnlyTrue))
}

fun Block.checkLocker(user: Player): Boolean {
    return blockLockerEnabled && BlockLockerAPIv2.isAllowed(user, this, false)
}