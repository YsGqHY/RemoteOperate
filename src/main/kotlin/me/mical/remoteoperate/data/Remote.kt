package me.mical.remoteoperate.data

import me.mical.remoteoperate.database.DatabasePlayer
import me.mical.remoteoperate.enums.RemoteType
import org.bukkit.entity.Player

/**
 * @author xiaomu
 * @since 2022/9/6 09:40
 */
data class Remote(val name: String, val type: RemoteType, val data: String) {

    override fun toString(): String {
        return "$name~${type.name}~$data"
    }

    companion object {

        fun Player.getRemotes(): ArrayList<Remote> {
            val remotes = DatabasePlayer.INSTANCE.get(uniqueId)?.split(",")?.toList() ?: return arrayListOf()
            val result = arrayListOf<Remote>()
            remotes.forEach {
                val (name, type, data) = it.split("~", limit = 3)
                result.add(Remote(name, RemoteType.valueOf(type), data))
            }
            return result
        }

        fun Player.addRemote(remote: Remote) {
            val remotes = DatabasePlayer.INSTANCE.get(uniqueId) ?: ""
            val builder = StringBuilder().append(remotes)
            if (remotes.isNotEmpty()) {
                builder.append(",")
            }
            builder.append("${remote.name}~${remote.type.name}~${remote.data}")
            DatabasePlayer.INSTANCE.insert(uniqueId, builder.toString())
        }

        fun Player.setRemote(content: String) {
            DatabasePlayer.INSTANCE.insert(uniqueId, content)
        }
    }
}