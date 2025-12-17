package com.hairup.service

import com.hairup.model.*

interface AuthService {
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun login(request: LoginRequest): Result<AuthResponse>
}