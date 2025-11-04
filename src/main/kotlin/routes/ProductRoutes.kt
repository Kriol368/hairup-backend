package com.hairup.routes


import com.hairup.models.ProductInput
import com.hairup.repositories.ProductRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(productRepository: ProductRepository) {
    route("/products") {
        get {
            val availableOnly = call.request.queryParameters["available"]?.toBooleanStrictOrNull() ?: false
            val products = if (availableOnly) {
                productRepository.getAvailableProducts()
            } else {
                productRepository.getAllProducts()
            }
            call.respond(products)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }

            val product = productRepository.getProductById(id)
            if (product == null) {
                call.respond(HttpStatusCode.NotFound, "Product not found")
            } else {
                call.respond(product)
            }
        }

        post {
            try {
                val productInput = call.receive<ProductInput>()
                val product = productRepository.createProduct(productInput)
                call.respond(HttpStatusCode.Created, product)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product data: ${e.message}")
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@put
            }

            try {
                val productInput = call.receive<ProductInput>()
                val updatedProduct = productRepository.updateProduct(id, productInput)
                if (updatedProduct == null) {
                    call.respond(HttpStatusCode.NotFound, "Product not found")
                } else {
                    call.respond(updatedProduct)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product data: ${e.message}")
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }

            val deleted = productRepository.deleteProduct(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Product deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Product not found")
            }
        }
    }
}