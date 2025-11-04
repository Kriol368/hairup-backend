package com.hairup.repositories

import com.hairup.models.User
import com.hairup.models.UserInput
import com.hairup.plugins.DatabaseFactory.dbExec
import com.hairup.plugins.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UserRepository {

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        email = row[Users.email],
        password = row[Users.password],
        name = row[Users.name],
        xp = row[Users.xp],
        levelId = row[Users.levelId]
    )

    suspend fun getAllUsers(): List<User> = dbExec {
        Users.selectAll().map(::resultRowToUser)
    }

    suspend fun getUserById(id: Int): User? = dbExec {
        Users.select { Users.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    suspend fun createUser(userInput: UserInput): User = dbExec {
        val insertStatement = Users.insert {
            it[email] = userInput.email
            it[password] = userInput.password
            it[name] = userInput.name
            it[levelId] = userInput.levelId
        }

        resultRowToUser(insertStatement.resultedValues!!.first())
    }

    suspend fun updateUser(id: Int, userInput: UserInput): User? = dbExec {
        val updateCount = Users.update({ Users.id eq id }) {
            it[email] = userInput.email
            it[password] = userInput.password
            it[name] = userInput.name
            it[levelId] = userInput.levelId
        }

        if (updateCount > 0) {
            Users.select { Users.id eq id }.map(::resultRowToUser).singleOrNull()
        } else null
    }

    suspend fun deleteUser(id: Int): Boolean = dbExec {
        Users.deleteWhere { Users.id eq id } > 0
    }

    suspend fun getUserByEmail(email: String): User? = dbExec {
        Users.select { Users.email eq email }
            .map(::resultRowToUser)
            .singleOrNull()
    }
}