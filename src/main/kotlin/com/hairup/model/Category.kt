package com.hairup.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface Category : Entity<Category> {
    companion object : Entity.Factory<Category>()

    val id: Int
    var name: String
}

object Categories : Table<Category>("category") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = text("name").bindTo { it.name }
}