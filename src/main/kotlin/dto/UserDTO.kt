package com.hairup.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int?,
    val email: String,
    val name: String,
    val xp: Int,
    val levelId: Int?
)

