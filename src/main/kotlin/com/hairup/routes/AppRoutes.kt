package com.hairup.routes

import com.hairup.model.ErrorResponse
import com.hairup.model.ListResponse
import com.hairup.model.MessageResponse
import com.hairup.service.AppServiceImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.appRoutes() {
    val appService = AppServiceImpl()

    authenticate("auth-jwt") {
        route("/api") {

            get("/appointments/next") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@get
                }

                val nextAppointment = appService.getNextAppointment(userId)

                if (nextAppointment == null) {
                    call.respond(HttpStatusCode.OK, MessageResponse("No tienes citas programadas"))
                } else {
                    call.respond(HttpStatusCode.OK, nextAppointment)
                }
            }

            get("/appointments/past") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@get
                }

                val pastAppointments = appService.getPastAppointments(userId, 3)
                call.respond(ListResponse(data = pastAppointments))
            }

            get("/user/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@get
                }

                val profile = appService.getUserProfile(userId)

                if (profile == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuario no encontrado"))
                } else {
                    call.respond(HttpStatusCode.OK, profile)
                }
            }

            get("/levels") {
                val levels = appService.getAllLevels()
                call.respond(ListResponse(data = levels))
            }

            get("/products") {
                val products = appService.getAllProducts()
                call.respond(ListResponse(data = products))
            }

            get("/services") {
                val services = appService.getAllServices()
                call.respond(ListResponse(data = services))
            }
        }
    }
}