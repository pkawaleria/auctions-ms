package pl.kawaleria.auctsys.auctions

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.auctions.domain.*
import java.time.Duration
import java.time.Instant
import java.util.*

private const val baseUrl = "/auction-service/auctions"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuctionOperationsControllerTest {

    private val mongo: MongoDBContainer = MongoDBContainer("mongo").apply {
        start()
    }

    init {
        System.setProperty("spring.data.mongodb.uri", mongo.replicaSetUrl)
    }

    @Autowired
    private lateinit var auctionRepository: MongoAuctionRepository

    @Autowired
    private lateinit var cityRepository: CityRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var mockMvc: MockMvc

    @AfterEach
    fun cleanUp() {
        mongoTemplate.dropCollection("auctions")
        mongoTemplate.dropCollection("cities")
    }

    @Test
    fun `should accept auction`() {
        // given
        val auction: Auction = thereIsAuction()

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post("$baseUrl/${auction.id}/operations/accept"))
                .andExpect { MockMvcResultMatchers.status().isAccepted }

        // then
        val auctionAfterAcceptance: Auction? = auction.id?.let { auctionRepository.findById(it).orElseThrow() }
        val isAccepted: Boolean = auctionAfterAcceptance?.isAccepted() ?: false
        Assertions.assertThat(isAccepted).isTrue()
    }

    @Test
    fun `should reject auction`() {
        // given
        val auction: Auction = thereIsAuction()

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post("$baseUrl/${auction.id}/operations/reject"))
                .andExpect { MockMvcResultMatchers.status().isAccepted }

        // then
        val auctionAfterAcceptance: Auction? = auction.id?.let { auctionRepository.findById(it).orElseThrow() }
        val isRejected: Boolean = auctionAfterAcceptance?.isRejected() ?: false
        Assertions.assertThat(isRejected).isTrue()
    }

    @Test
    fun `should archive auction`() {
        // given
        val auction: Auction = thereIsAuction()

        // when
        mockMvc.perform(
                MockMvcRequestBuilders.post("$baseUrl/${auction.id}/operations/archive"))
                .andExpect { MockMvcResultMatchers.status().isAccepted }

        // then
        val auctionAfterAcceptance: Auction? = auction.id?.let { auctionRepository.findById(it).orElseThrow() }
        val isArchived: Boolean = auctionAfterAcceptance?.isArchived() ?: false
        Assertions.assertThat(isArchived).isTrue()
    }


    private fun thereIsAuction(): Auction {
        val electronics = Category(UUID.randomUUID().toString(), "Electronics")
        val headphones = Category(UUID.randomUUID().toString(), "Headphones")
        val wirelessHeadphones = Category(UUID.randomUUID().toString(), "Wireless Headphones")
        val categoryPath = CategoryPath(
            pathElements = mutableListOf(electronics, headphones, wirelessHeadphones)
        )

        val city: City = thereIsCity()

        val auction = Auction(
            name = "Wireless Samsung headphones",
            description = "Best headphones you can have",
            price = 1.23,
            auctioneerId = "user-id",
            category = wirelessHeadphones,
            categoryPath = categoryPath,
            productCondition = Condition.NEW,
            cityId = city.id!!,
            cityName = city.name,
            location = GeoJsonPoint(city.latitude, city.longitude),
            expiresAt = Instant.now().plusSeconds(Duration.ofDays(1).toSeconds()),
        )

        return auctionRepository.save(auction)
    }

    private fun thereIsCity(): City {
        return cityRepository.save(
                City(
                    id = "id1",
                    name = "Lublin",
                    type = "village",
                    province = "Province-1",
                    district = "District-1",
                    commune = "Commune-1",
                    latitude = 51.25,
                    longitude = 22.5666
                )
        )
    }

    // zakomentowałem bo nigdzie nie używana funkcja
    // private fun defaultExpiration(): Instant = Instant.now().plusSeconds(Duration.ofDays(10).toSeconds())

}