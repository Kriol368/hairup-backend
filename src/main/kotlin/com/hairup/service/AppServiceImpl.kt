package com.hairup.service

import com.hairup.config.DatabaseConfig
import com.hairup.model.*
import com.hairup.utils.PasswordHasher
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
                    points = row[Users.points]!!,
                    levelName = row[Levels.name]!!,
                    levelId = row[Users.levelId]!!,
                    phone = row[Users.phone]!!
                )
            }.firstOrNull()
    }

    override suspend fun getUserAppointments(userId: Int): List<AppointmentResponse> {
        return database.from(Bookings)
            .innerJoin(Services, on = Bookings.serviceId eq Services.id)
            .innerJoin(Users, on = Bookings.barberId eq Users.id)
            .select(
                Bookings.id,
                Services.name,
                Services.id,
                Bookings.date,
                Bookings.time,
                Users.name,
                Users.id,
                Bookings.status,
                Services.price,
                Services.duration,
                Services.xp
            )
            .where { Bookings.userId eq userId }
            .orderBy(Bookings.date.desc(), Bookings.time.desc())
            .map { row ->
                AppointmentResponse(
                    id = row[Bookings.id]!!,
                    serviceName = row[Services.name]!!,
                    serviceId = row[Services.id]!!,
                    date = row[Bookings.date]!!.toString(),
                    time = row[Bookings.time]!!.toString(),
                    stylistName = row[Users.name]!!,
                    stylistId = row[Users.id]!!,
                    status = row[Bookings.status]!!,
                    price = row[Services.price]!!,
                    duration = row[Services.duration]!!,
                    xpEarned = if (row[Bookings.status] == 1) row[Services.xp]!! else 0
                )
            }
    }

    override suspend fun getNextAppointment(userId: Int): AppointmentResponse? {
        val today = LocalDate.now()
        return database.from(Bookings)
            .innerJoin(Services, on = Bookings.serviceId eq Services.id)
            .innerJoin(Users, on = Bookings.barberId eq Users.id)
            .select(
                Bookings.id,
                Services.name,
                Services.id,
                Bookings.date,
                Bookings.time,
                Users.name,
                Users.id,
                Bookings.status,
                Services.price,
                Services.duration,
                Services.xp
            )
            .where {
                (Bookings.userId eq userId) and
                        (Bookings.date greaterEq today) and
                        (Bookings.status eq 0)
            }
            .orderBy(Bookings.date.asc(), Bookings.time.asc())
            .limit(1)
            .map { row ->
                AppointmentResponse(
                    id = row[Bookings.id]!!,
                    serviceName = row[Services.name]!!,
                    serviceId = row[Services.id]!!,
                    date = row[Bookings.date]!!.toString(),
                    time = row[Bookings.time]!!.toString(),
                    stylistName = row[Users.name]!!,
                    stylistId = row[Users.id]!!,
                    status = row[Bookings.status]!!,
                    price = row[Services.price]!!,
                    duration = row[Services.duration]!!,
                    xpEarned = 0
                )
            }
            .firstOrNull()
    }

    override suspend fun getPastAppointments(userId: Int): List<AppointmentResponse> {
        val today = LocalDate.now()
        return database.from(Bookings)
            .innerJoin(Services, on = Bookings.serviceId eq Services.id)
            .innerJoin(Users, on = Bookings.barberId eq Users.id)
            .select(
                Bookings.id,
                Services.name,
                Services.id,
                Bookings.date,
                Bookings.time,
                Users.name,
                Users.id,
                Bookings.status,
                Services.price,
                Services.duration,
                Services.xp
            )
            .where {
                (Bookings.userId eq userId) and
                        (Bookings.date less today) and
                        (Bookings.status eq 1)
            }
            .orderBy(Bookings.date.desc())
            .map { row ->
                AppointmentResponse(
                    id = row[Bookings.id]!!,
                    serviceName = row[Services.name]!!,
                    serviceId = row[Services.id]!!,
                    date = row[Bookings.date]!!.toString(),
                    time = row[Bookings.time]!!.toString(),
                    stylistName = row[Users.name]!!,
                    stylistId = row[Users.id]!!,
                    status = row[Bookings.status]!!,
                    price = row[Services.price]!!,
                    duration = row[Services.duration]!!,
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
            val serviceInfo = database.from(Services)
                .select(Services.duration, Services.name)
                .where { Services.id eq request.serviceId }
                .map { row ->
                    Pair(row[Services.duration]!!, row[Services.name]!!)
                }
                .firstOrNull()

            if (serviceInfo == null) {
                return Result.failure(Exception("El servicio no existe"))
            }

            val serviceDuration = serviceInfo.first

            val bookingDate = LocalDate.parse(request.date)
            val bookingTime = LocalTime.parse(request.time)

            if (bookingDate.isBefore(LocalDate.now())) {
                return Result.failure(Exception("No puedes agendar citas en fechas pasadas"))
            }

            if (!isBookingAvailable(bookingDate, bookingTime, request.serviceId)) {
                return Result.failure(Exception("El horario no está disponible"))
            }

            val finalBarberId: Int? = request.barberId

            if (request.barberId != null) {
                val isBarberValid = database.from(Users)
                    .select()
                    .where { (Users.id eq request.barberId) and (Users.admin eq true) }
                    .totalRecordsInAllPages > 0

                if (!isBarberValid) {
                    return Result.failure(Exception("El barbero especificado no existe o no es administrador"))
                }

                val barberBookings = database.from(Bookings)
                    .innerJoin(Services, on = Bookings.serviceId eq Services.id)
                    .select(
                        Bookings.time,
                        Services.duration
                    )
                    .where {
                        (Bookings.barberId eq request.barberId) and
                                (Bookings.date eq bookingDate) and
                                (Bookings.status eq 0)
                    }
                    .map { row ->
                        Pair(
                            row[Bookings.time]!!,
                            row[Services.duration]!!
                        )
                    }

                val newBookingSlots = getSlotsForDuration(bookingTime.toString(), serviceDuration, 30)

                var hasConflict = false
                var conflictingService = ""

                for ((existingTime, existingDuration) in barberBookings) {
                    val existingSlots = getSlotsForDuration(existingTime.toString(), existingDuration, 30)

                    val intersection = newBookingSlots.intersect(existingSlots.toSet())
                    if (intersection.isNotEmpty()) {
                        hasConflict = true
                        conflictingService = "conflicto con cita existente"
                        break
                    }
                }

                if (hasConflict) {
                    return Result.failure(Exception("El barbero no está disponible en ese horario ($conflictingService)"))
                }
            }

            val bookingId = database.insertAndGenerateKey(Bookings) {
                set(Bookings.userId, userId)
                set(Bookings.serviceId, request.serviceId)
                set(Bookings.date, bookingDate)
                set(Bookings.time, bookingTime)
                set(Bookings.status, 0)
                set(Bookings.barberId, finalBarberId)
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

    override suspend fun getAdminUsers(): List<AdminUserResponse> {
        return database.from(Users)
            .select()
            .where { Users.admin eq true }
            .orderBy(Users.name.asc())
            .map { row ->
                AdminUserResponse(
                    id = row[Users.id]!!,
                    name = row[Users.name]!!,
                    email = row[Users.email]!!,
                    phone = row[Users.phone]
                )
            }
    }

    override suspend fun getAllUsers(): List<AllUsersResponse> {
        return database.from(Users)
            .select()
            .orderBy(Users.name.asc())
            .map { row ->
                AllUsersResponse(
                    id = row[Users.id]!!,
                    name = row[Users.name]!!,
                    email = row[Users.email]!!,
                    phone = row[Users.phone],
                    xp = row[Users.xp]!!,
                    points = row[Users.points]!!,
                    admin = row[Users.admin]!!,
                    levelId = row[Users.levelId]!!,
                    created = row[Users.created]!!.toString()
                )
            }
    }

    override suspend fun makeUserAdmin(userId: Int): Result<Boolean> {
        return try {
            val userExists = database.from(Users)
                .select()
                .where { Users.id eq userId }
                .totalRecordsInAllPages > 0

            if (!userExists) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val rowsUpdated = database.update(Users) {
                set(Users.admin, true)
                where { Users.id eq userId }
            }

            if (rowsUpdated > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("No se pudo actualizar el usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeUserAdmin(userId: Int): Result<Boolean> {
        return try {
            val userExists = database.from(Users)
                .select()
                .where { Users.id eq userId }
                .totalRecordsInAllPages > 0

            if (!userExists) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val adminCount = database.from(Users)
                .select()
                .where { Users.admin eq true }
                .totalRecordsInAllPages

            val isTargetAdmin = database.from(Users)
                .select()
                .where { (Users.id eq userId) and (Users.admin eq true) }
                .totalRecordsInAllPages > 0

            if (isTargetAdmin && adminCount <= 1) {
                return Result.failure(Exception("No puedes quitar el último administrador del sistema"))
            }

            val rowsUpdated = database.update(Users) {
                set(Users.admin, false)
                where { Users.id eq userId }
            }

            if (rowsUpdated > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("No se pudo actualizar el usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(userId: Int, request: UpdateProfileRequest): Result<UserProfileResponse> {
        return try {
            val userExists = database.from(Users)
                .select()
                .where { Users.id eq userId }
                .totalRecordsInAllPages > 0

            if (!userExists) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            if (!request.email.isNullOrBlank()) {
                val emailExists = database.from(Users)
                    .select()
                    .where { (Users.email eq request.email) and (Users.id neq userId) }
                    .totalRecordsInAllPages > 0

                if (emailExists) {
                    return Result.failure(Exception("El email ya está siendo utilizado por otro usuario"))
                }
            }

            val updateBuilder = database.update(Users) {
                request.name?.let { set(Users.name, it) }
                request.email?.let { set(Users.email, it) }
                request.phone?.let { set(Users.phone, it) }

                where { Users.id eq userId }
            }

            if (updateBuilder == 0) {
                return Result.failure(Exception("No se pudo actualizar el perfil"))
            }

            val updatedProfile = getUserProfile(userId)
            if (updatedProfile == null) {
                Result.failure(Exception("Perfil actualizado pero no se pudo recuperar la información"))
            } else {
                Result.success(updatedProfile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(userId: Int, request: ChangePasswordRequest): Result<Boolean> {
        return try {
            if (request.newPassword.isBlank()) {
                return Result.failure(Exception("La nueva contraseña no puede estar vacía"))
            }

            if (request.newPassword.length < 6) {
                return Result.failure(Exception("La nueva contraseña debe tener al menos 6 caracteres"))
            }

            if (request.currentPassword == request.newPassword) {
                return Result.failure(Exception("La nueva contraseña debe ser diferente a la actual"))
            }

            val userRow = database.from(Users)
                .select(Users.password)
                .where { Users.id eq userId }
                .map { row ->
                    row[Users.password]
                }
                .firstOrNull()

            if (userRow == null) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            if (!PasswordHasher.verifyPassword(request.currentPassword, userRow)) {
                return Result.failure(Exception("Contraseña actual incorrecta"))
            }

            val hashedNewPassword = PasswordHasher.hashPassword(request.newPassword)

            val rowsUpdated = database.update(Users) {
                set(Users.password, hashedNewPassword)
                where { Users.id eq userId }
            }

            if (rowsUpdated > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("No se pudo cambiar la contraseña"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getBarberAvailableHours(barberId: Int, date: String): Result<BarberAvailabilityResponse> {
        return try {
            val barber = database.from(Users)
                .select(Users.name)
                .where { (Users.id eq barberId) and (Users.admin eq true) }
                .map { row -> row[Users.name] }
                .firstOrNull()

            if (barber == null) {
                return Result.failure(Exception("Barbero no encontrado o no es administrador"))
            }

            val bookingDate = LocalDate.parse(date)

            val allSlots = generateTimeSlots("09:00", "18:00", 30)

            val barberBookings = database.from(Bookings)
                .innerJoin(Services, on = Bookings.serviceId eq Services.id)
                .select(
                    Bookings.time,
                    Services.id,
                    Services.name,
                    Services.duration
                )
                .where {
                    (Bookings.barberId eq barberId) and
                            (Bookings.date eq bookingDate) and
                            (Bookings.status eq 0)
                }
                .map { row ->
                    BookingInfo(
                        time = row[Bookings.time]!!,
                        serviceId = row[Services.id]!!,
                        serviceName = row[Services.name]!!,
                        duration = row[Services.duration]!!
                    )
                }

            val bookedSlots = mutableSetOf<String>()

            barberBookings.forEach { booking ->
                val startTime = booking.time
                val duration = booking.duration

                val slotsToBook = getSlotsForDuration(startTime.toString(), duration, 30)
                bookedSlots.addAll(slotsToBook)
            }

            val availableSlots = allSlots.map { slotTime ->
                val isBooked = bookedSlots.contains(slotTime)
                val booking = if (isBooked) {
                    barberBookings.find { booking ->
                        val slots = getSlotsForDuration(booking.time.toString(), booking.duration, 30)
                        slots.contains(slotTime)
                    }
                } else null

                TimeSlot(
                    time = slotTime,
                    available = !isBooked,
                    serviceId = booking?.serviceId,
                    serviceName = booking?.serviceName,
                    duration = booking?.duration
                )
            }

            val response = BarberAvailabilityResponse(
                barberId = barberId,
                barberName = barber,
                date = date,
                availableSlots = availableSlots
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class BookingInfo(
        val time: LocalTime,
        val serviceId: Int,
        val serviceName: String,
        val duration: Int
    )

    private fun generateTimeSlots(startTime: String, endTime: String, intervalMinutes: Int): List<String> {
        val slots = mutableListOf<String>()
        var current = LocalTime.parse(startTime)
        val end = LocalTime.parse(endTime)

        while (current.isBefore(end) || current.equals(end)) {
            slots.add(current.toString())
            current = current.plusMinutes(intervalMinutes.toLong())
        }

        return slots
    }

    private fun getSlotsForDuration(startTime: String, durationMinutes: Int, intervalMinutes: Int): List<String> {
        val slots = mutableListOf<String>()
        var current = LocalTime.parse(startTime)
        val endTime = current.plusMinutes(durationMinutes.toLong())

        while (current.isBefore(endTime)) {
            slots.add(current.toString())
            current = current.plusMinutes(intervalMinutes.toLong())
        }

        return slots
    }


    override suspend fun getBookingsByBarber(barberId: Int): List<AppointmentDetailResponse> {
        val today = LocalDate.now()

        return database.from(Bookings)
            .innerJoin(Services, on = Bookings.serviceId eq Services.id)
            .innerJoin(Users, on = Bookings.userId eq Users.id)
            .select()
            .where {
                (Bookings.barberId eq barberId) and
                        (Bookings.date greaterEq today)
            }
            .orderBy(Bookings.date.asc(), Bookings.time.asc())
            .map { row ->
                AppointmentDetailResponse(
                    id = row[Bookings.id]!!,
                    serviceName = row[Services.name]!!,
                    clientName = row[Users.name]!!,
                    clientPhone = row[Users.phone],
                    date = row[Bookings.date]!!.toString(),
                    time = row[Bookings.time]!!.toString(),
                    status = row[Bookings.status]!!
                )
            }
    }

    override suspend fun getAllRewards(): List<RewardResponse> {
        return database.from(Rewards)
            .select()
            .where { Rewards.available eq true }
            .orderBy(Rewards.pointsCost.asc())
            .map { row ->
                RewardResponse(
                    id = row[Rewards.id]!!,
                    name = row[Rewards.name]!!,
                    description = row[Rewards.description],
                    pointsCost = row[Rewards.pointsCost]!!,
                    minLevelId = row[Rewards.minLevelId]!!,
                    available = row[Rewards.available]!!
                )
            }
    }

    override suspend fun redeemReward(userId: Int, request: RedeemRequest): Result<RedeemResponse> {
        return try {
            val user = database.from(Users)
                .select(Users.xp, Users.points, Users.levelId)
                .where { Users.id eq userId }
                .map { row ->
                    Triple(row[Users.xp]!!, row[Users.points]!!, row[Users.levelId]!!)
                }
                .firstOrNull()

            if (user == null) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val (userXp, userPoints, userLevelId) = user

            val reward = database.from(Rewards)
                .select()
                .where { Rewards.id eq request.rewardId }
                .map { row ->
                    RewardResponse(
                        id = row[Rewards.id]!!,
                        name = row[Rewards.name]!!,
                        description = row[Rewards.description],
                        pointsCost = row[Rewards.pointsCost]!!,
                        minLevelId = row[Rewards.minLevelId]!!,
                        available = row[Rewards.available]!!
                    )
                }
                .firstOrNull()

            if (reward == null) {
                return Result.failure(Exception("Recompensa no encontrada"))
            }

            if (!reward.available) {
                return Result.failure(Exception("Esta recompensa no está disponible"))
            }

            if (userLevelId < reward.minLevelId) {
                return Result.failure(Exception("Necesitas nivel ${reward.minLevelId} para canjear esta recompensa"))
            }

            if (userPoints < reward.pointsCost) {
                return Result.failure(Exception("No tienes suficientes puntos. Te faltan ${reward.pointsCost - userPoints} puntos"))
            }

            val newPoints = userPoints - reward.pointsCost
            val rowsUpdated = database.update(Users) {
                set(Users.points, newPoints)
                where { Users.id eq userId }
            }

            if (rowsUpdated == 0) {
                return Result.failure(Exception("Error al actualizar puntos"))
            }

            Result.success(
                RedeemResponse(
                    success = true,
                    message = "¡Recompensa canjeada con éxito!",
                    newPoints = newPoints
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addXpAndPoints(userId: Int, xpToAdd: Int): Result<Boolean> {
        return try {
            val rowsUpdated = database.update(Users) {
                set(Users.xp, Users.xp + xpToAdd)
                set(Users.points, Users.points + xpToAdd)
                where { Users.id eq userId }
            }

            if (rowsUpdated > 0) {
                updateUserLevel(userId)
                Result.success(true)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateUserLevel(userId: Int) {
        val user = database.from(Users)
            .select(Users.xp, Users.levelId)
            .where { Users.id eq userId }
            .map { row -> Pair(row[Users.xp]!!, row[Users.levelId]!!) }
            .firstOrNull() ?: return

        val (userXp, currentLevelId) = user

        val newLevelId = database.from(Levels)
            .select(Levels.id)
            .where { Levels.required lessEq userXp }
            .orderBy(Levels.required.desc())
            .limit(1)
            .map { it[Levels.id]!! }
            .firstOrNull() ?: currentLevelId

        if (newLevelId != currentLevelId) {
            database.update(Users) {
                set(Users.levelId, newLevelId)
                where { Users.id eq userId }
            }
        }
    }
    override suspend fun getAllCategories(): List<CategoryResponse> {
        return database.from(Categories)
            .select()
            .orderBy(Categories.name.asc())
            .map { row ->
                CategoryResponse(
                    id = row[Categories.id]!!,
                    name = row[Categories.name]!!
                )
            }
    }
}