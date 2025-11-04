package com.hairup.models

import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val id: Int,
    val name: String,
    val required: Int,
    val reward: String
)

@Serializable
data class LevelInput(
    val name: String,
    val required: Int,
    val reward: String
)