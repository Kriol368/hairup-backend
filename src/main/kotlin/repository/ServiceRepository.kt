package com.hairup.repository

import com.hairup.entity.Service
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

class ServiceRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    fun findAll(): List<Service> {
        return entityManager.createQuery("SELECT s FROM Service s", Service::class.java).resultList
    }

    fun findById(id: Int): Service? {
        return entityManager.find(Service::class.java, id)
    }

    fun save(service: Service): Service {
        return if (service.id == null) {
            entityManager.persist(service)
            service
        } else {
            entityManager.merge(service)
        }
    }

    fun delete(id: Int) {
        val service = findById(id)
        service?.let { entityManager.remove(it) }
    }
}