package com.hairup.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val DEFAULT_SECRET = "your-secret-key-change-this-in-production"
    private const val DEFAULT_ISSUER = "hairup-api"
    private const val DEFAULT_AUDIENCE = "hairup-mobile-app"

    private val secret = System.getenv("JWT_SECRET") ?: DEFAULT_SECRET
    private val issuer = System.getenv("JWT_ISSUER") ?: DEFAULT_ISSUER
    private val audience = System.getenv("JWT_AUDIENCE") ?: DEFAULT_AUDIENCE

    private val algorithm = Algorithm.HMAC256(secret)

    // 16 hours in milliseconds
    private const val VALIDITY = 16 * 60 * 60 * 1000L

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun makeToken(userId: Int, email: String, isAdmin: Boolean): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("isAdmin", isAdmin)
            .withExpiresAt(getExpiration())
            .sign(algorithm)
    }

    private fun getExpiration() = Date(System.currentTimeMillis() + VALIDITY)
}