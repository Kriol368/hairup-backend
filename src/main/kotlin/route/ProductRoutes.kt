package com.hairup.route


import com.hairup.repository.ProductRepository
import com.hairup.plugin.DatabaseFactory
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes() {
    route("/products") {
        get {
            val products = DatabaseFactory.withTransaction { entityManager ->
                val productRepository = ProductRepository(entityManager)
                productRepository.findAll()
            }
            call.respond(products)
        }
    }
}