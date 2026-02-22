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

    route("/api/admin-users") {
        get {
            val adminUsers = appService.getAdminUsers()
            call.respond(ListResponse(data = adminUsers))
        }
    }

    authenticate("auth-jwt") {
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

            put("/profile") {

                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@put
                }

                try {
                    val request = call.receive<UpdateProfileRequest>()

                    if (request.name == null && request.email == null && request.phone == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Debes proporcionar al menos un campo para actualizar")
                        )
                        return@put
                    }

                    if (!request.email.isNullOrBlank() && !isValidEmail(request.email)) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Formato de email inválido"))
                        return@put
                    }

                    if (!request.name.isNullOrBlank() && request.name.length < 2) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("El nombre debe tener al menos 2 caracteres")
                        )
                        return@put
                    }

                    val result = appService.updateUserProfile(userId, request)

                    result.fold(
                        onSuccess = { updatedProfile ->
                            call.respond(HttpStatusCode.OK, updatedProfile)
                        },
                        onFailure = { exception ->
                            when (exception.message) {
                                "Usuario no encontrado" ->
                                    call.respond(HttpStatusCode.NotFound, ErrorResponse(exception.message!!))

                                "El email ya está siendo utilizado por otro usuario" ->
                                    call.respond(HttpStatusCode.Conflict, ErrorResponse(exception.message!!))

                                else ->
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse(exception.message ?: "Error al actualizar el perfil")
                                    )
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Formato de request inválido: ${e.message}")
                    )
                }
            }

            post("/change-password") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@post
                }

                try {
                    val request = call.receive<ChangePasswordRequest>()

                    if (request.currentPassword.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("La contraseña actual es requerida"))
                        return@post
                    }

                    if (request.newPassword.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("La nueva contraseña es requerida"))
                        return@post
                    }

                    if (request.newPassword.length < 6) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("La nueva contraseña debe tener al menos 6 caracteres")
                        )
                        return@post
                    }

                    if (request.currentPassword == request.newPassword) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("La nueva contraseña debe ser diferente a la actual")
                        )
                        return@post
                    }

                    val result = appService.changePassword(userId, request)

                    result.fold(
                        onSuccess = {
                            call.respond(
                                HttpStatusCode.OK,
                                MessageResponse("Contraseña actualizada exitosamente")
                            )
                        },
                        onFailure = { exception ->
                            when (exception.message) {
                                "Usuario no encontrado" ->
                                    call.respond(HttpStatusCode.NotFound, ErrorResponse(exception.message!!))

                                "Contraseña actual incorrecta" ->
                                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(exception.message!!))

                                else ->
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse(exception.message ?: "Error al cambiar la contraseña")
                                    )
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Formato de request inválido: ${e.message}")
                    )
                }
            }

        }

        route("/api/admin/users") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                if (!isAdmin) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("Solo administradores pueden acceder a este recurso")
                    )
                    return@get
                }

                val users = appService.getAllUsers()
                call.respond(ListResponse(data = users))
            }
        }

        route("/api/admin/make-admin") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                if (!isAdmin) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("Solo administradores pueden realizar esta acción")
                    )
                    return@post
                }

                try {
                    val request = call.receive<MakeAdminRequest>()

                    if (request.userId <= 0) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de usuario inválido"))
                        return@post
                    }

                    val currentUserId = principal?.getClaim("userId", Int::class)
                    if (currentUserId == request.userId) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("No puedes modificar tu propio estado de administrador")
                        )
                        return@post
                    }

                    val result = appService.makeUserAdmin(request.userId)

                    result.fold(
                        onSuccess = { _ ->
                            call.respond(
                                HttpStatusCode.OK,
                                SuccessResponse(message = "Usuario promovido a administrador exitosamente")
                            )
                        },
                        onFailure = { exception ->
                            when (exception.message) {
                                "Usuario no encontrado" ->
                                    call.respond(HttpStatusCode.NotFound, ErrorResponse(exception.message!!))

                                else ->
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse(exception.message ?: "Error al promover usuario")
                                    )
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Formato de request inválido: ${e.message}")
                    )
                }
            }
        }
        route("/api/barbers/{barberId}/availability") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@get
                }

                val barberId = call.parameters["barberId"]?.toIntOrNull()
                if (barberId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de barbero inválido"))
                    return@get
                }

                val date = call.request.queryParameters["date"]
                if (date.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Parámetro 'date' requerido (formato YYYY-MM-DD)")
                    )
                    return@get
                }

                val result = appService.getBarberAvailableHours(barberId, date)

                result.fold(
                    onSuccess = { availability ->
                        call.respond(HttpStatusCode.OK, availability)
                    },
                    onFailure = { exception ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(exception.message ?: "Error al obtener disponibilidad")
                        )
                    }
                )
            }
        }

        route("/api/barbers/{barberId}/bookings") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val currentUserId = principal?.getClaim("userId", Int::class)
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                if (currentUserId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@get
                }

                val barberId = call.parameters["barberId"]?.toIntOrNull()
                if (barberId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de barbero inválido"))
                    return@get
                }

                if (currentUserId != barberId && !isAdmin) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("No tienes permiso para ver las citas de este barbero")
                    )
                    return@get
                }

                val bookings = appService.getBookingsByBarber(barberId)
                call.respond(ListResponse(data = bookings))
            }
        }

        route("/api/admin/remove-admin") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                if (!isAdmin) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("Solo administradores pueden realizar esta acción")
                    )
                    return@post
                }

                try {
                    val request = call.receive<RemoveAdminRequest>()

                    if (request.userId <= 0) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de usuario inválido"))
                        return@post
                    }

                    val currentUserId = principal?.getClaim("userId", Int::class)
                    if (currentUserId == request.userId) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("No puedes quitarte tus propios privilegios de administrador")
                        )
                        return@post
                    }

                    val result = appService.removeUserAdmin(request.userId)

                    result.fold(
                        onSuccess = { _ ->
                            call.respond(
                                HttpStatusCode.OK,
                                SuccessResponse(message = "Privilegios de administrador removidos exitosamente")
                            )
                        },
                        onFailure = { exception ->
                            when (exception.message) {
                                "Usuario no encontrado" ->
                                    call.respond(HttpStatusCode.NotFound, ErrorResponse(exception.message!!))

                                "No puedes quitar el último administrador del sistema" ->
                                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(exception.message!!))

                                else ->
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse(exception.message ?: "Error al remover privilegios")
                                    )
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Formato de request inválido: ${e.message}")
                    )
                }
            }
        }

        route("/api/appointments") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@get
                }

                val appointments = appService.getUserAppointments(userId)
                call.respond(ListResponse(data = appointments))
            }
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

            get("/past") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@get
                }

                val pastAppointments = appService.getPastAppointments(userId)
                call.respond(ListResponse(data = pastAppointments))
            }

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
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Debe proporcionar al menos un campo para actualizar")
                        )
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

        route("/api/admin/products") {
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

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                if (!isAdmin) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("Solo administradores pueden actualizar productos")
                    )
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
                        request.categoryId == null
                    ) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Debe proporcionar al menos un campo para actualizar")
                        )
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

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val isAdmin = principal?.getClaim("isAdmin", Boolean::class) ?: false

                if (!isAdmin) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("Solo administradores pueden eliminar productos")
                    )
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

        // ===== REWARDS ENDPOINTS =====
        route("/api/rewards") {
            // Obtener todas las recompensas disponibles
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@get
                }

                val rewards = appService.getAllRewards()
                call.respond(ListResponse(data = rewards))
            }

            // Canjear una recompensa
            post("/redeem") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuario no autenticado"))
                    return@post
                }

                try {
                    val request = call.receive<RedeemRequest>()

                    if (request.rewardId <= 0) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID de recompensa inválido"))
                        return@post
                    }

                    val result = appService.redeemReward(userId, request)

                    result.fold(
                        onSuccess = { response ->
                            call.respond(HttpStatusCode.OK, response)
                        },
                        onFailure = { exception ->
                            when (exception.message) {
                                "Usuario no encontrado" ->
                                    call.respond(HttpStatusCode.NotFound, ErrorResponse(exception.message!!))
                                "Recompensa no encontrada" ->
                                    call.respond(HttpStatusCode.NotFound, ErrorResponse(exception.message!!))
                                else ->
                                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(exception.message ?: "Error al canjear recompensa"))
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Formato de request inválido: ${e.message}")
                    )
                }
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
}