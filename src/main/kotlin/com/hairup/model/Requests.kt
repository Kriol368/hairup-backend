package com.hairup.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse
)



@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class MessageResponse(
    val message: String
)

@Serializable
data class CreateBookingRequest(
    val serviceId: Int,
    val date: String,
    val time: String,
    val barberId: Int? = null
)

@Serializable
data class UpdateBookingRequest(
    val date: String? = null,
    val time: String? = null,
    val status: Int? = null
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val description: String? = null,
    val price: Double,
    val image: String? = null,
    val available: Boolean = true,
    val points: Int = 0,
    val categoryId: Int? = null
)

@Serializable
data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val image: String? = null,
    val available: Boolean? = null,
    val points: Int? = null,
    val categoryId: Int? = null
)


@Serializable
data class MakeAdminRequest(
    val userId: Int
)

@Serializable
data class RemoveAdminRequest(
    val userId: Int
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class RedeemRequest(
    val rewardId: Int
)



@Serializable
data class CreateCategoryRequest(
    val name: String
)

@Serializable
data class UpdateCategoryRequest(
    val name: String
)

@Serializable
data class CategorySuccessResponse(
    val success: Boolean = true,
    val message: String,
    val id: Int? = null
)

@Serializable
data class AddPointsRequest(
    val points: Int
)


@Serializable
data class CreateServiceRequest(
    val name: String,
    val description: String? = null,
    val price: Double,
    val duration: Int,
    val xp: Int
)

@Serializable
data class UpdateServiceRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val duration: Int? = null,
    val xp: Int? = null
)