package com.nano.routes

import com.nano.models.*
import com.nano.repository.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chatRoutes() {
    authenticate("auth-jwt") {
        route("/chats") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val chats = ChatRepository.findByUserId(userId)
                call.respond(chats.map { chat ->
                    val chatId = chat[Chats.id].value
                    val members = ChatMemberRepository.findMembers(chatId)
                    ChatResponse(
                        id = chatId,
                        type = chat[Chats.type],
                        createdAt = chat[Chats.createdAt].toString(),
                        members = members.map { it[ChatMembers.userId].value }
                    )
                })
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<CreateChatRequest>()

                if (UserRepository.findById(request.userId) == null) {
                    return@post call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("User not found")
                    )
                }

                val chatId = ChatRepository.create("personal")
                ChatMemberRepository.addMember(chatId, userId)
                ChatMemberRepository.addMember(chatId, request.userId)

                val members = listOf(userId, request.userId)
                call.respond(HttpStatusCode.Created, ChatResponse(
                    id = chatId,
                    type = "personal",
                    createdAt = java.time.LocalDateTime.now().toString(),
                    members = members
                ))
            }

            get("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val chatId = call.parameters["id"]?.toLongOrNull()
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

                val chat = ChatRepository.findById(chatId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Chat not found")
                    )

                val members = ChatMemberRepository.findMembers(chatId)
                call.respond(ChatResponse(
                    id = chatId,
                    type = chat[Chats.type],
                    createdAt = chat[Chats.createdAt].toString(),
                    members = members.map { it[ChatMembers.userId].value }
                ))
            }
        }
    }
}

