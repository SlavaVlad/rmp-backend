package com.nano

import com.nano.models.*
import com.nano.services.RabbitMQService
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/rmpdb"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )

    log.info("Connected to database: $dbUrl")

    transaction {
        SchemaUtils.create(Users, Chats, ChatMembers, Messages, Groups)
        log.info("Database tables created/verified")
    }

    val rabbitHost = System.getenv("RABBITMQ_HOST") ?: "localhost"
    val rabbitPort = System.getenv("RABBITMQ_PORT")?.toIntOrNull() ?: 5672
    RabbitMQService.init(rabbitHost, rabbitPort)
}
