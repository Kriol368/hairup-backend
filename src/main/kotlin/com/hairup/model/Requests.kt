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
data class UserResponse(
    val id: Int,
    val email: String,
    val name: String,
    val xp: Int,
    val admin: Boolean,
    val phone: String?,
    val created: String,
    val levelId: Int
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

