package me.mical.remoteoperate.database

import me.mical.remoteoperate.ConfigReader
import taboolib.module.database.ColumnOptionSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.HostSQL
import taboolib.module.database.Table
import java.util.UUID

/**
 * @author xiaomu
 * @since 2022/9/6 10:45
 */
class DatabasePlayer {

    private val host = HostSQL(ConfigReader.database)

    private val table = Table("remoteoperate_data", host) {
        add("user") {
            type(ColumnTypeSQL.VARCHAR, 255) {
                options(ColumnOptionSQL.PRIMARY_KEY)
            }
        }
        add("data") {
            type(ColumnTypeSQL.MEDIUMTEXT)
        }
    }

    private val dataSource = host.createDataSource(withoutConfig = true)

    init {
        table.workspace(dataSource) { createTable(true) }.run()
    }

    fun insert(user: UUID, data: String) {
        if (get(user) == null) {
            table.insert(dataSource, "user", "data") { value(user.toString(), data) }
        } else {
            table.update(dataSource) { where { "user" eq user.toString() }; set("data", data) }
        }
    }

    fun get(user: UUID): String? {
        return table.select(dataSource) { where { "user" eq user.toString() } }.firstOrNull { getString("data") }
    }

    companion object {

        val INSTANCE = DatabasePlayer()
    }
}