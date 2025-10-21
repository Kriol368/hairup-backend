package com.hairup.entity

import com.hairup.dto.UserDto
import jakarta.persistence.*

@Entity
@Table(name = "user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    var email: String = "",

    @Column(nullable = false)
    var password: String = "",

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var xp: Int = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "level_id")
    var level: Level? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    var bookings: MutableList<Booking> = mutableListOf()
) {
    constructor() : this(null, "", "", "", 0, null, mutableListOf())

    fun toDto(): UserDto {
        return UserDto(
            id = this.id,
            email = this.email,
            name = this.name,
            xp = this.xp,
            levelId = this.level?.id
        )
    }
}