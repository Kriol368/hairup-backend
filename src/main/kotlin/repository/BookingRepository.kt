package com.hairup.repository

import com.hairup.entity.Booking
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

class BookingRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    fun findAll(): List<Booking> {
        return entityManager.createQuery("SELECT b FROM Booking b", Booking::class.java).resultList
    }

    fun findById(id: Int): Booking? {
        return entityManager.find(Booking::class.java, id)
    }

    fun findByUserId(userId: Int): List<Booking> {
        return entityManager.createQuery(
            "SELECT b FROM Booking b WHERE b.user.id = :userId",
            Booking::class.java
        ).setParameter("userId", userId).resultList
    }

    fun save(booking: Booking): Booking {
        return if (booking.id == null) {
            entityManager.persist(booking)
            booking
        } else {
            entityManager.merge(booking)
        }
    }

    fun delete(id: Int) {
        val booking = findById(id)
        booking?.let { entityManager.remove(it) }
    }
}