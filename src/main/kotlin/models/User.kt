package com.hairup.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val email: String,
    val password: String,
    val name: String,
    val xp: Int = 0,
    val levelId: Int?
)

@Serializable
data class UserInput(
    val email: String,
    val password: String,
    val name: String,
    val levelId: Int? = null
)