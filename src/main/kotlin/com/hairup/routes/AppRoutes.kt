package com.hairup.routes

import com.hairup.model.*
import com.hairup.service.AppServiceImpl
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.appRoutes() {
    val appService = AppServiceImpl()

    // Endpoints GET (públicos o protegidos según necesites)
    route("/api/levels") {
        get {
            val levels = appService.getAllLevels()
            call.respond(ListResponse(data = levels))
        }
    }

    route("/api/services") {
        get {
            val services = appService.getAllServices()
            call.respond(ListResponse(data = services))
        }
    }

    route("/api/products") {
        get {
            val products = appService.getAllProducts()
            call.respond(ListResponse(data = products))
        }
    }

    // Endpoints protegidos por JWT
    authenticate("auth-jwt") {
        // Perfil del usuario
        route("/api/user") {
            get("/profile") {
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
        }

        // Citas
        route("/api/appointments") {
            // Próxima cita
            get("/next") {
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

            // Últimas 3 citas pasadas
            get("/past") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@get
                }

                val pastAppointments = appService.getPastAppointments(userId, 3)
                call.respond(ListResponse(data = pastAppointments))
            }

            // Crear nueva cita
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@post
                }

                try {
                    val request = call.receive<CreateBookingRequest>()

                    if (request.serviceId <= 0) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de servicio inválido"))
                        return@post
                    }

                    val result = appService.createBooking(userId, request)

                    result.fold(
                        onSuccess = { bookingId ->
                            call.respond(
                                HttpStatusCode.Created,
                                SuccessResponse(message = "Cita creada exitosamente", id = bookingId)
                            )
                        },
                        onFailure = { exception ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(exception.message ?: "Error al crear la cita")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Formato de request inválido: ${e.message}")
                    )
                }
            }

            // Actualizar cita específica
            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@put
                }

                val bookingId = call.parameters["id"]?.toIntOrNull()
                if (bookingId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de cita inválido"))
                    return@put
                }

                try {
                    val request = call.receive<UpdateBookingRequest>()

                    if (request.date == null && request.time == null && request.status == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Debe proporcionar al menos un campo para actualizar"))
                        return@put
                    }

                    val result = appService.updateBooking(bookingId, userId, request)

                    result.fold(
                        onSuccess = { success ->
                            if (success) {
                                call.respond(
                                    HttpStatusCode.OK,
                                    SuccessResponse(message = "Cita actualizada exitosamente")
                                )
                            } else {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorResponse("No se pudo actualizar la cita")
                                )
                            }
                        },
                        onFailure = { exception ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(exception.message ?: "Error al actualizar la cita")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Formato de request inválido: ${e.message}")
                    )
                }
            }

            // Eliminar cita
            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@delete
                }

                val bookingId = call.parameters["id"]?.toIntOrNull()
                if (bookingId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de cita inválido"))
                    return@delete
                }

                val result = appService.deleteBooking(bookingId, userId)

                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            call.respond(
                                HttpStatusCode.OK,
                                SuccessResponse(message = "Cita eliminada exitosamente")
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponse("Cita no encontrada")
                            )
                        }
                    },
                    onFailure = { exception ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(exception.message ?: "Error al eliminar la cita")
                        )
                    }
                )
            }
        }

        // Gestión de productos (solo admin)
        route("/api/admin/products") {
            // Crear producto (solo admin)
            post {
                val principal = call.principal<JWTPrincipal>()
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                if (!isAdmin) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Solo administradores pueden crear productos"))
                    return@post
                }

                try {
                    val request = call.receive<CreateProductRequest>()

                    if (request.name.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("El nombre del producto es requerido"))
                        return@post
                    }

                    if (request.price <= 0) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("El precio debe ser mayor a 0"))
                        return@post
                    }

                    val result = appService.createProduct(request)

                    result.fold(
                        onSuccess = { productId ->
                            call.respond(
                                HttpStatusCode.Created,
                                SuccessResponse(message = "Producto creado exitosamente", id = productId)
                            )
                        },
                        onFailure = { exception ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(exception.message ?: "Error al crear el producto")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Formato de request inválido: ${e.message}")
                    )
                }
            }

            // Actualizar producto (solo admin)
            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                if (!isAdmin) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Solo administradores pueden actualizar productos"))
                    return@put
                }

                val productId = call.parameters["id"]?.toIntOrNull()
                if (productId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de producto inválido"))
                    return@put
                }

                try {
                    val request = call.receive<UpdateProductRequest>()

                    if (request.name == null && request.description == null && request.price == null &&
                        request.image == null && request.available == null && request.points == null &&
                        request.categoryId == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Debe proporcionar al menos un campo para actualizar"))
                        return@put
                    }

                    val result = appService.updateProduct(productId, request)

                    result.fold(
                        onSuccess = { success ->
                            if (success) {
                                call.respond(
                                    HttpStatusCode.OK,
                                    SuccessResponse(message = "Producto actualizado exitosamente")
                                )
                            } else {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorResponse("Producto no encontrado")
                                )
                            }
                        },
                        onFailure = { exception ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(exception.message ?: "Error al actualizar el producto")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Formato de request inválido: ${e.message}")
                    )
                }
            }

            // Eliminar producto (solo admin)
            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                if (!isAdmin) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Solo administradores pueden eliminar productos"))
                    return@delete
                }

                val productId = call.parameters["id"]?.toIntOrNull()
                if (productId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de producto inválido"))
                    return@delete
                }

                val result = appService.deleteProduct(productId)

                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            call.respond(
                                HttpStatusCode.OK,
                                SuccessResponse(message = "Producto eliminado exitosamente")
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponse("Producto no encontrado")
                            )
                        }
                    },
                    onFailure = { exception ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(exception.message ?: "Error al eliminar el producto")
                        )
                    }
                )
            }
        }
    }
}