package com.nano.routes

import com.nano.models.*
import com.nano.repository.*
import com.nano.services.MessageEvent
import com.nano.services.RabbitMQService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.messageRoutes() {
    authenticate("auth-jwt") {
        route("/messages") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<SendMessageRequest>()

                if (!ChatMemberRepository.isMember(request.chatId, userId)) {
                    return@post call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("You are not a member of this chat")
                    )
                }

                val messageId = MessageRepository.create(
                    chatId = request.chatId,
                    senderId = userId,
                    content = request.content,
                    type = request.type
                )

                val members = ChatMemberRepository.findMembers(request.chatId)
                val recipientIds = members
                    .map { it[ChatMembers.userId].value }
                    .filter { it != userId }

                RabbitMQService.publishMessage(
                    MessageEvent(
                        messageId = messageId,
                        chatId = request.chatId,
                        senderId = userId,
                        content = request.content,
                        recipientIds = recipientIds
                    )
                )

                val message = MessageRepository.findByChatId(request.chatId, limit = 1).firstOrNull()
                if (message != null) {
                    call.respond(HttpStatusCode.Created, MessageResponse(
                        id = message[Messages.id].value,
                        chatId = message[Messages.chatId].value,
                        senderId = message[Messages.senderId].value,
                        content = message[Messages.content],
                        type = message[Messages.type],
                        createdAt = message[Messages.createdAt].toString(),
                        isRead = message[Messages.isRead]
                    ))
                } else {
                    call.respond(HttpStatusCode.Created, mapOf("id" to messageId))
                }
            }

            get("/{chatId}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val chatId = call.parameters["chatId"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid chat ID")
                    )

                if (!ChatMemberRepository.isMember(chatId, userId)) {
                    return@get call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("You are not a member of this chat")
                    )
                }

                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0

                val messages = MessageRepository.findByChatId(chatId, limit, offset)
                call.respond(messages.map { message ->
                    MessageResponse(
                        id = message[Messages.id].value,
                        chatId = message[Messages.chatId].value,
                        senderId = message[Messages.senderId].value,
                        content = message[Messages.content],
                        type = message[Messages.type],
                        createdAt = message[Messages.createdAt].toString(),
                        isRead = message[Messages.isRead]
                    )
                })
            }

            patch("/{messageId}/read") {
                val principal = call.principal<JWTPrincipal>()
                principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized)

                val messageId = call.parameters["messageId"]?.toLongOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid message ID")
                    )

                MessageRepository.markAsRead(messageId)
                call.respond(HttpStatusCode.OK, mapOf("status" to "marked as read"))
            }
        }
    }
}

