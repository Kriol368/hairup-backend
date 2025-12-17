package com.hairup.config

import org.ktorm.database.Database

object DatabaseConfig {
    val database: Database by lazy {
        Database.connect(
            url = System.getenv("DATABASE_URL") ?: "jdbc:mysql://127.0.0.1:3306/hairup",
            driver = "com.mysql.cj.jdbc.Driver",
            user = System.getenv("DATABASE_USER") ?: "kriol",
            password = System.getenv("DATABASE_PASSWORD") ?: "Taller2014"
        )
    }
}