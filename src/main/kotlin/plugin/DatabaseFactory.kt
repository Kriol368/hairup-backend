package com.hairup.plugin

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseFactory {
    private var entityManagerFactory: EntityManagerFactory? = null

    fun init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("hairup-persistence")
    }

    suspend fun <T> withTransaction(block: (EntityManager) -> T): T = withContext(Dispatchers.IO) {
        val entityManager = entityManagerFactory!!.createEntityManager()
        try {
            entityManager.transaction.begin()
            val result = block(entityManager)
            entityManager.transaction.commit()
            result
        } catch (e: Exception) {
            entityManager.transaction.rollback()
            throw e
        } finally {
            entityManager.close()
        }
    }
}