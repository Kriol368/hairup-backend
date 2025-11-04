package com.hairup.plugins

import com.hairup.repositories.*
import com.hairup.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureDatabase() {
    DatabaseFactory.init(environment.config)
}

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val serviceRepository = ServiceRepository()
    val bookingRepository = BookingRepository()
    val productRepository = ProductRepository()
    val levelRepository = LevelRepository()

    routing {
        get("/") {
            call.respond(mapOf("message" to "Welcome to Hairup API"))
        }

        userRoutes(userRepository)
        serviceRoutes(serviceRepository)
        bookingRoutes(bookingRepository)
        productRoutes(productRepository)
        levelRoutes(levelRepository)
    }
}