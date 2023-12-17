package pl.kawaleria.auctsys.auctions.domain

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import pl.kawaleria.auctsys.auctions.dto.events.AuctionTextVerificationReceivedEvent


@Component
class KafkaAuctionTextVerificationReceiver(val facade: AuctionFacade) : AuctionTextVerificationReceiver {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @KafkaListener(topics = ["inappropriate-auction-text"])
    override fun handleVerification(@Payload verification: AuctionTextVerificationReceivedEvent) {
        logger.info("Received auction text verification results $verification")

        when {
            verification.isNegative() -> facade.reject(verification.auctionId, verification)
            verification.isAccepted() -> facade.accept(verification.auctionId, verification)
            verification.hasFailed() -> facade.requireManualTextVerification(verification.auctionId, verification)
        }
    }
}