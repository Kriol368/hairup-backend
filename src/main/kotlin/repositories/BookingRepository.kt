package com.hairup.repositories

import com.hairup.models.Booking
import com.hairup.models.BookingInput
import com.hairup.plugins.Bookings
import com.hairup.plugins.DatabaseFactory.dbExec
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDate
import java.time.LocalTime

class BookingRepository {

    private fun resultRowToBooking(row: ResultRow) = Booking(
        id = row[Bookings.id],
        date = row[Bookings.date].toString(),  // LocalDate to String
        time = row[Bookings.time].toString(),  // LocalTime to String
        status = row[Bookings.status],
        userId = row[Bookings.userId],
        serviceId = row[Bookings.serviceId]
    )

    suspend fun getAllBookings(): List<Booking> = dbExec {
        Bookings.selectAll().map(::resultRowToBooking)
    }

    suspend fun getBookingById(id: Int): Booking? = dbExec {
        Bookings.select { Bookings.id eq id }
            .map(::resultRowToBooking)
            .singleOrNull()
    }

    suspend fun getBookingsByUserId(userId: Int): List<Booking> = dbExec {
        Bookings.select { Bookings.userId eq userId }.map(::resultRowToBooking)
    }

    suspend fun getBookingsByServiceId(serviceId: Int): List<Booking> = dbExec {
        Bookings.select { Bookings.serviceId eq serviceId }.map(::resultRowToBooking)
    }

    suspend fun createBooking(bookingInput: BookingInput): Booking = dbExec {
        val insertStatement = Bookings.insert {
            it[date] = LocalDate.parse(bookingInput.date)  // String to LocalDate
            it[time] = LocalTime.parse(bookingInput.time)  // String to LocalTime
            it[status] = bookingInput.status
            it[userId] = bookingInput.userId
            it[serviceId] = bookingInput.serviceId
        }

        resultRowToBooking(insertStatement.resultedValues!!.first())
    }

    suspend fun updateBooking(id: Int, bookingInput: BookingInput): Booking? = dbExec {
        val updateCount = Bookings.update({ Bookings.id eq id }) {
            it[date] = LocalDate.parse(bookingInput.date)
            it[time] = LocalTime.parse(bookingInput.time)
            it[status] = bookingInput.status
            it[userId] = bookingInput.userId
            it[serviceId] = bookingInput.serviceId
        }

        if (updateCount > 0) {
            Bookings.select { Bookings.id eq id }.map(::resultRowToBooking).singleOrNull()
        } else null
    }

    suspend fun deleteBooking(id: Int): Boolean = dbExec {
        Bookings.deleteWhere { Bookings.id eq id } > 0
    }

    suspend fun updateBookingStatus(id: Int, status: Int): Boolean = dbExec {
        Bookings.update({ Bookings.id eq id }) {
            it[Bookings.status] = status
        } > 0
    }
}