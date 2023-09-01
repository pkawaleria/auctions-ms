package pl.kawaleria.auctsys.images.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaImageVerificationRequestSender(
        private val kafkaTemplate: KafkaTemplate<String, ImagesVerificationEvent>,
        @Value("\${kafka.topics.image-verification}") val imageVerificationTopic: String
) : ImageVerificationRequestSender

{
    override fun sendToVerification(auctionId: String, imagesToVer: ImagesVerificationEvent) {
        kafkaTemplate.send(imageVerificationTopic, auctionId, imagesToVer)
    }

}