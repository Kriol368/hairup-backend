package com.hairup.entity

import jakarta.persistence.*

@Entity
@Table(name = "service")
data class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    var name: String,

    @Column
    var description: String? = null,

    @Column(nullable = false)
    var price: Double,

    @Column(nullable = false)
    var duration: Int,

    @Column(nullable = false)
    var xp: Int = 0,

    @OneToMany(mappedBy = "service")
    var bookings: MutableList<Booking> = mutableListOf()
)