package com.hairup.routes

import com.hairup.model.*
import com.hairup.service.AuthServiceImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val authService = AuthServiceImpl()

    route("/api/auth") {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()

                if (request.email.isBlank() || request.password.isBlank() || request.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email, password, and name are required"))
                    return@post
                }

                val result = authService.register(request)

                result.fold(
                    onSuccess = { authResponse ->
                        call.respond(HttpStatusCode.Created, authResponse)
                    },
                    onFailure = { exception ->
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(exception.message ?: "Registration failed"))
                    }
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format: ${e.message}"))
            }
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                if (request.email.isBlank() || request.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email and password are required"))
                    return@post
                }

                val result = authService.login(request)

                result.fold(
                    onSuccess = { authResponse ->
                        call.respond(HttpStatusCode.OK, authResponse)
                    },
                    onFailure = { exception ->
                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse(exception.message ?: "Login failed"))
                    }
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format: ${e.message}"))
            }
        }
    }
}