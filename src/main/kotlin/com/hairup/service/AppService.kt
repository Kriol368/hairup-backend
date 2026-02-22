package com.hairup.service

import com.hairup.model.*
import java.time.LocalDate
import java.time.LocalTime

interface AppService {
    suspend fun getUserProfile(userId: Int): UserProfileResponse?
    suspend fun getUserAppointments(userId: Int): List<AppointmentResponse>
    suspend fun getNextAppointment(userId: Int): AppointmentResponse?
    suspend fun getPastAppointments(userId: Int): List<AppointmentResponse>
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
    suspend fun getAdminUsers(): List<AdminUserResponse>
    suspend fun getAllUsers(): List<AllUsersResponse>
    suspend fun makeUserAdmin(userId: Int): Result<Boolean>
    suspend fun removeUserAdmin(userId: Int): Result<Boolean>
    suspend fun updateUserProfile(userId: Int, request: UpdateProfileRequest): Result<UserProfileResponse>
    suspend fun changePassword(userId: Int, request: ChangePasswordRequest): Result<Boolean>
    suspend fun getBarberAvailableHours(barberId: Int, date: String): Result<BarberAvailabilityResponse>
    suspend fun getBookingsByBarber(barberId: Int): List<AppointmentDetailResponse>
    suspend fun redeemReward(userId: Int, request: RedeemRequest): Result<RedeemResponse>
    suspend fun getAllRewards(): List<RewardResponse>
    suspend fun addXpAndPoints(userId: Int, xpToAdd: Int): Result<Boolean>
    suspend fun getAllCategories(): List<CategoryResponse>
    suspend fun createCategory(request: CreateCategoryRequest): Result<Int>
    suspend fun updateCategory(id: Int, request: UpdateCategoryRequest): Result<Boolean>
    suspend fun deleteCategory(id: Int): Result<Boolean>
}