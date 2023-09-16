package pl.kawaleria.auctsys.auctions.domain

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import pl.kawaleria.auctsys.auctions.dto.exceptions.AuctionNotFoundException
import pl.kawaleria.auctsys.auctions.dto.exceptions.UnsupportedOperationOnAuctionException
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.categories.domain.CategoryConfiguration
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.categories.dto.request.CategoryCreateRequest
import pl.kawaleria.auctsys.categories.dto.response.CategoryResponse
import pl.kawaleria.auctsys.verifications.ContentVerificationClient
import java.util.*

class AuctionFacadeTest(contentVerificationClient: ContentVerificationClient) {

    private val categoryFacade: CategoryFacade = CategoryConfiguration().categoryFacadeWithInMemoryRepository()

    private val auctionFacade: AuctionFacade =
        AuctionConfiguration().auctionFacadeWithInMemoryRepo(categoryFacade, contentVerificationClient)

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
        val auctionId: String = thereIsArchivedAuction()

        // when
        auctionFacade.accept(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ACCEPTED)

    }

    @Test
    fun `should ignore accepting when auction is accepted`() {
        // given
        val auctionId: String = thereIsAcceptedAuction()

        // when
        auctionFacade.accept(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ACCEPTED)
    }

    @Test
    fun `should not allow to accept rejected auction`() {
        // given
        val auctionId: String = thereIsRejectedAuction()

        // when then
        Assertions.assertThatThrownBy { auctionFacade.accept(auctionId) }
                .isInstanceOf(UnsupportedOperationOnAuctionException::class.java)
                .hasMessageContaining("Cannot perform acceptance on rejected auction")
    }

    @Test
    fun `should reject newly created auction`() {
        // given
        val auctionId: String = thereIsNewAuction()

        // when
        auctionFacade.reject(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.REJECTED)
    }

    @Test
    fun `should reject accepted auction`() {
        // given
        val auctionId: String = thereIsAcceptedAuction()

        // when
        auctionFacade.reject(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.REJECTED)
    }

    @Test
    fun `should not allow to reject archived auction`() {
        // given
        val auctionId: String = thereIsArchivedAuction()

        // when then
        Assertions.assertThatThrownBy { auctionFacade.reject(auctionId) }
                .isInstanceOf(UnsupportedOperationOnAuctionException::class.java)
                .hasMessageContaining("Cannot perform rejection on archived auction")
    }

    @Test
    fun `should ignore rejecting already rejected auction`() {
        // given
        val auctionId: String = thereIsRejectedAuction()

        // when
        auctionFacade.reject(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.REJECTED)
    }

    @Test
    fun `should archive newly created auction`() {
        // given
        val auctionId: String = thereIsNewAuction()

        // when
        auctionFacade.archive(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ARCHIVED)
    }

    @Test
    fun `should archive rejected auction`() {
        // given
        val auctionId: String = thereIsRejectedAuction()

        // when
        auctionFacade.archive(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ARCHIVED)
    }

    @Test
    fun `should archive accepted auction`() {
        // given
        val auctionId: String = thereIsAcceptedAuction()

        // when
        auctionFacade.archive(auctionId)

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ARCHIVED)
    }

    @Test
    fun `should not allow to archive already archived auction`() {
        // given
        val auctionId: String = thereIsArchivedAuction()

        // when then
        Assertions.assertThatThrownBy { auctionFacade.archive(auctionId) }
                .isInstanceOf(UnsupportedOperationOnAuctionException::class.java)
                .hasMessageContaining("Cannot perform archiving on archived auction")
    }

    @Test
    fun `should throw trying to perform any operation on nonexistent auction`() {
        // given
        val auctionId = "nonexistent"

        // when then
        Assertions.assertThatThrownBy {
            auctionFacade.archive(auctionId)
            auctionFacade.reject(auctionId)
            auctionFacade.archive(auctionId)
        }
                .isInstanceOf(AuctionNotFoundException::class.java)
                .hasMessageContaining("Accessed auction does not exist")
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
        val finalCategory: CategoryResponse = thereIsSampleCategoryTree()

        val auction = CreateAuctionRequest(
                name = "Adidas shoes",
                categoryId = finalCategory.id,
                description = "Breathable sports shoes",
                price = 145.2,
                cityId = "przykladoweID",
                productCondition = Condition.USED
        )
        val auctionId: String = auctionFacade.addNewAuction(createRequest = auction, auctioneerId = "auctioneer-${UUID.randomUUID()}").id!!
        action(auctionId)
        return auctionId
    }

    private fun thereIsSampleCategoryTree(): CategoryResponse {
        val topLevelCategory: CategoryResponse = categoryFacade.create(request = CategoryCreateRequest(
                name = "Clothing",
                description = "Just clothing",
                parentCategoryId = null,
                isTopLevel = true,
                isFinalNode = false
        ))

        val sneakersCategory: CategoryResponse = categoryFacade.create(request = CategoryCreateRequest(
                name = "Sneakers",
                description = "Nice sneakers",
                parentCategoryId = topLevelCategory.id,
                isTopLevel = false,
                isFinalNode = false
        ))

        val adidasSneakersCategory: CategoryResponse = categoryFacade.create(request = CategoryCreateRequest(
                name = "Adidas sneakers",
                description = "Nice adidas sneakers",
                parentCategoryId = sneakersCategory.id,
                isTopLevel = false,
                isFinalNode = true
        ))

        return adidasSneakersCategory
    }

}