package com.hairup.plugins

import com.hairup.routes.appRoutes
import com.hairup.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authRoutes()
        appRoutes()
    }
}