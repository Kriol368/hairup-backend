package com.hairup.service

import com.hairup.config.DatabaseConfig
import com.hairup.config.JwtConfig
import com.hairup.model.*
import com.hairup.utils.PasswordHasher
import org.ktorm.dsl.*
import java.time.LocalDate

class AuthServiceImpl : AuthService {
    private val database = DatabaseConfig.database

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            // Check if user already exists
            val existingUser = database.from(Users)
                .select()
                .where { Users.email eq request.email }
                .map { it[Users.email] }
                .firstOrNull()

            if (existingUser != null) {
                return Result.failure(Exception("User with this email already exists"))
            }

            // Hash password
            val hashedPassword = PasswordHasher.hashPassword(request.password)

            // Insert new user
            val userId = database.insertAndGenerateKey(Users) {
                set(Users.email, request.email)
                set(Users.password, hashedPassword)
                set(Users.name, request.name)
                set(Users.phone, request.phone)
                set(Users.created, LocalDate.now())
                set(Users.xp, 0)
                set(Users.admin, false)
                set(Users.levelId, 1)
            } as Int

            // Get the created user
            val user = getUserById(userId) ?: throw Exception("Failed to create user")

            // Generate token
            val token = JwtConfig.makeToken(userId, user.email, user.admin)

            Result.success(AuthResponse(token, user.toResponse()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            // Find user by email
            val user = database.from(Users)
                .select()
                .where { Users.email eq request.email }
                .map { row -> createUserEntity(row) }
                .firstOrNull()

            if (user == null) {
                return Result.failure(Exception("Invalid email or password"))
            }

            // Verify password
            if (!PasswordHasher.verifyPassword(request.password, user.password)) {
                return Result.failure(Exception("Invalid email or password"))
            }

            // Generate token
            val token = JwtConfig.makeToken(user.id, user.email, user.admin)

            Result.success(AuthResponse(token, user.toResponse()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getUserById(id: Int): User? {
        return database.from(Users)
            .select()
            .where { Users.id eq id }
            .map { row -> createUserEntity(row) }
            .firstOrNull()
    }

    private fun createUserEntity(row: QueryRowSet): User {
        return User {
            id = row[Users.id]!!
            email = row[Users.email]!!
            password = row[Users.password]!!
            name = row[Users.name]!!
            xp = row[Users.xp]!!
            admin = row[Users.admin]!!
            phone = row[Users.phone]
            created = row[Users.created]!!
            levelId = row[Users.levelId]!!
        }
    }

    private fun User.toResponse(): UserResponse {
        return UserResponse(
            id = this.id,
            email = this.email,
            name = this.name,
            xp = this.xp,
            admin = this.admin,
            phone = this.phone,
            created = this.created.toString(),
            levelId = this.levelId
        )
    }
}