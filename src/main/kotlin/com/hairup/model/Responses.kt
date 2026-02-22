package com.hairup.model

import kotlinx.serialization.Serializable

@Serializable
data class AppointmentResponse(
    val id: Int,
    val serviceName: String,
    val serviceId: Int,
    val date: String,
    val time: String,
    val stylistName: String,
    val stylistId: Int?,
    val status: Int,
    val price: Double,
    val duration: Int,
    val xpEarned: Int
)
@Serializable
data class UserProfileResponse(
    val id: Int,
    val name: String,
    val email: String,
    val xp: Int,
    val points: Int,
    val levelName: String,
    val levelId: Int,
    val phone: String
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

@Serializable
data class ListResponse<T>(
    val success: Boolean = true, val data: List<T>
)

@Serializable
data class AdminUserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?
)

@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val name: String,
    val xp: Int,
    val points: Int,
    val admin: Boolean,
    val phone: String?,
    val created: String,
    val levelId: Int
)

@Serializable
data class AllUsersResponse(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val xp: Int,
    val points: Int,
    val admin: Boolean,
    val levelId: Int,
    val created: String
)

@Serializable
data class SuccessResponse(
    val success: Boolean = true,
    val message: String,
    val id: Int? = null
)

@Serializable
data class BarberAvailabilityResponse(
    val barberId: Int,
    val barberName: String,
    val date: String,
    val availableSlots: List<TimeSlot>
)

@Serializable
data class TimeSlot(
    val time: String,
    val available: Boolean,
    val serviceId: Int? = null,
    val serviceName: String? = null,
    val duration: Int? = null
)

@Serializable
data class AppointmentDetailResponse(
    val id: Int,
    val serviceName: String,
    val clientName: String,
    val clientPhone: String?,
    val date: String,
    val time: String,
    val status: Int
)


@Serializable
data class RewardResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val pointsCost: Int,
    val minLevelId: Int,
    val available: Boolean
)

@Serializable
data class RedeemResponse(
    val success: Boolean,
    val message: String,
    val newPoints: Int? = null
)
