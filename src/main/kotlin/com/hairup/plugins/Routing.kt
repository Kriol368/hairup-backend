package com.hairup.plugins

import com.hairup.routes.authRoutes
import com.hairup.routes.protectedRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authRoutes()
        protectedRoutes()
    }
}