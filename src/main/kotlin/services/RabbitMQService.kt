package com.nano.services

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@Serializable
data class MessageEvent(
    val messageId: Long,
    val chatId: Long,
    val senderId: Long,
    val content: String,
    val recipientIds: List<Long>
)

object RabbitMQService {
    private val logger = LoggerFactory.getLogger(RabbitMQService::class.java)
    private lateinit var connection: Connection
    private lateinit var channel: Channel
    private const val MESSAGES_QUEUE = "messages_queue"

    fun init(host: String = "localhost", port: Int = 5672) {
        try {
            val factory = ConnectionFactory().apply {
                this.host = host
                this.port = port
            }
            connection = factory.newConnection()
            channel = connection.createChannel()
            channel.queueDeclare(MESSAGES_QUEUE, true, false, false, null)
            logger.info("RabbitMQ connected to $host:$port")
        } catch (e: Exception) {
            logger.error("Failed to connect to RabbitMQ: ${e.message}")
        }
    }

    fun publishMessage(event: MessageEvent) {
        try {
            val message = Json.encodeToString(event)
            channel.basicPublish("", MESSAGES_QUEUE, null, message.toByteArray())
            logger.debug("Published message to queue: $message")
        } catch (e: Exception) {
            logger.error("Failed to publish message: ${e.message}")
        }
    }

    fun consumeMessages(callback: (MessageEvent) -> Unit) {
        val deliverCallback = DeliverCallback { _, delivery ->
            try {
                val message = String(delivery.body)
                val event = Json.decodeFromString<MessageEvent>(message)
                callback(event)
                channel.basicAck(delivery.envelope.deliveryTag, false)
            } catch (e: Exception) {
                logger.error("Failed to process message: ${e.message}")
            }
        }
        channel.basicConsume(MESSAGES_QUEUE, false, deliverCallback) { _ -> }
    }

    fun close() {
        if (::channel.isInitialized && channel.isOpen) {
            channel.close()
        }
        if (::connection.isInitialized && connection.isOpen) {
            connection.close()
        }
    }
}

