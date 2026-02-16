package com.hairup.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface Product : Entity<Product> {
    companion object : Entity.Factory<Product>()

    val id: Int
    var name: String
    var description: String?
    var price: Double
    var image: String?
    var available: Boolean
    var points: Int
    var categoryId: Int?
}

object Products : Table<Product>("product") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = text("name").bindTo { it.name }
    val description = text("description").bindTo { it.description }
    val price = double("price").bindTo { it.price }
    val image = text("image").bindTo { it.image }
    val available = boolean("available").bindTo { it.available }
    val points = int("points").bindTo { it.points }
    val categoryId = int("category_id").bindTo { it.categoryId }
}