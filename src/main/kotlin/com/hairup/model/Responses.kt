package com.hairup.model

import kotlinx.serialization.Serializable

// Respuestas para los endpoints que necesitas

@Serializable
data class NextAppointmentResponse(
    val id: Int, val serviceName: String, val date: String, val time: String
)

@Serializable
data class PastAppointmentResponse(
    val id: Int, val serviceName: String, val date: String, val xpEarned: Int
)

@Serializable
data class UserProfileResponse(
    val id: Int, val name: String, val email: String, val xp: Int, val levelName: String, val levelId: Int, val phone: String
)

@Serializable
data class LevelResponse(
    val id: Int, val name: String, val requiredXp: Int, val reward: String
)

@Serializable
data class ProductResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val image: String?,
    val available: Boolean,
    val points: Int,
    val categoryName: String?,
    val categoryId: Int?
)

@Serializable
data class ServiceResponse(
    val id: Int, val name: String, val description: String?, val price: Double, val duration: Int, val xpReward: Int
)

// Para respuestas con listas
@Serializable
data class ListResponse<T>(
    val success: Boolean = true, val data: List<T>
)