package com.nano.repository

import com.nano.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object UserRepository {
    fun create(login: String, passwordHash: String): Long? = transaction {
        Users.insertAndGetId {
            it[Users.login] = login
            it[Users.passwordHash] = passwordHash
        }.value
    }

    fun findByLogin(login: String): ResultRow? = transaction {
        Users.selectAll().where { Users.login eq login }.singleOrNull()
    }

    fun findById(id: Long): ResultRow? = transaction {
        Users.selectAll().where { Users.id eq id }.singleOrNull()
    }

    fun updateLastSeen(id: Long) = transaction {
        Users.update({ Users.id eq id }) {
            it[lastSeen] = LocalDateTime.now()
        }
    }

    fun searchByLogin(query: String): List<ResultRow> = transaction {
        Users.selectAll().where { Users.login like "%$query%" }.toList()
    }
}

object ChatRepository {
    fun create(type: String): Long = transaction {
        Chats.insertAndGetId {
            it[Chats.type] = type
        }.value
    }

    fun findById(id: Long): ResultRow? = transaction {
        Chats.selectAll().where { Chats.id eq id }.singleOrNull()
    }

    fun findByUserId(userId: Long): List<ResultRow> = transaction {
        (Chats innerJoin ChatMembers)
            .selectAll()
            .where { ChatMembers.userId eq userId }
            .toList()
    }
}

object ChatMemberRepository {
    fun addMember(chatId: Long, userId: Long, role: String = "member") = transaction {
        ChatMembers.insert {
            it[ChatMembers.chatId] = chatId
            it[ChatMembers.userId] = userId
            it[ChatMembers.role] = role
        }
    }

    fun findMembers(chatId: Long): List<ResultRow> = transaction {
        ChatMembers.selectAll().where { ChatMembers.chatId eq chatId }.toList()
    }

    fun isMember(chatId: Long, userId: Long): Boolean = transaction {
        ChatMembers.selectAll()
            .where { (ChatMembers.chatId eq chatId) and (ChatMembers.userId eq userId) }
            .count() > 0
    }
}

object MessageRepository {
    fun create(chatId: Long, senderId: Long, content: String, type: String = "text"): Long = transaction {
        Messages.insertAndGetId {
            it[Messages.chatId] = chatId
            it[Messages.senderId] = senderId
            it[Messages.content] = content
            it[Messages.type] = type
        }.value
    }

    fun findByChatId(chatId: Long, limit: Int = 50, offset: Long = 0): List<ResultRow> = transaction {
        Messages.selectAll()
            .where { Messages.chatId eq chatId }
            .orderBy(Messages.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .toList()
    }

    fun markAsRead(messageId: Long) = transaction {
        Messages.update({ Messages.id eq messageId }) {
            it[isRead] = true
        }
    }
}

object GroupRepository {
    fun create(chatId: Long, name: String, description: String?): Long = transaction {
        Groups.insertAndGetId {
            it[Groups.chatId] = chatId
            it[Groups.name] = name
            it[Groups.description] = description
        }.value
    }

    fun findByChatId(chatId: Long): ResultRow? = transaction {
        Groups.selectAll().where { Groups.chatId eq chatId }.singleOrNull()
    }
}

