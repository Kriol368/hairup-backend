package com.hairup.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface Service : Entity<Service> {
    companion object : Entity.Factory<Service>()

    val id: Int
    var name: String
    var description: String?
    var price: Double
    var duration: Int
    var xp: Int
}

object Services : Table<Service>("service") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = text("name").bindTo { it.name }
    val description = text("description").bindTo { it.description }
    val price = double("price").bindTo { it.price }
    val duration = int("duration").bindTo { it.duration }
    val xp = int("xp").bindTo { it.xp }
}