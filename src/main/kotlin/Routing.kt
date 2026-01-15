package com.nano

import com.nano.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("RMP Backend API - v1.0")
        }

        get("/health") {
            call.respondText("OK")
        }

        authRoutes()
        userRoutes()
        chatRoutes()
        messageRoutes()
    }
}
