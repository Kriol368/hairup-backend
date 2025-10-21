package com.hairup.repository

import com.hairup.entity.User
import jakarta.persistence.EntityManager

class UserRepository(private val entityManager: EntityManager) {

    fun findAll(): List<User> {
        return entityManager.createQuery(
            "SELECT u FROM User u LEFT JOIN FETCH u.level",
            User::class.java
        ).resultList
    }

    fun findById(id: Int): User? {
        return entityManager.createQuery(
            "SELECT u FROM User u LEFT JOIN FETCH u.level WHERE u.id = :id",
            User::class.java
        ).setParameter("id", id).resultList.firstOrNull()
    }

    fun findByEmail(email: String): User? {
        return entityManager.createQuery(
            "SELECT u FROM User u LEFT JOIN FETCH u.level WHERE u.email = :email",
            User::class.java
        ).setParameter("email", email).resultList.firstOrNull()
    }

    fun save(user: User): User {
        return if (user.id == null) {
            entityManager.persist(user)
            user
        } else {
            entityManager.merge(user)
        }
    }

    fun delete(id: Int) {
        val user = findById(id)
        user?.let { entityManager.remove(it) }
    }
}