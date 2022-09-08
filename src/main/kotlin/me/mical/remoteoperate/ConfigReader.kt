package me.mical.remoteoperate

import me.mical.remoteoperate.condition.Condition
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getStringColored
import taboolib.module.configuration.util.getStringListColored
import taboolib.platform.compat.replacePlaceholder

/**
 * @author xiaomu
 * @since 2022/9/6 09:44
 */
object ConfigReader {

    @Config(migrate = true)
    lateinit var conf: Configuration

    @ConfigNode("first")
    val first = true

    val database: ConfigurationSection
        get() = conf.getConfigurationSection("database") ?: error("no database section")

    @ConfigNode("iahasnext")
    val iaHasNext = "icon_next_orange"

    @ConfigNode("ianonext")
    val iaNoNext = "icon_next_white"

    @ConfigNode("iahasback")
    val iaHasBack = "icon_back_orange"

    @ConfigNode("ianoback")
    val iaNoBack = "icon_back_white"

    val conditions = hashMapOf<String, HashMap<String, Condition>>()

    @Awake(LifeCycle.ENABLE)
    fun init() {
        conditions.clear()
        for (key in conf.getConfigurationSection("config")?.getKeys(false) ?: return) {
            val create = conf.getConfigurationSection("config.$key.create")
            val open = conf.getConfigurationSection("config.$key.open")
            val delete = conf.getConfigurationSection("config.$key.delete")
            conditions[key] = hashMapOf(
                "create" to Condition.of(create), "open" to Condition.of(open), "delete" to Condition.of(delete)
            )
        }
    }

    fun getTitle(user: Player): String {
        return (conf.getStringColored("title") ?: "chest").replacePlaceholder(user)
    }

    fun getName(user: Player): String {
        return (conf.getStringColored("name") ?: "chest").replacePlaceholder(user)
    }

    fun getTemplate(user: Player): List<String> {
        return conf.getStringListColored("template").map { it.replacePlaceholder(user) }
    }

    fun getDied(user: Player): List<String> {
        return conf.getStringListColored("died").map { it.replacePlaceholder(user) }
    }
}