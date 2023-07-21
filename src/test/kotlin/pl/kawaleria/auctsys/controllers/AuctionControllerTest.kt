package pl.kawaleria.auctsys.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.models.Auction
import pl.kawaleria.auctsys.models.Category
import pl.kawaleria.auctsys.repositories.AuctionRepository
import pl.kawaleria.auctsys.responses.PagedAuctions


/*To run this test you need running Docker environment*/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
class AuctionControllerTest {

    companion object {
        @Container
        val mongoDBContainer = MongoDBContainer("mongo:4.4.2").apply {
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }

        val LOGGER = getLogger(AuctionControllerTest::class.java)
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

    @Test
    @DisplayName("Gdy fraza wyszukiwania jest nieokreślona i kategoria wyszukiwania jest nieokreślona, system powinien zwrócić wybraną stronę spośród wszystkich aukcji")
    fun shouldSearchAmongAllAuctions_WhenSearchPhraseIsNotSpecifiedAndAuctionCategoryIsNotSpecified() {
        // GIVEN

        /*
        Sekcja given zawiera początkowe założenia testu, tutaj inicjalizujesz np początkowy stan systemu dla konkretnego testu
        np. Zakładamy że na starcie w systemie mamy 2 aukcje, trzeba je wstawić do bazy danych, w tym celu można wstrzyknąć repo i dodać obiekty
        */

        val auctions = listOf(
                Auction(
                        name = "Auction 1",
                        category = Category.MODA,
                        description = "Headphones",
                        price = 1.23,
                        auctioneerId = "user-id"
                ),
                Auction(
                        name = "Auction 2",
                        category = Category.SPORT,
                        description = "Sneakers",
                        price = 5.99,
                        auctioneerId = "user-id"
                )
        )

        auctionRepository.saveAll(auctions)

        val selectedPage = 0
        val selectedPageSize = 10
        val expectedPageCount = 1

        // WHEN

        /*
            Sekcja when - tutaj poprzez inteterfejs mockMvc strzelamy sobie do endpointa ustawiając odpowiednie parametry
        */

        val result = mockMvc.perform(
                get("/auction-service/auctions")
                        .param("page", selectedPage.toString())
                        .param("pageSize", selectedPageSize.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()


        // THEN
        val responseJson = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        LOGGER.info("Received response from rest controller: {}", responseJson)

        Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(auctions.size)
        Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
        Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
    }

    @Test
    @DisplayName("Gdy fraza wyszukiwania jest określona i kategoria wyszukiwania jest nieokreślona, system powinien zwrócić wybraną stronę spośród aukcji zawierających wybraną frazę")
    fun shouldSearchAmongAuctionsWithSelectedPhrase_WhenSearchPhraseIsSpecifiedAndAuctionCategoryIsNotSpecified() {
        // GIVEN
        givenTestAuctions()

        val selectedPage = 0
        val selectedPageSize = 10
        val expectedPageCount = 1
        val selectedSearchPhrase = "JBL"
        val expectedFilteredAuctionsCount = 2


        // WHEN
        val result = mockMvc.perform(
                get("/auction-service/auctions")
                        .param("page", selectedPage.toString())
                        .param("pageSize", selectedPageSize.toString())
                        .param("searchPhrase", selectedSearchPhrase)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()


        // THEN
        val responseJson = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        LOGGER.info("Received response from rest controller: {}", responseJson)


        Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
        Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
            auction.name?.contains(selectedSearchPhrase, ignoreCase = true) ?: false
        }
        Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
        Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
    }

    @Test
    @DisplayName("Gdy fraza wyszukiwania jest określona i kategoria wyszukiwania jest określona, system powinien zwrócić wybraną stronę spośród aukcji wybranej kategorii zawierających wybraną frazę")
    fun shouldSearchAmongAuctionsWithSelectedPhraseAndCategory() {
        // GIVEN
        givenTestAuctions()

        val selectedPage = 0
        val selectedPageSize = 10
        val expectedPageCount = 1
        val selectedSearchPhrase = "JBL"
        val selectedCategory = "MODA"
        val expectedFilteredAuctionsCount = 1


        // WHEN
        val result = mockMvc.perform(
                get("/auction-service/auctions")
                        .param("page", selectedPage.toString())
                        .param("pageSize", selectedPageSize.toString())
                        .param("searchPhrase", selectedSearchPhrase)
                        .param("category", selectedCategory)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()


        // THEN
        val responseJson = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        LOGGER.info("Received response from rest controller: {}", responseJson)

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
    @DisplayName("Gdy fraza wyszukiwania jest pusta i kategoria wyszukiwania jest określona, system powinien zwrócić wybraną stronę spośród aukcji wybranej kategorii")
    fun shouldSearchAmongAuctionsWithSelectedCategory_WhenSelectedPhraseIsBlankAndCategoryIsSpecified() {
        // GIVEN
        givenTestAuctions()

        val selectedPage = 0
        val selectedPageSize = 10
        val expectedPageCount = 1
        val selectedSearchPhrase = " "
        val selectedCategory = "SPORT"
        val expectedFilteredAuctionsCount = 2


        // WHEN
        val result = mockMvc.perform(
                get("/auction-service/auctions")
                        .param("page", selectedPage.toString())
                        .param("pageSize", selectedPageSize.toString())
                        .param("searchPhrase", selectedSearchPhrase)
                        .param("category", selectedCategory)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()


        // THEN
        val responseJson = result.response.contentAsString
        val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
        LOGGER.info("Received response from rest controller: {}", responseJson)

        Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
        Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
            auction.category?.equals(Category.valueOf(selectedCategory)) ?: false
        }
        Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
        Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
    }

    private fun givenTestAuctions() {
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
    }
}
