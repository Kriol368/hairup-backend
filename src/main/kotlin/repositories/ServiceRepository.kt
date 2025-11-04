package com.hairup.repositories

import com.hairup.models.Service
import com.hairup.models.ServiceInput
import com.hairup.plugins.DatabaseFactory.dbExec
import com.hairup.plugins.Services
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ServiceRepository {

    private fun resultRowToService(row: ResultRow) = Service(
        id = row[Services.id],
        name = row[Services.name],
        description = row[Services.description],
        price = row[Services.price],
        duration = row[Services.duration],
        xp = row[Services.xp]
    )

    suspend fun getAllServices(): List<Service> = dbExec {
        Services.selectAll().map(::resultRowToService)
    }

    suspend fun getServiceById(id: Int): Service? = dbExec {
        Services.select { Services.id eq id }
            .map(::resultRowToService)
            .singleOrNull()
    }

    suspend fun createService(serviceInput: ServiceInput): Service = dbExec {
        val insertStatement = Services.insert {
            it[name] = serviceInput.name
            it[description] = serviceInput.description
            it[price] = serviceInput.price
            it[duration] = serviceInput.duration
            it[xp] = serviceInput.xp
        }

        resultRowToService(insertStatement.resultedValues!!.first())
    }

    suspend fun updateService(id: Int, serviceInput: ServiceInput): Service? = dbExec {
        val updateCount = Services.update({ Services.id eq id }) {
            it[name] = serviceInput.name
            it[description] = serviceInput.description
            it[price] = serviceInput.price
            it[duration] = serviceInput.duration
            it[xp] = serviceInput.xp
        }

        if (updateCount > 0) {
            Services.select { Services.id eq id }.map(::resultRowToService).singleOrNull()
        } else null
    }

    suspend fun deleteService(id: Int): Boolean = dbExec {
        Services.deleteWhere { Services.id eq id } > 0
    }
}