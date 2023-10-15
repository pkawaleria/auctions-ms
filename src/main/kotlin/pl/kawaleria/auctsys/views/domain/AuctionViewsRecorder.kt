package pl.kawaleria.auctsys.views.domain

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import pl.kawaleria.auctsys.auctions.dto.events.AuctionViewedEvent
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit


private const val MINUTES_BETWEEN_CONSECUTIVE_VIEWS_FROM_SAME_IP = 5L

open class AuctionViewsRecorder(
    private val auctionViewsPerIpRepository: AuctionViewsPerIpRepository,
    private val auctionViewsRepository: AuctionViewsRepository,
    private val clock: Clock
) {
    @EventListener
    @Async
    open fun recordView(auctionViewedEvent: AuctionViewedEvent) {
        val key = AuctionViewPerIpKey(ipAddress = auctionViewedEvent.ipAddress, auctionId = auctionViewedEvent.auctionId)
        val auctionViewsPerIp: AuctionViewsPerIp = findOrCreate(key)

        if (enoughTimeSinceLastViewPassed(auctionViewsPerIp.lastViewed) || auctionViewsPerIp.isFirstView()) {
            val auctionViews = auctionViewsRepository.findById(auctionViewedEvent.auctionId)
                .orElse(AuctionViews(auctionId = auctionViewedEvent.auctionId))
                .recordNewView()
            auctionViewsRepository.save(auctionViews)
        }
        auctionViewsPerIp.recordNewView(Instant.now(clock))
        auctionViewsPerIpRepository.save(auctionViewsPerIp)
    }

    private fun enoughTimeSinceLastViewPassed(lastViewed: Instant): Boolean {
        val fiveMinutesAgo = Instant.now(clock).minus(MINUTES_BETWEEN_CONSECUTIVE_VIEWS_FROM_SAME_IP, ChronoUnit.MINUTES)
        return lastViewed.isBefore(fiveMinutesAgo)
    }

    private fun findOrCreate(key: AuctionViewPerIpKey): AuctionViewsPerIp {
        return auctionViewsPerIpRepository.findById(key.formatToStringRepresentation())
            .orElseGet {
                auctionViewsPerIpRepository.save(AuctionViewsPerIp(key, Instant.now(clock)))
            }
    }


}