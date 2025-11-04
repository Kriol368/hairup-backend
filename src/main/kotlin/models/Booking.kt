package com.hairup.models

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: Int,
    val date: String,
    val time: String,
    val status: Int = 0,
    val userId: Int,
    val serviceId: Int
)

@Serializable
data class BookingInput(
    val date: String,
    val time: String,
    val status: Int = 0,
    val userId: Int,
    val serviceId: Int
)