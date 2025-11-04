package com.hairup

import com.hairup.plugins.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*

fun Application.module() {
    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        json()
    }

    configureDatabase()
    configureRouting()
}