package com.example

import java.util.*

object Config {
    lateinit var databaseHost: String
    lateinit var databasePort: String
    lateinit var databaseUser: String
    lateinit var databasePassword: String
    lateinit var databaseName: String
    lateinit var databaseTimezone: String
}

fun Properties.loadConfig() {
    Config.databaseHost = get("database.host") as String
    Config.databasePort = get("database.port") as String
    Config.databaseUser = get("database.user") as String
    Config.databasePassword = get("database.password") as String
    Config.databaseName = get("database.name") as String
    Config.databaseTimezone = get("database.timezone") as String
}
