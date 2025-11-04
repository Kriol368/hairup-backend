package com.hairup.repositories

import com.hairup.models.Level
import com.hairup.models.LevelInput
import com.hairup.plugins.DatabaseFactory.dbExec
import com.hairup.plugins.Levels
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class LevelRepository {

    private fun resultRowToLevel(row: ResultRow) = Level(
        id = row[Levels.id],
        name = row[Levels.name],
        required = row[Levels.required],
        reward = row[Levels.reward]
    )

    suspend fun getAllLevels(): List<Level> = dbExec {
        Levels.selectAll().map(::resultRowToLevel)
    }

    suspend fun getLevelById(id: Int): Level? = dbExec {
        Levels.select { Levels.id eq id }
            .map(::resultRowToLevel)
            .singleOrNull()
    }

    suspend fun createLevel(levelInput: LevelInput): Level = dbExec {
        val insertStatement = Levels.insert {
            it[name] = levelInput.name
            it[required] = levelInput.required
            it[reward] = levelInput.reward
        }

        resultRowToLevel(insertStatement.resultedValues!!.first())
    }

    suspend fun updateLevel(id: Int, levelInput: LevelInput): Level? = dbExec {
        val updateCount = Levels.update({ Levels.id eq id }) {
            it[name] = levelInput.name
            it[required] = levelInput.required
            it[reward] = levelInput.reward
        }

        if (updateCount > 0) {
            Levels.select { Levels.id eq id }.map(::resultRowToLevel).singleOrNull()
        } else null
    }

    suspend fun deleteLevel(id: Int): Boolean = dbExec {
        Levels.deleteWhere { Levels.id eq id } > 0
    }
}