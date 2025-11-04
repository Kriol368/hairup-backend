package com.hairup.routes

import com.hairup.models.UserInput
import com.hairup.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userRepository: UserRepository) {
    route("/users") {
        get {
            val users = userRepository.getAllUsers()
            call.respond(users)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }

            val user = userRepository.getUserById(id)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user)
            }
        }

        post {
            try {
                val userInput = call.receive<UserInput>()
                val user = userRepository.createUser(userInput)
                call.respond(HttpStatusCode.Created, user)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user data: ${e.message}")
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@put
            }

            try {
                val userInput = call.receive<UserInput>()
                val updatedUser = userRepository.updateUser(id, userInput)
                if (updatedUser == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                } else {
                    call.respond(updatedUser)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user data: ${e.message}")
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }

            val deleted = userRepository.deleteUser(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "User deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
    }
}