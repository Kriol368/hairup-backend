package com.hairup.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDate

interface User : Entity<User> {
    companion object : Entity.Factory<User>()
    var id: Int
    var email: String
    var password: String
    var name: String
    var xp: Int
    var admin: Boolean
    var phone: String?
    var created: LocalDate
    var levelId: Int
}

object Users : Table<User>("user") {
    val id = int("id").primaryKey().bindTo { it.id }
    val email = text("email").bindTo { it.email }
    val password = text("password").bindTo { it.password }
    val name = text("name").bindTo { it.name }
    val xp = int("xp").bindTo { it.xp }
    val admin = boolean("admin").bindTo { it.admin }
    val phone = text("phone").bindTo { it.phone }
    val created = date("created").bindTo { it.created }
    val levelId = int("level_id").bindTo { it.levelId }
}