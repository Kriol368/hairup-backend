package com.hairup.service

import com.hairup.model.*
import java.time.LocalDate
import java.time.LocalTime

interface AppService {
    suspend fun getUserProfile(userId: Int): UserProfileResponse?
    suspend fun getNextAppointment(userId: Int): NextAppointmentResponse?
    suspend fun getPastAppointments(userId: Int): List<PastAppointmentResponse>
    suspend fun getAllLevels(): List<LevelResponse>
    suspend fun getAllProducts(): List<ProductResponse>
    suspend fun getAllServices(): List<ServiceResponse>

    suspend fun createBooking(userId: Int, request: CreateBookingRequest): Result<Int>
    suspend fun updateBooking(bookingId: Int, userId: Int, request: UpdateBookingRequest): Result<Boolean>
    suspend fun deleteBooking(bookingId: Int, userId: Int): Result<Boolean>

    suspend fun createProduct(request: CreateProductRequest): Result<Int>
    suspend fun updateProduct(productId: Int, request: UpdateProductRequest): Result<Boolean>
    suspend fun deleteProduct(productId: Int): Result<Boolean>

    suspend fun isBookingAvailable(date: LocalDate, time: LocalTime, serviceId: Int): Boolean
}