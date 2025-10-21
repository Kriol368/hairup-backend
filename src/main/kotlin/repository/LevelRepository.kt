package com.hairup.repository

import com.hairup.entity.Level
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

class LevelRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    fun findAll(): List<Level> {
        return entityManager.createQuery("SELECT l FROM Level l", Level::class.java).resultList
    }

    fun findById(id: Int): Level? {
        return entityManager.find(Level::class.java, id)
    }

    fun save(level: Level): Level {
        return if (level.id == null) {
            entityManager.persist(level)
            level
        } else {
            entityManager.merge(level)
        }
    }

    fun delete(id: Int) {
        val level = findById(id)
        level?.let { entityManager.remove(it) }
    }
}