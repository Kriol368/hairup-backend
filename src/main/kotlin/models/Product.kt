package com.hairup.models

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val image: String?,
    val available: Boolean = false
)

@Serializable
data class ProductInput(
    val name: String,
    val description: String? = null,
    val price: Double,
    val image: String? = null,
    val available: Boolean = false
)