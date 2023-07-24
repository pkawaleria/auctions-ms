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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.dtos.responses.PagedAuctions
import pl.kawaleria.auctsys.models.Auction
import pl.kawaleria.auctsys.models.Category
import pl.kawaleria.auctsys.repositories.AuctionRepository


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
        System.setProperty("spring.data.mongodb.uri", mongo.getReplicaSetUrl());
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
    inner class AuctionsCreationTests {
        // TODO: write this tests
    }

    @Nested
    inner class AuctionsSearchTests {
        @Test
        fun `should return selected page from all auctions when search phrase and search category are not specified`() {
            // given
            val existingAuctionsCount = thereAreAuctions()

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1

            // when
            val result = mockMvc.perform(
                    get("/auction-service/auctions")
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
                    get("/auction-service/auctions")
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
                    get("/auction-service/auctions")
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
                    get("/auction-service/auctions")
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
        return auctions.size;
    }
}
