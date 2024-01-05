package pl.kawaleria.auctsys.views.domain

import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Test
import pl.kawaleria.auctsys.auctions.dto.events.AuctionViewedEvent
import pl.kawaleria.auctsys.views.dto.AuctionViewsFromIpResponse
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class AuctionViewsModuleTest {

    val clock = MutableClock(LocalDateTime.of(2023, 10, 10, 23, 0)
        .toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
    private val module = AuctionViewsConfiguration().auctionViewsModuleWithInMemoryRepositories(clock = clock)
    private val auctionViewsQueryFacade = module.first
    private val auctionViewsRecorder = module.second

    @Test
    fun `should count auction views per ip`() {
        // given
        val auctionViewedFromIp = AuctionViewedEvent(
            ipAddress = "127.90.213.90",
            auctionId = "auction-id"
        )

        // when
        val secondsBetweenConsecutiveViews = 30
        val consecutiveViews = 5
        auctionViewedNTimesWithIntervalOfSeconds(
            auctionViewedFromIp = auctionViewedFromIp,
            n = consecutiveViews,
            seconds = secondsBetweenConsecutiveViews
        )


        // then
        val recentViewsFromIpAddressForAuction = auctionViewsQueryFacade.getRecentViewsFromIpAddressForAuction(
            auctionViewedFromIp.ipAddress,
            auctionViewedFromIp.auctionId
        )

        Assertions.assertThat(recentViewsFromIpAddressForAuction).extracting(
            { it.auctionId },
            { it.ipAddress },
            { it.viewCounter }
        ).containsExactly(
            auctionViewedFromIp.auctionId,
            auctionViewedFromIp.ipAddress,
            consecutiveViews
        )

        val differences = recentViewsFromIpAddressForAuction.viewsTimestamps.zipWithNext()
            .map { (first, second) -> Duration.between(first, second) }

        println(recentViewsFromIpAddressForAuction.viewsTimestamps)

        Assertions.assertThat(differences)
            .allMatch { it == Duration.ofSeconds(secondsBetweenConsecutiveViews.toLong()) }


        Assertions.assertThat(auctionViewsQueryFacade.getRecentViewsFromIpAddress(auctionViewedFromIp.ipAddress))
            .containsExactly(auctionViewedFromIp.auctionId)
    }
    @Test
    fun `should get most viewed auctions from ip address`() {
        // given
        val ipAddress = "127.90.213.90"
        val firstAuctionViewedFromIp = AuctionViewedEvent(
            ipAddress = ipAddress,
            auctionId = "auction-id-1"
        )
        val secondAuctionViewedFromIp = AuctionViewedEvent(
            ipAddress = ipAddress,
            auctionId = "auction-id-2"
        )
        val thirdAuctionViewedFromIp = AuctionViewedEvent(
            ipAddress = ipAddress,
            auctionId = "auction-id-3"
        )
        val secondsBetweenConsecutiveViews = 30
        val firstAuctionConsecutiveViews = 10
        val secondAuctionConsecutiveViews = 20
        val thirdAuctionConsecutiveViews = 30

        auctionViewedNTimesWithIntervalOfSeconds(auctionViewedFromIp = firstAuctionViewedFromIp,
            n = firstAuctionConsecutiveViews,
            seconds = secondsBetweenConsecutiveViews
        )
        auctionViewedNTimesWithIntervalOfSeconds(auctionViewedFromIp = secondAuctionViewedFromIp,
            n = secondAuctionConsecutiveViews,
            seconds = secondsBetweenConsecutiveViews
        )
        auctionViewedNTimesWithIntervalOfSeconds(auctionViewedFromIp = thirdAuctionViewedFromIp,
            n = thirdAuctionConsecutiveViews,
            seconds = secondsBetweenConsecutiveViews
        )

        // when
        val recentViewsFromIpAddressForAuction = auctionViewsQueryFacade
            .getMostViewedAuctionsFromIpAddress(ipAddress = ipAddress)

        // then
        Assertions.assertThat(recentViewsFromIpAddressForAuction)
            .isSortedAccordingTo(Comparator.comparingInt<AuctionViewsFromIpResponse> { it.viewCounter }.reversed())

        Assertions.assertThat(recentViewsFromIpAddressForAuction)
            .extracting(
                { it.auctionId },
                { it.viewCounter }
            ).containsExactly(
                Tuple.tuple("auction-id-3", thirdAuctionConsecutiveViews),
                Tuple.tuple("auction-id-2", secondAuctionConsecutiveViews),
                Tuple.tuple("auction-id-1", firstAuctionConsecutiveViews)
            )
    }

    @Test
    fun `should increment auction view counter when enough time passed since last view from the same ip address`() {
        // given
        val auctionViewedFromIp = AuctionViewedEvent(
            ipAddress = "127.90.213.90",
            auctionId = "auction-id"
        )

        // when
        auctionViewsRecorder.recordView(auctionViewedFromIp)

        clock.advanceBy(duration = Duration.ofMinutes(6))

        auctionViewsRecorder.recordView(auctionViewedFromIp)

        // then
        Assertions.assertThat(auctionViewsQueryFacade.getAuctionViews(auctionViewedFromIp.auctionId))
            .isEqualTo(2)

        Assertions.assertThat(auctionViewsQueryFacade.getRecentViewsFromIpAddress(auctionViewedFromIp.ipAddress))
            .containsExactly(auctionViewedFromIp.auctionId)
    }

    @Test
    fun `should increment auction view counter when auction is viewed from different ips`() {
        // given
        val auctionUnderTest = "auction-id"
        val auctionViewedFromOneIp = AuctionViewedEvent(
            ipAddress = "127.90.213.90",
            auctionId = auctionUnderTest
        )

        val auctionViewedFromSecondIp = AuctionViewedEvent(
            ipAddress = "127.90.213.91",
            auctionId = auctionUnderTest
        )

        // when
        auctionViewsRecorder.recordView(auctionViewedFromOneIp)
        auctionViewsRecorder.recordView(auctionViewedFromSecondIp)

        // then
        Assertions.assertThat(auctionViewsQueryFacade.getAuctionViews(auctionUnderTest))
            .isEqualTo(2)
    }

    @Test
    fun `should not increment auction view counter when too less time passed since last view from the same ip address`() {
        // given
        val auctionViewedFromIp = AuctionViewedEvent(
            ipAddress = "127.90.213.90",
            auctionId = "auction-id"
        )

        // when
        auctionViewsRecorder.recordView(auctionViewedFromIp)

        clock.advanceBy(duration = Duration.ofMinutes(3))

        auctionViewsRecorder.recordView(auctionViewedFromIp)

        // then
        Assertions.assertThat(auctionViewsQueryFacade.getAuctionViews(auctionViewedFromIp.auctionId)).isEqualTo(1)
    }


    @Test
    fun `should return view counter for multiple auctions`() {
        // given
        val ipAddress = "127.90.213.90"
        val firstAuctionViewedFromIp = AuctionViewedEvent(
            ipAddress = ipAddress,
            auctionId = "auction-id-1"
        )

        val secondAuctionViewedFromIp = AuctionViewedEvent(
            ipAddress = ipAddress,
            auctionId = "auction-id-2"
        )

        val thirdAuctionViewedFromIp = AuctionViewedEvent(
            ipAddress = ipAddress,
            auctionId = "auction-id-3"
        )

        val secondsBetweenConsecutiveViews = 3600
        val firstAuctionConsecutiveViews = 10
        val secondAuctionConsecutiveViews = 20
        val thirdAuctionConsecutiveViews = 30

        auctionViewedNTimesWithIntervalOfSeconds(
            auctionViewedFromIp = firstAuctionViewedFromIp,
            n = firstAuctionConsecutiveViews,
            seconds = secondsBetweenConsecutiveViews
        )
        auctionViewedNTimesWithIntervalOfSeconds(
            auctionViewedFromIp = secondAuctionViewedFromIp,
            n = secondAuctionConsecutiveViews,
            seconds = secondsBetweenConsecutiveViews
        )
        auctionViewedNTimesWithIntervalOfSeconds(
            auctionViewedFromIp = thirdAuctionViewedFromIp,
            n = thirdAuctionConsecutiveViews,
            seconds = secondsBetweenConsecutiveViews
        )

        // when

        val auctionsViews = auctionViewsQueryFacade.getAuctionsViews(
            listOf("auction-id-1", "auction-id-2", "auction-id-3"))

        // then
        SoftAssertions().apply {
            assertThat(auctionsViews.getViewsOfAuction("auction-id-1"))
                .`as`("Views for auction-id-1")
                .isEqualTo(firstAuctionConsecutiveViews.toLong())

            assertThat(auctionsViews.getViewsOfAuction("auction-id-2"))
                .`as`("Views for auction-id-2")
                .isEqualTo(secondAuctionConsecutiveViews.toLong())

            assertThat(auctionsViews.getViewsOfAuction("auction-id-3"))
                .`as`("Views for auction-id-3")
                .isEqualTo(thirdAuctionConsecutiveViews.toLong())

            assertAll()
        }
    }


    private fun auctionViewedNTimesWithIntervalOfSeconds(
        auctionViewedFromIp: AuctionViewedEvent,
        n: Int,
        seconds: Int
    ) {
        for (i in 0 until n) {
            clock.advanceBy(duration = Duration.ofSeconds(seconds.toLong()))
            auctionViewsRecorder.recordView(auctionViewedFromIp)
        }
    }
}
