package com.hairup.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface Level : Entity<Level> {
    companion object : Entity.Factory<Level>()

    val id: Int
    var name: String
    var required: Int
    var reward: String
}

object Levels : Table<Level>("level") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = text("name").bindTo { it.name }
    val required = int("required").bindTo { it.required }
    val reward = text("reward").bindTo { it.reward }
}