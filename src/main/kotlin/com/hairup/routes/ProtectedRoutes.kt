package com.hairup.routes

import com.hairup.model.MessageResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.protectedRoutes() {
    authenticate("auth-jwt") {
        route("/api") {
            get("/hello") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)
                val email = principal?.getClaim("email", String::class)

                call.respond(
                    MessageResponse("Hello World! User ID: $userId, Email: $email")
                )
            }

            // Example of another protected endpoint
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)
                val email = principal?.getClaim("email", String::class)
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                call.respond(
                    mapOf(
                        "userId" to userId,
                        "email" to email,
                        "isAdmin" to isAdmin
                    )
                )
            }
        }
    }
}