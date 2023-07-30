package pl.kawaleria.auctsys.auctions.domain

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import pl.kawaleria.auctsys.auctions.dto.exceptions.UnsupportedOperationOnAuctionException
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import java.util.*

class AuctionFacadeTest {

    private val auctionFacade: AuctionFacade = AuctionConfiguration().auctionFacadeWithInMemoryRepo()

    @Test
    fun `should accept newly created auction`() {
        // given
        val auctionId: String = thereIsNewAuction()

        // when
        auctionFacade.accept(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ACCEPTED)
    }

    @Test
    fun `should accept archived auction`() {
        // given
        val auctionId = thereIsArchivedAuction()

        // when
        auctionFacade.accept(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ACCEPTED)

    }

    @Test
    fun `should not allow to accept already accepted auction`() {
        // given
        val auctionId = thereIsAcceptedAuction()

        // when then
        Assertions.assertThatThrownBy { auctionFacade.accept(auctionId) }
                .isInstanceOf(UnsupportedOperationOnAuctionException::class.java)
                .hasMessageContaining("Cannot perform acceptance on accepted auction")
    }

    @Test
    fun `should not allow to accept rejected auction`() {
        // given
        val auctionId = thereIsRejectedAuction()

        // when then
        Assertions.assertThatThrownBy { auctionFacade.accept(auctionId) }
                .isInstanceOf(UnsupportedOperationOnAuctionException::class.java)
                .hasMessageContaining("Cannot perform acceptance on rejected auction")
    }

    @Test
    fun `should reject newly created auction`() {
        // given
        val auctionId = thereIsNewAuction()

        // when
        auctionFacade.reject(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.REJECTED)
    }

    @Test
    fun `should reject accepted auction`() {
        // given
        val auctionId = thereIsAcceptedAuction()

        // when
        auctionFacade.reject(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.REJECTED)
    }

    @Test
    fun `should not allow to reject archived auction`() {
        // given
        val auctionId = thereIsArchivedAuction()

        // when then
        Assertions.assertThatThrownBy { auctionFacade.reject(auctionId) }
                .isInstanceOf(UnsupportedOperationOnAuctionException::class.java)
                .hasMessageContaining("Cannot perform rejection on archived auction")
    }

    @Test
    fun `should not allow to reject already rejected auction`() {
        // given
        val auctionId = thereIsRejectedAuction()

        // when then
        Assertions.assertThatThrownBy { auctionFacade.reject(auctionId) }
                .isInstanceOf(UnsupportedOperationOnAuctionException::class.java)
                .hasMessageContaining("Cannot perform rejection on rejected auction")
    }

    @Test
    fun `should archive newly created auction`() {
        // given
        val auctionId = thereIsNewAuction()

        // when
        auctionFacade.archive(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ARCHIVED)
    }

    @Test
    fun `should archive rejected auction`() {
        // given
        val auctionId = thereIsRejectedAuction()

        // when
        auctionFacade.archive(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ARCHIVED)
    }

    @Test
    fun `should archive accepted auction`() {
        // given
        val auctionId = thereIsAcceptedAuction()

        // when
        auctionFacade.archive(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ARCHIVED)
    }

    @Test
    fun `should not allow to archive already archived auction`() {
        // given
        val auctionId = thereIsArchivedAuction()

        // when then
        Assertions.assertThatThrownBy { auctionFacade.archive(auctionId) }
                .isInstanceOf(UnsupportedOperationOnAuctionException::class.java)
                .hasMessageContaining("Cannot perform archiving on archived auction")
    }


    private fun thereIsRejectedAuction(): String {
        return thereIsAuctionAfterOperationOf { auctionId -> auctionFacade.reject(auctionId) }
    }

    private fun thereIsArchivedAuction(): String {
        return thereIsAuctionAfterOperationOf { auctionId -> auctionFacade.archive(auctionId) }
    }

    private fun thereIsAcceptedAuction(): String {
        return thereIsAuctionAfterOperationOf { auctionId -> auctionFacade.accept(auctionId) }
    }

    private fun thereIsNewAuction(): String {
        return thereIsAuctionAfterOperationOf()
    }

    private fun thereIsAuctionAfterOperationOf(action: (String) -> Unit = {}): String {
        val auction = CreateAuctionRequest(
                name = "Adidas shoes",
                category = Category.MODA,
                description = "Breathable sports shoes",
                price = 145.2
        )
        val auctionId: String = auctionFacade.addNewAuction(payload = auction, auctioneerId = "auctioneer-${UUID.randomUUID()}").id!!
        action(auctionId)
        return auctionId
    }
}