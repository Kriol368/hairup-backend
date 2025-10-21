package com.hairup.repository

import com.hairup.entity.Product
import jakarta.persistence.EntityManager

class ProductRepository(private val entityManager: EntityManager) {

    fun findAll(): List<Product> {
        return entityManager.createQuery("SELECT p FROM Product p", Product::class.java).resultList
    }
}