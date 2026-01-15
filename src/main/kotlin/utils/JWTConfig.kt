package com.nano.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JWTConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "default-secret-key-change-in-production"
    private val issuer = System.getenv("JWT_ISSUER") ?: "rmpbackend"
    private val audience = System.getenv("JWT_AUDIENCE") ?: "rmp-users"
    val realm = "rmp"
    private val validityInMs = 36_000_00 * 24

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(userId: Long): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(algorithm)

    fun verifier() = JWT.require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}

