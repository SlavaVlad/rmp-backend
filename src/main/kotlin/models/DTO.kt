package com.nano.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val login: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val login: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Long
)

@Serializable
data class UserResponse(
    val id: Long,
    val login: String,
    val createdAt: String,
    val lastSeen: String
)

@Serializable
data class SendMessageRequest(
    val chatId: Long,
    val content: String,
    val type: String = "text"
)

@Serializable
data class MessageResponse(
    val id: Long,
    val chatId: Long,
    val senderId: Long,
    val content: String,
    val type: String,
    val createdAt: String,
    val isRead: Boolean
)

@Serializable
data class ChatResponse(
    val id: Long,
    val type: String,
    val createdAt: String,
    val members: List<Long>
)

@Serializable
data class CreateChatRequest(
    val userId: Long
)

@Serializable
data class ErrorResponse(
    val error: String
)

