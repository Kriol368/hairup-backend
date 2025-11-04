package com.hairup.repositories

import com.hairup.models.Product
import com.hairup.models.ProductInput
import com.hairup.plugins.DatabaseFactory.dbExec
import com.hairup.plugins.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ProductRepository {

    private fun resultRowToProduct(row: ResultRow) = Product(
        id = row[Products.id],
        name = row[Products.name],
        description = row[Products.description],
        price = row[Products.price],
        image = row[Products.image],
        available = row[Products.available]
    )

    suspend fun getAllProducts(): List<Product> = dbExec {
        Products.selectAll().map(::resultRowToProduct)
    }

    suspend fun getAvailableProducts(): List<Product> = dbExec {
        Products.select { Products.available eq true }.map(::resultRowToProduct)
    }

    suspend fun getProductById(id: Int): Product? = dbExec {
        Products.select { Products.id eq id }
            .map(::resultRowToProduct)
            .singleOrNull()
    }

    suspend fun createProduct(productInput: ProductInput): Product = dbExec {
        val insertStatement = Products.insert {
            it[name] = productInput.name
            it[description] = productInput.description
            it[price] = productInput.price
            it[image] = productInput.image
            it[available] = productInput.available
        }

        resultRowToProduct(insertStatement.resultedValues!!.first())
    }

    suspend fun updateProduct(id: Int, productInput: ProductInput): Product? = dbExec {
        val updateCount = Products.update({ Products.id eq id }) {
            it[name] = productInput.name
            it[description] = productInput.description
            it[price] = productInput.price
            it[image] = productInput.image
            it[available] = productInput.available
        }

        if (updateCount > 0) {
            Products.select { Products.id eq id }.map(::resultRowToProduct).singleOrNull()
        } else null
    }

    suspend fun deleteProduct(id: Int): Boolean = dbExec {
        Products.deleteWhere { Products.id eq id } > 0
    }
}