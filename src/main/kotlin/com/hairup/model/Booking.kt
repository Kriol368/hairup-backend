package com.hairup.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDate
import java.time.LocalTime

interface Booking : Entity<Booking> {
    companion object : Entity.Factory<Booking>()

    val id: Int
    var date: LocalDate
    var time: LocalTime
    var status: Int
    var userId: Int
    var serviceId: Int
    var barberId: Int?
}

object Bookings : Table<Booking>("booking") {
    val id = int("id").primaryKey().bindTo { it.id }
    val date = date("date").bindTo { it.date }
    val time = time("time").bindTo { it.time }
    val status = int("status").bindTo { it.status }
    val userId = int("user_id").bindTo { it.userId }
    val serviceId = int("service_id").bindTo { it.serviceId }
    val barberId = int("barber_id").bindTo { it.barberId }
}