package com.hairup.routes


import com.hairup.models.BookingInput
import com.hairup.repositories.BookingRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookingRoutes(bookingRepository: BookingRepository) {
    route("/bookings") {
        get {
            val bookings = bookingRepository.getAllBookings()
            call.respond(bookings)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }

            val booking = bookingRepository.getBookingById(id)
            if (booking == null) {
                call.respond(HttpStatusCode.NotFound, "Booking not found")
            } else {
                call.respond(booking)
            }
        }

        get("/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid User ID")
                return@get
            }

            val bookings = bookingRepository.getBookingsByUserId(userId)
            call.respond(bookings)
        }

        get("/service/{serviceId}") {
            val serviceId = call.parameters["serviceId"]?.toIntOrNull()
            if (serviceId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Service ID")
                return@get
            }

            val bookings = bookingRepository.getBookingsByServiceId(serviceId)
            call.respond(bookings)
        }

        post {
            try {
                val bookingInput = call.receive<BookingInput>()
                val booking = bookingRepository.createBooking(bookingInput)
                call.respond(HttpStatusCode.Created, booking)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid booking data: ${e.message}")
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@put
            }

            try {
                val bookingInput = call.receive<BookingInput>()
                val updatedBooking = bookingRepository.updateBooking(id, bookingInput)
                if (updatedBooking == null) {
                    call.respond(HttpStatusCode.NotFound, "Booking not found")
                } else {
                    call.respond(updatedBooking)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid booking data: ${e.message}")
            }
        }

        patch("{id}/status") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@patch
            }

            try {
                val status = call.request.queryParameters["status"]?.toIntOrNull()
                if (status == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid status")
                    return@patch
                }

                val updated = bookingRepository.updateBookingStatus(id, status)
                if (updated) {
                    call.respond(HttpStatusCode.OK, "Booking status updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Booking not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid status data: ${e.message}")
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }

            val deleted = bookingRepository.deleteBooking(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Booking deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Booking not found")
            }
        }
    }
}