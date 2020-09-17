package com.example

import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.slf4j.LoggerFactory
import java.util.*

class DatabaseImpl : Database {
    val log = LoggerFactory.getLogger(DatabaseImpl::class.java)
    var sessionFactory: SessionFactory = connect()

    private fun connect(): SessionFactory {
        val url = "jdbc:mysql://${Config.databaseHost}:${Config.databasePort}/${Config.databaseName}?serverTimezone=${Config.databaseTimezone}"
        val props = Properties()
        props["hibernate.connection.provider_class"] = "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
        props["hibernate.hikari.dataSourceClassName"] = "com.mysql.cj.jdbc.MysqlDataSource"
        props["hibernate.hikari.dataSource.url"] = url
        props["hibernate.hikari.dataSource.user"] = Config.databaseUser
        props["hibernate.hikari.dataSource.password"] = Config.databasePassword
//        props["hibernate.hikari.maximumPoolSize"] = "10"
//        props["hibernate.hikari.minimumIdle"] = "5"
//        props["hibernate.hikari.idleTimeout"] = "30000"
        val hibernateConfig = Configuration().apply {
            properties = props
            addAnnotatedClass(Item::class.java)
        }
        val registry = StandardServiceRegistryBuilder()
            .applySettings(hibernateConfig.properties)
            .build()
        val factory = hibernateConfig.buildSessionFactory(registry)
        log.info("Successfully connected to database")
        return factory
    }

    override fun getItems(): List<Item> {
        return sessionFactory.openSession().use {
            it.createQuery("from Item", Item::class.java)
                .resultList
        }
    }
}