package com.hairup.models

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val duration: Int,
    val xp: Int = 0
)

@Serializable
data class ServiceInput(
    val name: String,
    val description: String? = null,
    val price: Double,
    val duration: Int,
    val xp: Int = 0
)