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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.auctions.domain.Auction
import pl.kawaleria.auctsys.auctions.domain.AuctionRepository
import pl.kawaleria.auctsys.auctions.domain.Category
import pl.kawaleria.auctsys.auctions.domain.MongoAuctionRepository
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.AuctionDetailedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.AuctionSimplifiedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.PagedAuctions
import java.time.Duration
import java.time.Instant

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
            val existingAuctionsCount = thereAreAuctions()

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1

            // when
            val result = mockMvc.perform(
                    get(baseUrl)
                            .param("page", selectedPage.toString())
                            .param("pageSize", selectedPageSize.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect { status().isOk() }
                    .andReturn()

            // then
            val responseJson = result.response.contentAsString
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
            val result = mockMvc.perform(
                    get(baseUrl)
                            .param("page", selectedPage.toString())
                            .param("pageSize", selectedPageSize.toString())
                            .param("searchPhrase", selectedSearchPhrase)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson = result.response.contentAsString
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
            val selectedCategory = "MODA"
            val expectedFilteredAuctionsCount = 1

            // when
            val result = mockMvc.perform(
                    get(baseUrl)
                            .param("page", selectedPage.toString())
                            .param("pageSize", selectedPageSize.toString())
                            .param("searchPhrase", selectedSearchPhrase)
                            .param("category", selectedCategory)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received response from rest controller: {}", responseJson)

            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.name?.contains(selectedSearchPhrase, ignoreCase = true) ?: false
            }
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.category?.equals(Category.valueOf(selectedCategory)) ?: false
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
            val selectedCategory = "SPORT"
            val expectedFilteredAuctionsCount = 2

            // when
            val result = mockMvc.perform(
                    get(baseUrl)
                            .param("page", selectedPage.toString())
                            .param("pageSize", selectedPageSize.toString())
                            .param("searchPhrase", selectedSearchPhrase)
                            .param("category", selectedCategory)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received response from rest controller: {}", responseJson)

            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.category?.equals(Category.valueOf(selectedCategory)) ?: false
            }
            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
        }
    }

    @Nested
    inner class AuctionsGettersTests {
        private val baseUrl: String = "/auction-service/users/user-id/auctions"

        @Test
        fun `should return specific auction`() {
            // given
            val auction: Auction = thereIsAuction()
            val auctionId: String? = auction.id

            // when
            val result = mockMvc.perform(
                    get("$baseUrl/$auctionId")
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
            val result = mockMvc.perform(
                    get(baseUrl)
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
            val result = mockMvc.perform(
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
            val expectedErrorMessage = "Auction does not exists"

            // when
            val result = mockMvc.perform(
                    get("$baseUrl/nonExistingAuctionId")
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
            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    category = Category.MODA,
                    description = "Best headphones you can have",
                    price = 1.23
            )

            // when
            val result = mockMvc.perform(
                    post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(auctionRequestData)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val createdAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(createdAuction.name).isEqualTo(auctionRequestData.name)
            Assertions.assertThat(createdAuction.category).isEqualTo(auctionRequestData.category)
            Assertions.assertThat(createdAuction.price).isEqualTo(auctionRequestData.price)
            Assertions.assertThat(createdAuction.description).isEqualTo(auctionRequestData.description)
            Assertions.assertThat(createdAuction.auctioneerId).isEqualTo("user-id")
        }

        @Test
        fun `should not create auction with blank name`() {
            // given
            val auctionRequestData = CreateAuctionRequest(
                    name = "",
                    category = Category.MODA,
                    description = "Headphones",
                    price = 1.23
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
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
            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    category = Category.MODA,
                    description = "Headphones",
                    price = 1.23
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
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
            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Extra Ultra Mega Best Giga Fastest Smoothest Cleanest Cheapest Samsung headphones with Bluetooth",
                    category = Category.MODA,
                    description = "Headphones",
                    price = 1.23
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
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
            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    category = Category.MODA,
                    description = "Best headphones you can have",
                    price = -13.0
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
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
            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    category = Category.MODA,
                    description = "Best headphones you can have;[,.[;.;~??",
                    price = 13.0
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
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
                    category = oldAuction.category!!,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!
            )

            // when
            val result = mockMvc.perform(
                    put("$baseUrl/$oldAuctionId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val responseUpdatedAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(responseUpdatedAuction.name).isEqualTo(expectedAuctionName)
            Assertions.assertThat(responseUpdatedAuction.category).isEqualTo(updateAuctionRequest.category)
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
                    category = oldAuction.category!!,
                    description = expectedAuctionDescription,
                    price = expectedAuctionPrice
            )

            // when
            val result = mockMvc.perform(
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
            Assertions.assertThat(updatedAuction.category).isEqualTo(updateAuctionRequest.category)
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
                    category = oldAuction.category!!,
                    description = oldAuction.description!!,
                    price = newPrice
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
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
                    category = oldAuction.category!!,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
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
                    category = oldAuction.category!!,
                    description = newDescription,
                    price = oldAuction.price!!
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
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
                    category = oldAuction.category!!,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
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
                    category = Category.MODA,
                    description = "Best headphones you can have",
                    price = 1.23
            )

            val expectedErrorMessage = "Auction does not exists"

            // when
            val result = mockMvc.perform(
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
        fun `should not delete non-existing auction`() {
            // given
            val expectedErrorMessage = "Auction does not exists"

            // when
            val result = mockMvc.perform(
                    delete("$baseUrl/nonExistingAuctionId")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn()

            // then
            val responseErrorMessage: String? = result.response.errorMessage

            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }
    }

    private fun thereIsAuction(): Auction {
        val auction = Auction(
                name = "Wireless Samsung headphones",
                category = Category.MODA,
                description = "Best headphones you can have",
                price = 1.23,
                auctioneerId = "user-id",
                expiresAt = defaultExpiration()
        )

        return auctionRepository.save(auction)
    }

    private fun defaultExpiration(): Instant = Instant.now().plusSeconds(Duration.ofDays(10).toSeconds())

    private fun thereAreAuctions(): Int {
        val auctions = listOf(
                Auction(
                        name = "Wireless Samsung headphones",
                        category = Category.MODA,
                        description = "Headphones",
                        price = 1.23,
                        auctioneerId = "user-id",
                        expiresAt = defaultExpiration()


                ),
                Auction(
                        name = "Wireless JBL headphones",
                        category = Category.MODA,
                        description = "Headphones",
                        price = 1.13,
                        auctioneerId = "user-id",
                        expiresAt = defaultExpiration()

                ),
                Auction(
                        name = "jbl Speaker",
                        category = Category.SPORT,
                        description = "Speaker",
                        price = 5.99,
                        auctioneerId = "user-id",
                        expiresAt = defaultExpiration()

                ),
                Auction(
                        name = "Adidas T-Shirt",
                        category = Category.SPORT,
                        description = "T-Shirt",
                        price = 9.11,
                        auctioneerId = "user-id",
                        expiresAt = defaultExpiration()

                )
        )

        auctionRepository.saveAll(auctions)
        return auctions.size
    }
}
