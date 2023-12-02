package pl.kawaleria.auctsys.auctions.domain

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import pl.kawaleria.auctsys.TestAuctioneerAuthentication
import pl.kawaleria.auctsys.auctions.AuctionControllerTest
import pl.kawaleria.auctsys.auctions.dto.exceptions.AuctionNotFoundException
import pl.kawaleria.auctsys.auctions.dto.exceptions.UnsupportedOperationOnAuctionException
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.categories.domain.CategoryConfiguration
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.categories.dto.requests.CategoryCreateRequest
import pl.kawaleria.auctsys.categories.dto.responses.CategoryResponse
import pl.kawaleria.auctsys.commons.toAuctioneerId
import pl.kawaleria.auctsys.views.domain.AuctionViewsConfiguration
import pl.kawaleria.auctsys.views.domain.AuctionViewsQueryFacade

class AuctionFacadeTest {

    private val auctionViewsQueryFacade: AuctionViewsQueryFacade = AuctionViewsConfiguration().auctionViewsQueryFacadeWithInMemoryRepositories()
    private val categoryFacade: CategoryFacade = CategoryConfiguration().categoryFacadeWithInMemoryRepository()
    private val auctionFacade: AuctionFacade = AuctionConfiguration().auctionFacadeWithInMemoryRepo(categoryFacade, auctionViewsQueryFacade)

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AuctionControllerTest::class.java)
    }

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
            .hasMessageContaining("Invalid operation performed on an auction")
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
            .hasMessageContaining("Invalid operation performed on an auction")
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
        auctionFacade.archive(auctionId, getDefaultAuthContext())

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ARCHIVED)
    }

    @Test
    fun `should archive rejected auction`() {
        // given
        val auctionId: String = thereIsRejectedAuction()

        // when
        auctionFacade.archive(auctionId, getDefaultAuthContext())

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ARCHIVED)
    }

    @Test
    fun `should archive accepted auction`() {
        // given
        val auctionId: String = thereIsAcceptedAuction()

        // when
        auctionFacade.archive(auctionId, getDefaultAuthContext())

        // then
        Assertions.assertThat(auctionFacade.findAuctionById(auctionId).status).isEqualTo(AuctionStatus.ARCHIVED)
    }

    @Test
    fun `should not allow to archive already archived auction`() {
        // given
        val auctionId: String = thereIsArchivedAuction()
        logger.info(auctionFacade.findAuctionById(auctionId).toString())

        // when then
        Assertions.assertThatThrownBy { auctionFacade.archive(auctionId, getDefaultAuthContext()) }
            .isInstanceOf(UnsupportedOperationOnAuctionException::class.java)
            .hasMessageContaining("Invalid operation performed on an auction")
    }

    @Test
    fun `should throw trying to perform any operation on nonexistent auction`() {
        // given
        val auctionId = "nonexistent"

        // when then
        Assertions.assertThatThrownBy {
            auctionFacade.archive(auctionId, getDefaultAuthContext())
            auctionFacade.reject(auctionId)
            auctionFacade.archive(auctionId, getDefaultAuthContext())
        }
            .isInstanceOf(AuctionNotFoundException::class.java)
            .hasMessageContaining("Accessed auction does not exist")
    }

    private fun thereIsRejectedAuction(): String {
        return thereIsAuctionAfterOperationOf { auctionId -> auctionFacade.reject(auctionId) }
    }

    private fun thereIsArchivedAuction(): String {
        return thereIsAuctionAfterOperationOf { auctionId -> auctionFacade.archive(auctionId, getDefaultAuthContext()) }
    }

    private fun thereIsAcceptedAuction(): String {
        return thereIsAuctionAfterOperationOf { auctionId -> auctionFacade.accept(auctionId) }
    }

    private fun thereIsNewAuction(): String {
        return thereIsAuctionAfterOperationOf()
    }

    private fun getDefaultAuthContext(): Authentication {
        return TestAuctioneerAuthentication()
    }

    private fun thereIsAuctionAfterOperationOf(action: (String) -> Unit = {}): String {
        val finalCategory: CategoryResponse = thereIsSampleCategoryTree()

        val city: City = thereIsCity()

        val auction = CreateAuctionRequest(
            name = "Adidas shoes",
            description = "Breathable sports shoes",
            price = 145.2,
            categoryId = finalCategory.id,
            productCondition = Condition.USED,
            cityId = city.id,
            phoneNumber = "123456780"
        )

        val auctionId: String =
            auctionFacade.create(createRequest = auction, auctioneerId = getDefaultAuthContext().toAuctioneerId()).id

        action(auctionId)

        return auctionId
    }

    private fun thereIsSampleCategoryTree(): CategoryResponse {
        val topLevelCategory: CategoryResponse = categoryFacade.create(
            request = CategoryCreateRequest(
                name = "Clothing",
                description = "Just clothing",
                parentCategoryId = null,
                isTopLevel = true,
                isFinalNode = false
            )
        )

        val sneakersCategory: CategoryResponse = categoryFacade.create(
            request = CategoryCreateRequest(
                name = "Sneakers",
                description = "Nice sneakers",
                parentCategoryId = topLevelCategory.id,
                isTopLevel = false,
                isFinalNode = false
            )
        )

        return categoryFacade.create(
            request = CategoryCreateRequest(
                name = "Adidas sneakers",
                description = "Nice adidas sneakers",
                parentCategoryId = sneakersCategory.id,
                isTopLevel = false,
                isFinalNode = true
            )
        )
    }

    private fun thereIsCity(): City {
        return auctionFacade.saveCity(
            City(
                name = "Lublin testowy",
                type = "village",
                province = "Wojewodztwo pierwsze",
                district = "Powiat pierwszy",
                commune = "Gmina pierwsza",
                latitude = 51.25,
                longitude = 22.5666
            )
        )
    }

}