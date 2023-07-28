package pl.kawaleria.auctsys.controllers

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
import pl.kawaleria.auctsys.auctions.dto.responses.AuctionDetailedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.AuctionSimplifiedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.PagedAuctions

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
    private lateinit var auctionRepository: AuctionRepository

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
            val auctionId = thereIsAuction()
            val expectedAuction = auctionRepository.findAuctionById(auctionId)

            // when
            val result = mockMvc.perform(
                get("$baseUrl/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()

            // then
            val responseJson = result.response.contentAsString
            val foundAuction = objectMapper.readValue(responseJson, AuctionSimplifiedResponse::class.java)

            Assertions.assertThat(foundAuction.id).isEqualTo(expectedAuction.id)
            Assertions.assertThat(foundAuction.name).isEqualTo(expectedAuction.name)
            Assertions.assertThat(foundAuction.category).isEqualTo(expectedAuction.category)
            Assertions.assertThat(foundAuction.price).isEqualTo(expectedAuction.price)
        }

        @Test
        fun `should return list of auctions belonging to the user`() {
            // given
            val expectedNumberOfAuctions = thereAreAuctions()

            // when
            val result = mockMvc.perform(
                get(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()

            // then
            val responseJson = result.response.contentAsString
            val mappedAuctions: List<AuctionSimplifiedResponse> = objectMapper.readValue(responseJson, objectMapper.typeFactory.constructCollectionType(List::class.java, AuctionSimplifiedResponse::class.java))
            val responseNumberOfAuctions = mappedAuctions.size

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
            val responseJson = result.response.contentAsString
            val mappedAuctions: List<AuctionSimplifiedResponse> = objectMapper.readValue(responseJson, objectMapper.typeFactory.constructCollectionType(List::class.java, AuctionSimplifiedResponse::class.java))
            val responseNumberOfAuctions = mappedAuctions.size

            Assertions.assertThat(responseNumberOfAuctions).isEqualTo(expectedNumberOfAuctions)
        }

        @Test
        fun `should not return a non-existing auction`() {
            // given
            val expectedStatus = 400
            val expectedErrorMessage = "Auction does not exists"

            // when
            val result = mockMvc.perform(
                get("$baseUrl/nonExistingAuctionId")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }
    }

    @Nested
    inner class AuctionsCreationTests {
        private val baseUrl: String = "/auction-service/users/user-id/auctions"

        @Test
        fun `should create auction`() {
            // given
            val auctionRequestData = Auction(
                name = "Wireless Samsung headphones",
                category = Category.MODA,
                description = "Best headphones you can have",
                price = 1.23,
                auctioneerId = "user-id"
            )
            val expectedName = "Wireless Samsung headphones"
            val expectedCategory = Category.MODA
            val expectedPrice = 1.23

            // when
            val result = mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData)))
                .andExpect(status().isOk())
                .andReturn()

            // then
            val responseJson = result.response.contentAsString
            val createdAuction = objectMapper.readValue(responseJson, AuctionSimplifiedResponse::class.java)

            Assertions.assertThat(createdAuction.name).isEqualTo(expectedName)
            Assertions.assertThat(createdAuction.category).isEqualTo(expectedCategory)
            Assertions.assertThat(createdAuction.price).isEqualTo(expectedPrice)
        }

        @Test
        fun `should not create auction with blank name`() {
            // given
            val auctionRequestData = Auction(
                name = "",
                category = Category.MODA,
                description = "Headphones",
                price = 1.23,
                auctioneerId = "user-id"
            )

            val expectedStatus = 400
            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not create auction with description containing less than 20 characters`() {
            // given
            val auctionRequestData = Auction(
                name = "Wireless Samsung headphones",
                category = Category.MODA,
                description = "Headphones",
                price = 1.23,
                auctioneerId = "user-id"
            )

            val expectedStatus = 400
            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not create auction with name containing more than 100 characters`() {
            // given
            val auctionRequestData = Auction(
                name = "Wireless Extra Ultra Mega Best Giga Fastest Smoothest Cleanest Cheapest Samsung headphones with Bluetooth",
                category = Category.MODA,
                description = "Headphones",
                price = 1.23,
                auctioneerId = "user-id"
            )

            val expectedStatus = 400
            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not create auction with negative price`() {
            // given
            val auctionRequestData = Auction(
                name = "Wireless Samsung headphones",
                category = Category.MODA,
                description = "Best headphones you can have",
                price = -13.0,
                auctioneerId = "user-id"
            )

            val expectedStatus = 400
            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not create auction with invalid description syntax`() {
            // given
            val auctionRequestData = Auction(
                name = "Wireless Samsung headphones",
                category = Category.MODA,
                description = "Best headphones you can have;[,.[;.;~??",
                price = 13.0,
                auctioneerId = "user-id"
            )

            val expectedStatus = 400
            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }
    }

    @Nested
    inner class AuctionsUpdateTests {
        private val baseUrl: String = "/auction-service/users/user-id/auctions"

        @Test
        fun `should update name in auction`() {
            // given
            val auctionId = thereIsAuction()
            val oldAuction = auctionRepository.findById(auctionId).orElseThrow()
            val expectedAuctionName = "Wireless Apple headphones"
            val newAuction = Auction(
                name = expectedAuctionName,
                category = oldAuction.category,
                description = oldAuction.description,
                price = oldAuction.price
            )

            // when
            val result = mockMvc.perform(
                put("$baseUrl/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newAuction)))
                .andExpect(status().isOk())
                .andReturn()

            // then
            val responseJson = result.response.contentAsString
            val updatedAuction = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.name).isEqualTo(expectedAuctionName)

            // Nie wiem czy jest to potrzebne, sprawdzam tu czy pozostałe parametry aukcji się nie zmieniły
            // Ewentualnie można to zostawić i usunać sprawdzanie id aukcji i id auctioneera
            Assertions.assertThat(updatedAuction.category).isEqualTo(oldAuction.category)
            Assertions.assertThat(updatedAuction.price).isEqualTo(oldAuction.price)
            Assertions.assertThat(updatedAuction.description).isEqualTo(oldAuction.description)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
        }

        @Test
        fun `should update description and price in auction`() {
            // given
            val auctionId = thereIsAuction()
            val oldAuction = auctionRepository.findById(auctionId).orElseThrow()
            val expectedAuctionDescription = "Wireless headphones with charger and original box"
            val expectedAuctionPrice = 123.45
            val newAuction = Auction(
                name = oldAuction.name,
                category = oldAuction.category,
                description = expectedAuctionDescription,
                price = expectedAuctionPrice
            )

            // when
            val result = mockMvc.perform(
                put("$baseUrl/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newAuction)))
                .andExpect(status().isOk())
                .andReturn()

            // then
            val responseJson = result.response.contentAsString
            val updatedAuction = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.description).isEqualTo(expectedAuctionDescription)
            Assertions.assertThat(updatedAuction.price).isEqualTo(expectedAuctionPrice)

            // To samo co w linii 512-513 (dla testu `should update name in auction`)
            Assertions.assertThat(updatedAuction.name).isEqualTo(oldAuction.name)
            Assertions.assertThat(updatedAuction.category).isEqualTo(oldAuction.category)
            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
        }

        @Test
        fun `should not update auction because of negative new price`() {
            // given
            val auctionId = thereIsAuction()
            val oldAuction = auctionRepository.findById(auctionId).orElseThrow()
            val newPrice = -15.45387
            val newAuction = Auction(
                name = oldAuction.name,
                category = oldAuction.category,
                description = oldAuction.description,
                price = newPrice
            )

            val expectedStatus = 400
            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
                put("$baseUrl/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newAuction)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not update auction because of too short new name`() {
            // given
            val auctionId = thereIsAuction()
            val oldAuction = auctionRepository.findById(auctionId).orElseThrow()
            val newName = "Bike"
            val newAuction = Auction(
                name = newName,
                category = oldAuction.category,
                description = oldAuction.description,
                price = oldAuction.price
            )

            val expectedStatus = 400
            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
                put("$baseUrl/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newAuction)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not update auction because of too long description`() {
            // given
            val auctionId = thereIsAuction()
            val oldAuction = auctionRepository.findById(auctionId).orElseThrow()

            // this description has 525 chars
            val newDescription = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc" +
                    "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc"

            val newAuction = Auction(
                name = oldAuction.name,
                category = oldAuction.category,
                description = newDescription,
                price = oldAuction.price
            )

            val expectedStatus = 400
            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
                put("$baseUrl/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newAuction)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not update auction because of invalid new name syntax`() {
            // given
            val auctionId = thereIsAuction()
            val oldAuction = auctionRepository.findById(auctionId).orElseThrow()
            val newName = "Headphones?"
            val newAuction = Auction(
                name = newName,
                category = oldAuction.category,
                description = oldAuction.description,
                price = oldAuction.price
            )

            val expectedStatus = 400
            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result = mockMvc.perform(
                put("$baseUrl/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newAuction)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }

        @Test
        fun `should not update non-existing auction`() {
            // given
            val newAuction = Auction(
                name = "Wireless Samsung headphones",
                category = Category.MODA,
                description = "Best headphones you can have",
                price = 1.23,
            )

            val expectedStatus = 400
            val expectedErrorMessage = "Auction does not exists"

            // when
            val result = mockMvc.perform(
                put("$baseUrl/nonExistingAuctionId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newAuction)))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }
    }

    @Nested
    inner class AuctionsDeleteTests {
        private val baseUrl = "/auction-service/users/user-id/auctions"
        @Test
        fun `should delete auction`() {
            // given
            val auctionId: String = thereIsAuction()

            // when
            mockMvc.perform(
                delete("$baseUrl/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())

            // then
            val doesAuctionExists = auctionRepository.existsById(auctionId)

            Assertions.assertThat(doesAuctionExists).isFalse()
        }
        
        @Test
        fun `should not delete non-existing auction`() {
            // given
            val expectedStatus = 400
            val expectedErrorMessage = "Auction does not exists"

            // when
            val result = mockMvc.perform(
                delete("$baseUrl/nonExistingAuctionId")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()

            // then
            val responseStatus = result.response.status
            val responseErrorMessage = result.response.errorMessage
            logger.info(responseErrorMessage)

            Assertions.assertThat(responseStatus).isEqualTo(expectedStatus)
            Assertions.assertThat(responseErrorMessage).isEqualTo(expectedErrorMessage)
        }
    }

    private fun thereIsAuction(): String {
        val auction = Auction(
            name = "Wireless Samsung headphones",
            category = Category.MODA,
            description = "Best headphones you can have",
            price = 1.23,
            auctioneerId = "user-id"
        )

        return auctionRepository.save(auction).id!!
    }

    private fun thereAreAuctions(): Int {
        val auctions = listOf(
                Auction(
                        name = "Wireless Samsung headphones",
                        category = Category.MODA,
                        description = "Headphones",
                        price = 1.23,
                        auctioneerId = "user-id"
                ),
                Auction(
                        name = "Wireless JBL headphones",
                        category = Category.MODA,
                        description = "Headphones",
                        price = 1.13,
                        auctioneerId = "user-id"
                ),
                Auction(
                        name = "jbl Speaker",
                        category = Category.SPORT,
                        description = "Speaker",
                        price = 5.99,
                        auctioneerId = "user-id"
                ),
                Auction(
                        name = "Adidas T-Shirt",
                        category = Category.SPORT,
                        description = "T-Shirt",
                        price = 9.11,
                        auctioneerId = "user-id"
                )
        )

        auctionRepository.saveAll(auctions)
        return auctions.size
    }
}
