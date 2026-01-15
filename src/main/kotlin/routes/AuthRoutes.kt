package com.nano.routes

import com.nano.models.*
import com.nano.repository.UserRepository
import com.nano.utils.JWTConfig
import com.nano.utils.PasswordHasher
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()

            if (request.login.isBlank() || request.password.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Login and password are required")
                )
            }

            val existing = UserRepository.findByLogin(request.login)
            if (existing != null) {
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("User with this login already exists")
                )
            }

            val passwordHash = PasswordHasher.hash(request.password)
            val userId = UserRepository.create(request.login, passwordHash)

            if (userId == null) {
                return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Failed to create user")
                )
            }

            val token = JWTConfig.generateToken(userId)
            call.respond(HttpStatusCode.Created, AuthResponse(token, userId))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val user = UserRepository.findByLogin(request.login)
            if (user == null) {
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Invalid credentials")
                )
            }

            val passwordHash = user[Users.passwordHash]
            if (!PasswordHasher.verify(request.password, passwordHash)) {
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Invalid credentials")
                )
            }

            val userId = user[Users.id].value
            UserRepository.updateLastSeen(userId)

            val token = JWTConfig.generateToken(userId)
            call.respond(AuthResponse(token, userId))
        }
    }
}

