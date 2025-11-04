package com.hairup.plugins

import io.ktor.server.application.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("database.driver").getString()
        val jdbcURL = config.property("database.url").getString()
        val username = config.property("database.user").getString()
        val password = config.property("database.password").getString()
        val maxPoolSize = config.property("database.maxPoolSize").getString().toInt()

        val database = Database.connect(createHikariDataSource(
            url = jdbcURL,
            driver = driverClassName,
            user = username,
            password = password,
            maxPoolSize = maxPoolSize
        ))

        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Users, Levels, Services, Products, Bookings)
        }
    }

    private fun createHikariDataSource(
        url: String,
        driver: String,
        user: String = "",
        password: String = "",
        maxPoolSize: Int = 10
    ) = HikariDataSource(HikariConfig().apply {
        driverClassName = driver
        jdbcUrl = url
        username = user
        this.password = password
        maximumPoolSize = maxPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })

    suspend fun <T> dbExec(block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) {
            block()
        }
    }
}

object Users : Table("user") {
    val id = integer("id").autoIncrement()
    val email = text("email")
    val password = text("password")
    val name = text("name")
    val xp = integer("xp").default(0)
    val levelId = integer("level_id").nullable()
    override val primaryKey = PrimaryKey(id)
}

object Levels : Table("level") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val required = integer("required")
    val reward = text("reward")
    override val primaryKey = PrimaryKey(id)
}

object Services : Table("service") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val description = text("description").nullable()
    val price = double("price")
    val duration = integer("duration")
    val xp = integer("xp").default(0)
    override val primaryKey = PrimaryKey(id)
}

object Products : Table("product") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val description = text("description").nullable()
    val price = double("price")
    val image = text("image").nullable()
    val available = bool("available").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Bookings : Table("booking") {
    val id = integer("id").autoIncrement()
    val date = date("date")
    val time = time("time")
    val status = integer("status").default(0)
    override val primaryKey = PrimaryKey(id)

    val userId = reference("user_id", Users.id)
    val serviceId = reference("service_id", Services.id)
}