package com.nano.routes

import com.nano.models.*
import com.nano.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    authenticate("auth-jwt") {
        route("/users") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val user = UserRepository.findById(userId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("User not found")
                    )

                call.respond(UserResponse(
                    id = user[Users.id].value,
                    login = user[Users.login],
                    createdAt = user[Users.createdAt].toString(),
                    lastSeen = user[Users.lastSeen].toString()
                ))
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid user ID")
                    )

                val user = UserRepository.findById(id)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("User not found")
                    )

                call.respond(UserResponse(
                    id = user[Users.id].value,
                    login = user[Users.login],
                    createdAt = user[Users.createdAt].toString(),
                    lastSeen = user[Users.lastSeen].toString()
                ))
            }

            get("/search") {
                val query = call.request.queryParameters["query"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Query parameter is required")
                    )

                val users = UserRepository.searchByLogin(query)
                call.respond(users.map { user ->
                    UserResponse(
                        id = user[Users.id].value,
                        login = user[Users.login],
                        createdAt = user[Users.createdAt].toString(),
                        lastSeen = user[Users.lastSeen].toString()
                    )
                })
            }
        }
    }
}

