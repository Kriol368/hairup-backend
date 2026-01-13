package com.hairup.service

import com.hairup.model.*

interface AppService {
    suspend fun getUserProfile(userId: Int): UserProfileResponse?

    suspend fun getNextAppointment(userId: Int): NextAppointmentResponse?
    suspend fun getPastAppointments(userId: Int, limit: Int = 3): List<PastAppointmentResponse>

    suspend fun getAllLevels(): List<LevelResponse>

    suspend fun getAllProducts(): List<ProductResponse>

    suspend fun getAllServices(): List<ServiceResponse>
}