package com.hairup.entity

import jakarta.persistence.*

@Entity
@Table(name = "level")
data class Level(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var required: Int,

    @Column(nullable = false)
    var reward: String,

    @OneToMany(mappedBy = "level")
    var users: MutableList<User> = mutableListOf()
) {
    constructor() : this(null, "", 0, "", mutableListOf())
}