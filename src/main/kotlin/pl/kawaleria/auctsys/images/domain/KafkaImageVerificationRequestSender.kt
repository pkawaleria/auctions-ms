package pl.kawaleria.auctsys.images.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaImageVerificationRequestSender(
        private val kafkaTemplate: KafkaTemplate<String, Any>,
        @Value("\${kafka.topics.inappropriate-image}") val imageVerificationTopic: String
) : ImageVerificationRequestSender

{
    override fun sendToVerification(auctionId: String, event: VerifyImagesRequestEvent) {
        kafkaTemplate.send(imageVerificationTopic, auctionId, event)
    }

}