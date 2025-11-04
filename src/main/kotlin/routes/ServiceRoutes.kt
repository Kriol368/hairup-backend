package com.hairup.routes


import com.hairup.models.ServiceInput
import com.hairup.repositories.ServiceRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.serviceRoutes(serviceRepository: ServiceRepository) {
    route("/services") {
        get {
            val services = serviceRepository.getAllServices()
            call.respond(services)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }

            val service = serviceRepository.getServiceById(id)
            if (service == null) {
                call.respond(HttpStatusCode.NotFound, "Service not found")
            } else {
                call.respond(service)
            }
        }

        post {
            try {
                val serviceInput = call.receive<ServiceInput>()
                val service = serviceRepository.createService(serviceInput)
                call.respond(HttpStatusCode.Created, service)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid service data: ${e.message}")
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@put
            }

            try {
                val serviceInput = call.receive<ServiceInput>()
                val updatedService = serviceRepository.updateService(id, serviceInput)
                if (updatedService == null) {
                    call.respond(HttpStatusCode.NotFound, "Service not found")
                } else {
                    call.respond(updatedService)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid service data: ${e.message}")
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }

            val deleted = serviceRepository.deleteService(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Service deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Service not found")
            }
        }
    }
}