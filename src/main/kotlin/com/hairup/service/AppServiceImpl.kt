package com.hairup.service

import com.hairup.config.DatabaseConfig
import com.hairup.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDate

class AppServiceImpl : AppService {
    private val database: Database = DatabaseConfig.database

    override suspend fun getUserProfile(userId: Int): UserProfileResponse? {
        return database
            .from(Users)
            .innerJoin(Levels, on = Users.levelId eq Levels.id)
            .select()
            .where { Users.id eq userId }
            .map { row ->
                UserProfileResponse(
                    id = row[Users.id]!!,
                    name = row[Users.name]!!,
                    email = row[Users.email]!!,
                    xp = row[Users.xp]!!,
                    levelName = row[Levels.name]!!,
                    levelId = row[Users.levelId]!!
                )
            }
            .firstOrNull()
    }

    override suspend fun getNextAppointment(userId: Int): NextAppointmentResponse? {
        val today = LocalDate.now()

        return database
            .from(Bookings)
            .innerJoin(Services, on = Bookings.serviceId eq Services.id)
            .select()
            .where {
                (Bookings.userId eq userId) and
                        (Bookings.date greaterEq today) and
                        (Bookings.status eq 0)
            }
            .orderBy(Bookings.date.asc(), Bookings.time.asc())
            .limit(1)
            .map { row ->
                NextAppointmentResponse(
                    id = row[Bookings.id]!!,
                    serviceName = row[Services.name]!!,
                    date = row[Bookings.date]!!.toString(),
                    time = row[Bookings.time]!!.toString()
                )
            }
            .firstOrNull()
    }

    override suspend fun getPastAppointments(userId: Int, limit: Int): List<PastAppointmentResponse> {
        val today = LocalDate.now()

        return database
            .from(Bookings)
            .innerJoin(Services, on = Bookings.serviceId eq Services.id)
            .select()
            .where {
                (Bookings.userId eq userId) and
                        (Bookings.date less today) and
                        (Bookings.status eq 1)
            }
            .orderBy(Bookings.date.desc())
            .limit(limit)
            .map { row ->
                PastAppointmentResponse(
                    id = row[Bookings.id]!!,
                    serviceName = row[Services.name]!!,
                    date = row[Bookings.date]!!.toString(),
                    xpEarned = row[Services.xp]!!
                )
            }
    }

    override suspend fun getAllLevels(): List<LevelResponse> {
        return database
            .from(Levels)
            .select()
            .orderBy(Levels.required.asc())
            .map { row ->
                LevelResponse(
                    id = row[Levels.id]!!,
                    name = row[Levels.name]!!,
                    requiredXp = row[Levels.required]!!,
                    reward = row[Levels.reward]!!
                )
            }
    }

    override suspend fun getAllProducts(): List<ProductResponse> {
        return database
            .from(Products)
            .leftJoin(Categories, on = Products.categoryId eq Categories.id)
            .select()
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
        return database
            .from(Services)
            .select()
            .map { row ->
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
}