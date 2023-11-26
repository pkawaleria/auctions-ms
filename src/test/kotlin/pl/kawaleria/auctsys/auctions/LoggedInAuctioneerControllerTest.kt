package pl.kawaleria.auctsys.auctions

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.AUCTIONEER_ID_UNDER_TEST
import pl.kawaleria.auctsys.MongoTestContainer
import pl.kawaleria.auctsys.auctions.domain.*
import pl.kawaleria.auctsys.auctions.dto.responses.PagedAuctions
import pl.kawaleria.auctsys.withAuthenticatedAuctioneer
import java.time.Duration
import java.time.Instant
import java.util.*


private const val baseUrl: String = "/auction-service/active-auctioneer"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoggedInAuctioneerControllerTest {

    private val mongo: MongoDBContainer = MongoTestContainer.instance

    init {
        System.setProperty("spring.data.mongodb.uri", mongo.replicaSetUrl)
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

    @Test
    fun `should get all auctions of logged in auctioneer`() {
        // given
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ARCHIVED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.NEW)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ACCEPTED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.REJECTED)

        val selectedPage = 0
        val selectedPageSize = 20
        val expectedPageCount = 1
        val expectedSearchedAuctionsCount = 12

        // when
        val result: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/auctions")
                .withAuthenticatedAuctioneer()
                .param("page", selectedPage.toString())
                .param("pageSize", selectedPageSize.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )

            .andExpect { MockMvcResultMatchers.status().isOk }
            .andReturn()

        // then
        val responseJson: String = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        AuctionControllerTest.logger.info("Received responses from rest controller: {}", responseJson)

        Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedSearchedAuctionsCount)
        Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
        Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
    }

    @Test
    fun `should get logged in auctioneer accepted auctions`() {
        // given
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ARCHIVED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.NEW)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ACCEPTED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.REJECTED)

        val selectedPage = 0
        val selectedPageSize = 10
        val expectedPageCount = 1
        val expectedSearchedAuctionsCount = 3

        // when
        val result: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/active-auctions")
                .withAuthenticatedAuctioneer()
                .param("page", selectedPage.toString())
                .param("pageSize", selectedPageSize.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )

            .andExpect { MockMvcResultMatchers.status().isOk }
            .andReturn()

        // then
        val responseJson: String = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        AuctionControllerTest.logger.info("Received responses from rest controller: {}", responseJson)

        Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedSearchedAuctionsCount)
        Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
        Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
    }

    @Test
    fun `should get logged in auctioneer rejected auctions`() {
        // given
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ARCHIVED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.NEW)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ACCEPTED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.REJECTED)

        val selectedPage = 0
        val selectedPageSize = 10
        val expectedPageCount = 1
        val expectedSearchedAuctionsCount = 3

        // when
        val result: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/rejected-auctions")
                .withAuthenticatedAuctioneer()
                .param("page", selectedPage.toString())
                .param("pageSize", selectedPageSize.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )

            .andExpect { MockMvcResultMatchers.status().isOk }
            .andReturn()

        // then
        val responseJson: String = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        AuctionControllerTest.logger.info("Received responses from rest controller: {}", responseJson)

        Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedSearchedAuctionsCount)
        Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
        Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
    }


    @Test
    fun `should get logged in auctioneer awaiting acceptance auctions`() {
        // given
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ARCHIVED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.NEW)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ACCEPTED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.REJECTED)

        val selectedPage = 0
        val selectedPageSize = 10
        val expectedPageCount = 1
        val expectedSearchedAuctionsCount = 3

        // when
        val result: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/awaiting-auctions")
                .withAuthenticatedAuctioneer()
                .param("page", selectedPage.toString())
                .param("pageSize", selectedPageSize.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )

            .andExpect { MockMvcResultMatchers.status().isOk }
            .andReturn()

        // then
        val responseJson: String = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        AuctionControllerTest.logger.info("Received responses from rest controller: {}", responseJson)

        Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedSearchedAuctionsCount)
        Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
        Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
    }


    @Test
    fun `should get logged in auctioneer archived auctions`() {
        // given
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ARCHIVED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.NEW)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.ACCEPTED)
        thereAreAuctionsOfStatusPostedByAuctioneer(AuctionStatus.REJECTED)

        val selectedPage = 0
        val selectedPageSize = 10
        val expectedPageCount = 1
        val expectedSearchedAuctionsCount = 3

        // when
        val result: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/archived-auctions")
                .withAuthenticatedAuctioneer()
                .param("page", selectedPage.toString())
                .param("pageSize", selectedPageSize.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )

            .andExpect { MockMvcResultMatchers.status().isOk }
            .andReturn()

        // then
        val responseJson: String = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        AuctionControllerTest.logger.info("Received responses from rest controller: {}", responseJson)

        Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedSearchedAuctionsCount)
        Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
        Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
    }

    @Test
    fun `should get logged in auctioneer expired auctions`() {
        // given
        thereIsAcceptedButExpiredAuction()
        thereIsAcceptedAndNonExpiredAuction()

        val selectedPage = 0
        val selectedPageSize = 10
        val expectedPageCount = 1
        val expectedSearchedAuctionsCount = 1

        // when
        val result: MvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/expired-auctions")
                .withAuthenticatedAuctioneer()
                .param("page", selectedPage.toString())
                .param("pageSize", selectedPageSize.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )

            .andExpect { MockMvcResultMatchers.status().isOk }
            .andReturn()

        // then
        val responseJson: String = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        AuctionControllerTest.logger.info("Received responses from rest controller: {}", responseJson)

        Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedSearchedAuctionsCount)
        Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
        Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
    }


    private fun thereIsAcceptedButExpiredAuction(): Auction {
        return auctionRepository.save(
            Auction(
                name = "Wireless Samsung headphones",
                description = "Best headphones you can have",
                price = 1.23,
                auctioneerId = AUCTIONEER_ID_UNDER_TEST,
                category = Category(ObjectId().toString(), "cat name"),
                categoryPath = CategoryPath(pathElements = mutableListOf()),
                productCondition = Condition.NEW,
                cityId = ObjectId().toString(),
                cityName = "city name",
                province = "province name",
                location = GeoJsonPoint(12.23, 23.33),
                expiresAt = Instant.now().minusSeconds(Duration.ofDays(1).toSeconds()),
                thumbnail = byteArrayOf(),
                status = AuctionStatus.ACCEPTED,
                phoneNumber = "123456780"
            )
        )
    }

    private fun thereIsAcceptedAndNonExpiredAuction(): Auction {
        return auctionRepository.save(
            Auction(
                name = "Wireless Samsung headphones",
                description = "Best headphones you can have",
                price = 1.23,
                auctioneerId = AUCTIONEER_ID_UNDER_TEST,
                category = Category(ObjectId().toString(), "cat name"),
                categoryPath = CategoryPath(pathElements = mutableListOf()),
                productCondition = Condition.NEW,
                cityId = ObjectId().toString(),
                cityName = "city name",
                province = "province name",
                location = GeoJsonPoint(12.23, 23.33),
                expiresAt = Instant.now().plusSeconds(Duration.ofDays(1).toSeconds()),
                thumbnail = byteArrayOf(),
                status = AuctionStatus.ACCEPTED,
                phoneNumber = "123456780"
            )
        )
    }

    private fun thereAreAuctionsOfStatusPostedByAuctioneer(auctionStatus: AuctionStatus): List<Auction> {
        val auctions: List<Auction> = listOf(
            Auction(
                name = "Wireless Samsung headphones",
                description = "Best headphones you can have",
                price = 1.23,
                auctioneerId = AUCTIONEER_ID_UNDER_TEST,
                category = Category(ObjectId().toString(), "cat name"),
                categoryPath = CategoryPath(pathElements = mutableListOf()),
                productCondition = Condition.NEW,
                cityId = ObjectId().toString(),
                cityName = "city name",
                province = "province name",
                location = GeoJsonPoint(12.23, 23.33),
                expiresAt = Instant.now().plusSeconds(Duration.ofDays(1).toSeconds()),
                thumbnail = byteArrayOf(),
                status = auctionStatus,
                phoneNumber = "123456780"
            ),
            Auction(
                name = "Wireless JBL headphones",
                description = "Worst headphones you can have",
                price = 4.56,
                auctioneerId = AUCTIONEER_ID_UNDER_TEST,
                category = Category(ObjectId().toString(), "cat name"),
                categoryPath = CategoryPath(pathElements = mutableListOf()),
                productCondition = Condition.NEW,
                cityId = ObjectId().toString(),
                cityName = "city name",
                province = "province name",
                location = GeoJsonPoint(12.23, 23.33),
                expiresAt = Instant.now().plusSeconds(Duration.ofDays(1).toSeconds()),
                thumbnail = byteArrayOf(),
                status = auctionStatus,
                phoneNumber = "123456780"

            ),
            Auction(
                name = "Wireless Sony headphones",
                description = "Best sony headphones you can have",
                price = 78.9,
                auctioneerId = AUCTIONEER_ID_UNDER_TEST,
                category = Category(ObjectId().toString(), "cat name"),
                categoryPath = CategoryPath(pathElements = mutableListOf()),
                productCondition = Condition.NEW,
                cityId = ObjectId().toString(),
                cityName = "city name",
                province = "province name",
                location = GeoJsonPoint(12.23, 23.33),
                expiresAt = Instant.now().plusSeconds(Duration.ofDays(1).toSeconds()),
                thumbnail = byteArrayOf(),
                status = auctionStatus,
                phoneNumber = "123456780"
            )
        )

        return auctionRepository.saveAll(auctions)
    }
}