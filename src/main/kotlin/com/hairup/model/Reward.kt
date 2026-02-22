package com.hairup.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface Reward : Entity<Reward> {
    companion object : Entity.Factory<Reward>()

    val id: Int
    var name: String
    var description: String?
    var pointsCost: Int
    var minLevelId: Int
    var available: Boolean
}

object Rewards : Table<Reward>("rewards") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val description = varchar("description").bindTo { it.description }
    val pointsCost = int("points_cost").bindTo { it.pointsCost }
    val minLevelId = int("min_level_id").bindTo { it.minLevelId }
    val available = boolean("available").bindTo { it.available }
}