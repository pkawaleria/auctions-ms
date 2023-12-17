package pl.kawaleria.auctsys.auctions.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import pl.kawaleria.auctsys.auctions.dto.events.VerifyAuctionTextRequestEvent

@Component
class KafkaAuctionMessageSender(
    val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${kafka.topics.text-content.verification.request}") val kafkaInappropriateContentTopic: String
) : AuctionMessageSender {
    override fun sendToVerification(event: VerifyAuctionTextRequestEvent) {
        kafkaTemplate.send(kafkaInappropriateContentTopic, event.auctionId, event)
    }
}