package pl.kawaleria.auctsys.auctions

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.auctions.domain.*
import java.time.Duration
import java.time.Instant

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
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var mockMvc: MockMvc

    @AfterEach
    fun cleanUp() {
        mongoTemplate.dropCollection("auctions")
    }


    @Test
    fun `should accept auction`() {
        // given
        val auction = thereIsAuction()

        // when

        mockMvc.perform(
                MockMvcRequestBuilders.post("/auction-service/auctions/${auction.id}/operations/accept"))
                .andExpect{ MockMvcResultMatchers.status().isAccepted }

        // then
        val auctionAfterAcceptance: Auction? = auction.id?.let { auctionRepository.findById(it).orElseThrow() }
        val isAccepted = auctionAfterAcceptance?.isAccepted() ?: false
        Assertions.assertThat(isAccepted).isTrue()
    }

    @Test
    fun `should reject auction`() {
        // given
        val auction = thereIsAuction()

        // when

        mockMvc.perform(
                MockMvcRequestBuilders.post("/auction-service/auctions/${auction.id}/operations/reject"))
                .andExpect{ MockMvcResultMatchers.status().isAccepted }

        // then
        val auctionAfterAcceptance: Auction? = auction.id?.let { auctionRepository.findById(it).orElseThrow() }
        val isRejected = auctionAfterAcceptance?.isRejected() ?: false
        Assertions.assertThat(isRejected).isTrue()
    }

    @Test
    fun `should archive auction`() {
        // given
        val auction = thereIsAuction()

        // when

        mockMvc.perform(
                MockMvcRequestBuilders.post("/auction-service/auctions/${auction.id}/operations/archive"))
                .andExpect{ MockMvcResultMatchers.status().isAccepted }

        // then
        val auctionAfterAcceptance: Auction? = auction.id?.let { auctionRepository.findById(it).orElseThrow() }
        val isArchived = auctionAfterAcceptance?.isArchived() ?: false
        Assertions.assertThat(isArchived).isTrue()
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

}