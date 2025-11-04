package com.hairup.routes

import com.hairup.models.LevelInput
import com.hairup.repositories.LevelRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.levelRoutes(levelRepository: LevelRepository) {
    route("/levels") {
        get {
            val levels = levelRepository.getAllLevels()
            call.respond(levels)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }

            val level = levelRepository.getLevelById(id)
            if (level == null) {
                call.respond(HttpStatusCode.NotFound, "Level not found")
            } else {
                call.respond(level)
            }
        }

        post {
            try {
                val levelInput = call.receive<LevelInput>()
                val level = levelRepository.createLevel(levelInput)
                call.respond(HttpStatusCode.Created, level)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid level data: ${e.message}")
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@put
            }

            try {
                val levelInput = call.receive<LevelInput>()
                val updatedLevel = levelRepository.updateLevel(id, levelInput)
                if (updatedLevel == null) {
                    call.respond(HttpStatusCode.NotFound, "Level not found")
                } else {
                    call.respond(updatedLevel)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid level data: ${e.message}")
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }

            val deleted = levelRepository.deleteLevel(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Level deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Level not found")
            }
        }
    }
}