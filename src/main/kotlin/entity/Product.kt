package com.hairup.entity

import jakarta.persistence.*

@Entity
@Table(name = "product")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column
    var description: String? = null,

    @Column(nullable = false)
    var price: Double = 0.0,

    @Column
    var image: String? = null,

    @Column(nullable = false)
    var available: Boolean = false
) {
    constructor() : this(null, "", null, 0.0, null, false)
}