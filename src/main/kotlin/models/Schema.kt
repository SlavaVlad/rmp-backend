package com.nano.models

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : LongIdTable("users") {
    val login = varchar("login", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val lastSeen = datetime("last_seen").clientDefault { LocalDateTime.now() }
}

object Chats : LongIdTable("chats") {
    val type = varchar("type", 50)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object ChatMembers : LongIdTable("chat_members") {
    val chatId = reference("chat_id", Chats)
    val userId = reference("user_id", Users)
    val role = varchar("role", 50).default("member")
    val joinedAt = datetime("joined_at").clientDefault { LocalDateTime.now() }
}

object Messages : LongIdTable("messages") {
    val chatId = reference("chat_id", Chats)
    val senderId = reference("sender_id", Users)
    val content = text("content")
    val type = varchar("type", 50).default("text")
    val mediaUrl = varchar("media_url", 500).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val isRead = bool("is_read").default(false)
}

object Groups : LongIdTable("groups") {
    val chatId = reference("chat_id", Chats).uniqueIndex()
    val name = varchar("name", 255)
    val description = text("description").nullable()
}

