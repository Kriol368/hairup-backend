package com.hairup.service

import com.hairup.config.DatabaseConfig
import com.hairup.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDate
import java.time.LocalTime

class AppServiceImpl : AppService {
    private val database: Database = DatabaseConfig.database

    override suspend fun getUserProfile(userId: Int): UserProfileResponse? {
        return database.from(Users).innerJoin(Levels, on = Users.levelId eq Levels.id).select()
            .where { Users.id eq userId }.map { row ->
                UserProfileResponse(
                    id = row[Users.id]!!,
                    name = row[Users.name]!!,
                    email = row[Users.email]!!,
                    xp = row[Users.xp]!!,
                    levelName = row[Levels.name]!!,
                    levelId = row[Users.levelId]!!,
                    phone = row[Users.phone]!!
                )
            }.firstOrNull()
    }

    override suspend fun getNextAppointment(userId: Int): NextAppointmentResponse? {
        val today = LocalDate.now()
        return database.from(Bookings).innerJoin(Services, on = Bookings.serviceId eq Services.id).select().where {
            (Bookings.userId eq userId) and (Bookings.date greaterEq today) and (Bookings.status eq 0)
        }.orderBy(Bookings.date.asc(), Bookings.time.asc()).limit(1).map { row ->
            NextAppointmentResponse(
                id = row[Bookings.id]!!,
                serviceName = row[Services.name]!!,
                date = row[Bookings.date]!!.toString(),
                time = row[Bookings.time]!!.toString()
            )
        }.firstOrNull()
    }

    override suspend fun getPastAppointments(userId: Int,): List<PastAppointmentResponse> {
        val today = LocalDate.now()
        return database.from(Bookings).innerJoin(Services, on = Bookings.serviceId eq Services.id).select().where {
            (Bookings.userId eq userId) and (Bookings.date less today) and (Bookings.status eq 1)
        }.orderBy(Bookings.date.desc()).map { row ->
            PastAppointmentResponse(
                id = row[Bookings.id]!!,
                serviceName = row[Services.name]!!,
                date = row[Bookings.date]!!.toString(),
                xpEarned = row[Services.xp]!!
            )
        }
    }

    override suspend fun getAllLevels(): List<LevelResponse> {
        return database.from(Levels).select().orderBy(Levels.required.asc()).map { row ->
            LevelResponse(
                id = row[Levels.id]!!,
                name = row[Levels.name]!!,
                requiredXp = row[Levels.required]!!,
                reward = row[Levels.reward]!!
            )
        }
    }

    override suspend fun getAllProducts(): List<ProductResponse> {
        return database.from(Products).leftJoin(Categories, on = Products.categoryId eq Categories.id).select()
            .map { row ->
                ProductResponse(
                    id = row[Products.id]!!,
                    name = row[Products.name]!!,
                    description = row[Products.description],
                    price = row[Products.price]!!,
                    image = row[Products.image],
                    available = row[Products.available]!!,
                    points = row[Products.points]!!,
                    categoryName = row[Categories.name],
                    categoryId = row[Products.categoryId]
                )
            }
    }

    override suspend fun getAllServices(): List<ServiceResponse> {
        return database.from(Services).select().map { row ->
            ServiceResponse(
                id = row[Services.id]!!,
                name = row[Services.name]!!,
                description = row[Services.description],
                price = row[Services.price]!!,
                duration = row[Services.duration]!!,
                xpReward = row[Services.xp]!!
            )
        }
    }

    override suspend fun createBooking(userId: Int, request: CreateBookingRequest): Result<Int> {
        return try {
            val serviceExists =
                database.from(Services).select().where { Services.id eq request.serviceId }.totalRecordsInAllPages > 0
            if (!serviceExists) {
                return Result.failure(Exception("El servicio no existe"))
            }
            val bookingDate = LocalDate.parse(request.date)
            val bookingTime = LocalTime.parse(request.time)
            if (bookingDate.isBefore(LocalDate.now())) {
                return Result.failure(Exception("No puedes agendar citas en fechas pasadas"))
            }
            if (!isBookingAvailable(bookingDate, bookingTime, request.serviceId)) {
                return Result.failure(Exception("El horario no está disponible"))
            }
            val bookingId = database.insertAndGenerateKey(Bookings) {
                set(Bookings.userId, userId)
                set(Bookings.serviceId, request.serviceId)
                set(Bookings.date, bookingDate)
                set(Bookings.time, bookingTime)
                set(Bookings.status, 0)
            } as Int
            Result.success(bookingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBooking(
        bookingId: Int,
        userId: Int,
        request: UpdateBookingRequest
    ): Result<Boolean> {
        return try {
            val isPendingBooking = database.from(Bookings).select().where {
                    (Bookings.id eq bookingId) and (Bookings.userId eq userId) and (Bookings.status eq 0)
                }.totalRecordsInAllPages > 0

            if (!isPendingBooking) {
                return Result.failure(Exception("Cita no encontrada, no tienes permisos o no está pendiente"))
            }

            request.date?.let {
                val newDate = LocalDate.parse(it)
                if (newDate.isBefore(LocalDate.now())) {
                    return Result.failure(Exception("No puedes cambiar a una fecha pasada"))
                }
            }

            request.status?.let {
                if (it !in 0..1) {
                    return Result.failure(Exception("Estado inválido"))
                }
            }

            val rowsUpdated = database.update(Bookings) {
                request.date?.let { dateStr -> set(Bookings.date, LocalDate.parse(dateStr)) }
                request.time?.let { timeStr -> set(Bookings.time, LocalTime.parse(timeStr)) }
                request.status?.let { status -> set(Bookings.status, status) }

                where {
                    (Bookings.id eq bookingId) and (Bookings.userId eq userId)
                }
            }

            Result.success(rowsUpdated > 0)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBooking(bookingId: Int, userId: Int): Result<Boolean> {
        return try {
            val rowsDeleted = database.delete(Bookings) {
                (it.id eq bookingId) and (it.userId eq userId)
            }
            if (rowsDeleted == 0) {
                Result.failure(Exception("Cita no encontrada o no tienes permisos"))
            } else {
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createProduct(request: CreateProductRequest): Result<Int> {
        return try {
            request.categoryId?.let { categoryId ->
                val categoryExists =
                    database.from(Categories).select().where { Categories.id eq categoryId }.totalRecordsInAllPages > 0
                if (!categoryExists) {
                    return Result.failure(Exception("La categoría no existe"))
                }
            }
            if (request.price <= 0) {
                return Result.failure(Exception("El precio debe ser mayor a 0"))
            }
            val productId = database.insertAndGenerateKey(Products) {
                set(Products.name, request.name)
                set(Products.description, request.description)
                set(Products.price, request.price)
                set(Products.image, request.image)
                set(Products.available, request.available)
                set(Products.points, request.points)
                set(Products.categoryId, request.categoryId)
            } as Int
            Result.success(productId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(productId: Int, request: UpdateProductRequest): Result<Boolean> {
        return try {
            val productExists =
                database.from(Products).select().where { Products.id eq productId }.totalRecordsInAllPages > 0
            if (!productExists) {
                return Result.failure(Exception("Producto no encontrado"))
            }
            request.categoryId?.let { categoryId ->
                val categoryExists =
                    database.from(Categories).select().where { Categories.id eq categoryId }.totalRecordsInAllPages > 0
                if (!categoryExists) {
                    return Result.failure(Exception("La categoría no existe"))
                }
            }
            request.price?.let { price ->
                if (price <= 0) {
                    return Result.failure(Exception("El precio debe ser mayor a 0"))
                }
            }

            val rowsUpdated = database.update(Products) {
                request.name?.let { set(Products.name, it) }
                request.description?.let { set(Products.description, it) }
                request.price?.let { set(Products.price, it) }
                request.image?.let { set(Products.image, it) }
                request.available?.let { set(Products.available, it) }
                request.points?.let { set(Products.points, it) }
                request.categoryId?.let { set(Products.categoryId, it) }

                where { Products.id eq productId }
            }

            Result.success(rowsUpdated > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: Int): Result<Boolean> {
        return try {
            val rowsDeleted = database.delete(Products) {
                it.id eq productId
            }
            if (rowsDeleted == 0) {
                Result.failure(Exception("Producto no encontrado"))
            } else {
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isBookingAvailable(date: LocalDate, time: LocalTime, serviceId: Int): Boolean {
        val existingBookings = database.from(Bookings).select().where {
            (Bookings.date eq date) and (Bookings.time eq time) and (Bookings.serviceId eq serviceId) and (Bookings.status eq 0)
        }.totalRecordsInAllPages
        return existingBookings == 0
    }
}