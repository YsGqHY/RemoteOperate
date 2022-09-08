package me.mical.remoteoperate.condition

import taboolib.library.configuration.ConfigurationSection

/**
 * @author xiaomu
 * @since 2022/9/7 17:12
 */
data class Condition(val condition: String?, val actions: List<String>, val deny: List<String>) {

    companion object {

        fun of(section: ConfigurationSection?): Condition {
            if (section == null) return Condition(null, emptyList(), emptyList())
            val condition = section.getString("condition")
            val actions = section.getStringList("actions")
            val deny = section.getStringList("deny")
            return Condition(condition, actions, deny)
        }
    }
}