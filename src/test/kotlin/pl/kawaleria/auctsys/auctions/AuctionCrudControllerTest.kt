package pl.kawaleria.auctsys.auctions

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.auctions.domain.*
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.AuctionDetailedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.AuctionSimplifiedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.PagedAuctions
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.categories.dto.request.CategoryCreateRequest
import pl.kawaleria.auctsys.categories.dto.response.CategoryResponse
import java.time.Duration
import java.time.Instant
import java.util.*

/*To run this test you need running Docker environment*/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuctionControllerTest {

    private val mongo: MongoDBContainer = MongoDBContainer("mongo").apply {
        start()
    }

    init {
        System.setProperty("spring.data.mongodb.uri", mongo.replicaSetUrl)
    }

    companion object {
        val logger: Logger = getLogger(AuctionControllerTest::class.java)
    }

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var auctionRepository: MongoAuctionRepository

    @Autowired
    private lateinit var categoryFacade: CategoryFacade

    @Autowired
    private lateinit var cityRepository: CityRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var mockMvc: MockMvc

    @AfterEach
    fun cleanUp() {
        mongoTemplate.dropCollection("auctions")
    }

    @Nested
    inner class AuctionsSearchTests {
        private val baseUrl: String = "/auction-service/auctions"

        @Test
        fun `should return selected page from all auctions when search phrase and search category are not specified`() {
            // given
            val existingAuctionsCount: Int = thereAreAuctions()

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1

            // when
            val result: MvcResult = mockMvc.perform(
                    get(baseUrl)
                            .param("page", selectedPage.toString())
                            .param("pageSize", selectedPageSize.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect { status().isOk() }
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received response from rest controller: {}", responseJson)

            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(existingAuctionsCount)
            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
        }

        @Test
        fun `should search among auctions with selected phrase when search phrase is specified but search category is not`() {
            // given
            thereAreAuctions()

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1
            val selectedSearchPhrase = "JBL"
            val expectedFilteredAuctionsCount = 2

            // when
            val result: MvcResult = mockMvc.perform(
                    get(baseUrl)
                            .param("page", selectedPage.toString())
                            .param("pageSize", selectedPageSize.toString())
                            .param("searchPhrase", selectedSearchPhrase)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received response from rest controller: {}", responseJson)


            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.name?.contains(selectedSearchPhrase, ignoreCase = true) ?: false
            }
            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
        }

        @Test
        fun `should search among auctions with provided search phrase and category`() {
            // given
            thereAreAuctions()

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1
            val selectedSearchPhrase = "JBL"
            val selectedCategory = "Electronics"
            val expectedFilteredAuctionsCount = 2

            // when
            val result: MvcResult = mockMvc.perform(
                    get(baseUrl)
                            .param("page", selectedPage.toString())
                            .param("pageSize", selectedPageSize.toString())
                            .param("searchPhrase", selectedSearchPhrase)
                            .param("category", selectedCategory)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received response from rest controller: {}", responseJson)

            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.name?.contains(selectedSearchPhrase, ignoreCase = true) ?: false
            }
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.categoryPath.pathElements.map{ it.name }.any { it.equals(selectedCategory) }
            }
            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
        }

        @Test
        fun `should search among auctions with selected category when search phrase is blank and category is specified`() {
            // given
            thereAreAuctions()

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1
            val selectedSearchPhrase = " "
            val selectedCategory = "Headphones"
            val expectedFilteredAuctionsCount = 2

            // when
            val result: MvcResult = mockMvc.perform(
                    get(baseUrl)
                            .param("page", selectedPage.toString())
                            .param("pageSize", selectedPageSize.toString())
                            .param("searchPhrase", selectedSearchPhrase)
                            .param("category", selectedCategory)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received response from rest controller: {}", responseJson)

            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.categoryPath.pathElements.map{ it.name }.any { it.equals(selectedCategory) }
            }
            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
        }
    }

    @Nested
    inner class AuctionsGettersTests {
        private val singleAuctionBaseUrl: String = "/auction-service/auctions"
        private val userAuctionsBaseUrl: String = "/auction-service/users/user-id/auctions"

        @Test
        fun `should return specific auction`() {
            // given
            val auction: Auction = thereIsAuction()
            val auctionId: String? = auction.id

            // when
            val result: MvcResult = mockMvc.perform(
                    get("$singleAuctionBaseUrl/$auctionId")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val foundAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(foundAuction.id).isEqualTo(auction.id)
            Assertions.assertThat(foundAuction.name).isEqualTo(auction.name)
            Assertions.assertThat(foundAuction.auctioneerId).isEqualTo(auction.auctioneerId)
            Assertions.assertThat(foundAuction.description).isEqualTo(auction.description)
            Assertions.assertThat(foundAuction.category).isEqualTo(auction.category)
            Assertions.assertThat(foundAuction.price).isEqualTo(auction.price)
        }


        @Test
        fun `should return list of auctions belonging to the user`() {
            // given
            val expectedNumberOfAuctions: Int = thereAreAuctions()

            // when
            val result: MvcResult = mockMvc.perform(
                    get(userAuctionsBaseUrl)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val mappedAuctions: List<AuctionSimplifiedResponse> = objectMapper.readValue(responseJson, objectMapper.typeFactory.constructCollectionType(List::class.java, AuctionSimplifiedResponse::class.java))
            val responseNumberOfAuctions: Int = mappedAuctions.size

            Assertions.assertThat(responseNumberOfAuctions).isEqualTo(expectedNumberOfAuctions)
        }

        @Test
        fun `should return empty list of auctions of non-existing user`() {
            // given
            val expectedNumberOfAuctions = 0

            // when
            val result: MvcResult = mockMvc.perform(
                    get("/auction-service/users/nonExistingUserId/auctions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val mappedAuctions: List<AuctionSimplifiedResponse> = objectMapper.readValue(responseJson, objectMapper.typeFactory.constructCollectionType(List::class.java, AuctionSimplifiedResponse::class.java))
            val responseNumberOfAuctions: Int = mappedAuctions.size

            Assertions.assertThat(responseNumberOfAuctions).isEqualTo(expectedNumberOfAuctions)
        }

        @Test
        fun `should not return a non-existing auction`() {
            // given
            val expectedErrorMessage = "Accessed auction does not exist"

            // when
            val result: MvcResult = mockMvc.perform(
                    get("$singleAuctionBaseUrl/nonExistingAuctionId")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }
    }

    @Nested
    inner class AuctionsCreationTests {
        private val baseUrl: String = "/auction-service/users/user-id/auctions"

        @Test
        fun `should create auction`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val cityId: String = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    categoryId = category.id,
                    description = "Best headphones you can have",
                    price = 1.23,
                    cityId = cityId,
                    productCondition = "Nowy"
            )

            // when
            val result: MvcResult = mockMvc.perform(
                    post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(auctionRequestData)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val createdAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(createdAuction.name).isEqualTo(auctionRequestData.name)
            Assertions.assertThat(createdAuction.price).isEqualTo(auctionRequestData.price)
            Assertions.assertThat(createdAuction.description).isEqualTo(auctionRequestData.description)
            Assertions.assertThat(createdAuction.auctioneerId).isEqualTo("user-id")
        }

        @Test
        fun `should not create auction with blank name`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val cityId: String = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                    name = "",
                    categoryId = category.id,
                    description = "Headphones",
                    price = 1.23,
                    cityId = cityId,
                    productCondition = "Nowy"
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(auctionRequestData)))
                    .andExpect(status().isBadRequest())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not create auction with description containing less than 20 characters`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val cityId: String = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    categoryId = category.id,
                    description = "Headphones",
                    price = 1.23,
                    cityId = cityId,
                    productCondition = "Używany"
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(auctionRequestData)))
                    .andExpect(status().isBadRequest())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not create auction with name containing more than 100 characters`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val cityId: String = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Extra Ultra Mega Best Giga Fastest Smoothest Cleanest Cheapest Samsung headphones with Bluetooth",
                    categoryId = category.id,
                    description = "Headphones",
                    price = 1.23,
                    cityId = cityId,
                    productCondition = "Nie dotyczy"
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(auctionRequestData)))
                    .andExpect(status().isBadRequest())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not create auction with negative price`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val cityId: String = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    categoryId = category.id,
                    description = "Best headphones you can have",
                    price = -13.0,
                    cityId = cityId,
                    productCondition = "Używany"
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(auctionRequestData)))
                    .andExpect(status().isBadRequest())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not create auction with invalid description syntax`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val cityId: String = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    description = "Best headphones you can have;[,.[;.;~??",
                    categoryId = category.id,
                    price = 13.0,
                    cityId = cityId,
                    productCondition = "Nie dotyczy"
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(auctionRequestData)))
                    .andExpect(status().isBadRequest())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }
    }

    @Nested
    inner class AuctionsUpdateTests {
        private val baseUrl: String = "/auction-service/users/user-id/auctions"

        @Test
        fun `should update name in auction`() {
            // given
            val oldAuction: Auction = thereIsAuction()
            val oldAuctionId: String? = oldAuction.id

            val expectedAuctionName = "Wireless Apple headphones"

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = expectedAuctionName,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!
            )

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$baseUrl/$oldAuctionId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val responseUpdatedAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(responseUpdatedAuction.name).isEqualTo(expectedAuctionName)
            Assertions.assertThat(responseUpdatedAuction.price).isEqualTo(updateAuctionRequest.price)
            Assertions.assertThat(responseUpdatedAuction.description).isEqualTo(updateAuctionRequest.description)
            Assertions.assertThat(responseUpdatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
            Assertions.assertThat(responseUpdatedAuction.id).isEqualTo(oldAuctionId)
        }

        @Test
        fun `should update description and price in auction`() {
            // given
            val oldAuction: Auction = thereIsAuction()
            val oldAuctionId: String? = oldAuction.id

            val expectedAuctionDescription = "Wireless headphones with charger and original box"
            val expectedAuctionPrice = 123.45

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = oldAuction.name!!,
                    description = expectedAuctionDescription,
                    price = expectedAuctionPrice
            )

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$baseUrl/$oldAuctionId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.description).isEqualTo(expectedAuctionDescription)
            Assertions.assertThat(updatedAuction.price).isEqualTo(expectedAuctionPrice)
            Assertions.assertThat(updatedAuction.name).isEqualTo(updateAuctionRequest.name)
            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuctionId)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
        }

        @Test
        fun `should not update auction because of negative new price`() {
            // given
            val oldAuction: Auction = thereIsAuction()
            val oldAuctionId: String? = oldAuction.id

            val newPrice = -15.45387

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = oldAuction.name!!,
                    description = oldAuction.description!!,
                    price = newPrice
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$baseUrl/$oldAuctionId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isBadRequest())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not update auction because of too short new name`() {
            // given
            val oldAuction: Auction = thereIsAuction()
            val auctionId: String? = oldAuction.id

            val newName = "Bike"

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = newName,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$baseUrl/$auctionId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isBadRequest())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not update auction because of too long description`() {
            // given
            val oldAuction: Auction = thereIsAuction()
            val oldAuctionId: String? = oldAuction.id

            // this description has 525 chars
            val newDescription = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc"

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = oldAuction.name!!,
                    description = newDescription,
                    price = oldAuction.price!!
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result:MvcResult = mockMvc.perform(
                    put("$baseUrl/$oldAuctionId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isBadRequest())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not update auction because of invalid new name syntax`() {
            // given
            val oldAuction: Auction = thereIsAuction()
            val oldAuctionId: String? = oldAuction.id

            val newName = "Headphones?"

            val newAuction = UpdateAuctionRequest(
                    name = newName,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$baseUrl/$oldAuctionId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newAuction)))
                    .andExpect(status().isBadRequest())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not update non-existing auction`() {
            // given
            val updateAuctionRequest = UpdateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    description = "Best headphones you can have",
                    price = 1.23
            )

            val expectedErrorMessage = "Accessed auction does not exist"

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$baseUrl/nonExistingAuctionId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isNotFound())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }
    }

    @Nested
    inner class AuctionsDeleteTests {
        private val baseUrl = "/auction-service/users/user-id/auctions"

        @Test
        fun `should delete auction`() {
            // given
            val auctionId: String = thereIsAuction().id!!

            // when
            mockMvc.perform(
                    delete("$baseUrl/$auctionId")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())

            // then
            val doesAuctionExists: Boolean = auctionRepository.existsById(auctionId)

            Assertions.assertThat(doesAuctionExists).isFalse()
        }

        @Test
        fun `should return not found trying to delete non-existing auction`() {
            // given
            val expectedErrorMessage = "Accessed auction does not exist"

            // when
            val result: MvcResult = mockMvc.perform(
                    delete("$baseUrl/nonExistingAuctionId")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound)
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }
    }

    private fun thereIsAuction(): Auction {
        val electronics = Category(UUID.randomUUID().toString(), "Electronics")
        val headphones = Category(UUID.randomUUID().toString(), "Headphones")
        val wirelessHeadphones = Category(UUID.randomUUID().toString(), "Wireless Headphones")
        val categoryPath = CategoryPath(
                pathElements = mutableListOf(electronics, headphones, wirelessHeadphones)
        )

        val cityId: String = thereIsCity()

        val auction = Auction(
                name = "Wireless Samsung headphones",
                category = wirelessHeadphones,
                categoryPath = categoryPath,
                description = "Best headphones you can have",
                price = 1.23,
                auctioneerId = "user-id",
                expiresAt = Instant.now().plusSeconds(Duration.ofDays(1).toSeconds()),
                cityId = cityId,
                productCondition = "Nowy"
        )

        return auctionRepository.save(auction)
    }

    private fun thereIsCity(): String {
        val city = City(
                name = "Miasto1",
                type = "village",
                province = "Województwo",
                district = "Powiat",
                commune = "Gmina",
                latitude = 1.23,
                longitude = 4.56
        )

        return cityRepository.save(city).id.toString()
    }

    private fun thereAreAuctions(): Int {
        val electronics = Category(UUID.randomUUID().toString(), "Electronics")
        val headphones = Category(UUID.randomUUID().toString(), "Headphones")
        val wirelessHeadphones = Category(UUID.randomUUID().toString(), "Wireless Headphones")
        val wirelessHeadphonesCategoryPath = CategoryPath(
                pathElements = mutableListOf(electronics, headphones, wirelessHeadphones)
        )

        val speakers = Category(UUID.randomUUID().toString(), "Speakers")
        val speakersCategoryPath = CategoryPath(
                pathElements = mutableListOf(electronics, speakers)
        )

        val clothing = Category(UUID.randomUUID().toString(), "Clothing")
        val unisexClothing = Category(UUID.randomUUID().toString(), "Unisex")
        val tshirts = Category(UUID.randomUUID().toString(), "Tshirts")
        val tshirtsCategoryPath = CategoryPath(
                pathElements = mutableListOf(clothing, unisexClothing, tshirts)
        )

        val cities: List<City> = thereAreCities()

        val auctions: List<Auction> = listOf(
                Auction(
                        name = "Wireless Samsung headphones",
                        category = wirelessHeadphones,
                        categoryPath = wirelessHeadphonesCategoryPath,
                        description = "Best headphones you can have",
                        price = 1.23,
                        auctioneerId = "user-id",
                        expiresAt = Instant.now().plusSeconds(Duration.ofDays(1).toSeconds()),
                        cityId = cities[0].id,
                        productCondition = "Nowy"
                ),
                Auction(
                        name = "Wireless JBL headphones",
                        category = wirelessHeadphones,
                        categoryPath = wirelessHeadphonesCategoryPath,
                        description = "Headphones",
                        price = 1.13,
                        auctioneerId = "user-id",
                        expiresAt = defaultExpiration(),
                        cityId = cities[1].id,
                        productCondition = "Używany"
                ),
                Auction(
                        name = "jbl Speaker",
                        category = speakers,
                        categoryPath = speakersCategoryPath,
                        description = "Speaker",
                        price = 5.99,
                        auctioneerId = "user-id",
                        expiresAt = defaultExpiration(),
                        cityId = cities[2].id,
                        productCondition = "Nie dotyczy"
                ),
                Auction(
                        name = "Adidas T-Shirt",
                        category = tshirts,
                        categoryPath = tshirtsCategoryPath,
                        description = "T-Shirt",
                        price = 9.11,
                        auctioneerId = "user-id",
                        expiresAt = defaultExpiration(),
                        cityId = cities[1].id,
                        productCondition = "Używany"
                )
        )

        auctionRepository.saveAll(auctions)
        return auctions.size
    }

    private fun thereAreCities(): List<City> {
        val cities: List<City> = listOf(
            City(
                name = "Nazwa1",
                type = "village",
                province = "Województwo1",
                district = "Powiat1",
                commune = "Gmina1",
                latitude = 123.0,
                longitude = 456.0
            ),
            City(
                name = "Nazwa2",
                type = "village",
                province = "Województwo2",
                district = "Powiat2",
                commune = "Gmina2",
                latitude = 234.0,
                longitude = 567.0
            ),
            City(
                name = "Nazwa3",
                type = "village",
                province = "Województwo3",
                district = "Powiat3",
                commune = "Gmina3",
                latitude = 987.0,
                longitude = 654.0
            )
        )

        cityRepository.saveAll(cities)
        return cities
    }

    private fun defaultExpiration(): Instant = Instant.now().plusSeconds(Duration.ofDays(10).toSeconds())

    private fun thereIsSampleCategoryTree(): CategoryResponse {
        val topLevelCategory: CategoryResponse = categoryFacade.create(request = CategoryCreateRequest(
                name = "Top level category",
                description = "Just top level category",
                parentCategoryId = null,
                isTopLevel = true,
                isFinalNode = false
        ))

        val secondLevelCategory: CategoryResponse = categoryFacade.create(request = CategoryCreateRequest(
                name = "Second level category",
                description = "Just second level category",
                parentCategoryId = topLevelCategory.id,
                isTopLevel = false,
                isFinalNode = false
        ))

        val finalLevelCategory: CategoryResponse = categoryFacade.create(request = CategoryCreateRequest(
                name = "Final level category",
                description = "Nice final level category",
                parentCategoryId = secondLevelCategory.id,
                isTopLevel = false,
                isFinalNode = true
        ))

        return finalLevelCategory
    }

}
