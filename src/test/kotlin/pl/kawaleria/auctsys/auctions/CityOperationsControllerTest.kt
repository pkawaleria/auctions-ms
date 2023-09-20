package pl.kawaleria.auctsys.auctions

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
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
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.*
import pl.kawaleria.auctsys.auctions.domain.CityFacade
import pl.kawaleria.auctsys.auctions.domain.CityRepository
import pl.kawaleria.auctsys.auctions.dto.responses.PagedCities

private const val baseUrl = "/cities"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CityOperationsControllerTest {

    private val mongo: MongoDBContainer = MongoDBContainer("mongo").apply {
        start()
    }

    init {
        System.setProperty("spring.data.mongodb.uri", mongo.replicaSetUrl)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AuctionControllerTest::class.java)
    }

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var cityRepository: CityRepository

    @Autowired
    private lateinit var cityFacade: CityFacade

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var mockMvc: MockMvc

    @AfterEach
    fun cleanUp() {
        mongoTemplate.dropCollection("cities")
    }

    @Test
    fun `should insert cities from json file if document is empty`() {
        // given
        cityRepository.deleteAll()

        // when
        val result: MvcResult = mockMvc.perform(
            post("$baseUrl/import")
                .withAuthenticatedAdmin()
        )
            .andExpect(status().isOk())
            .andReturn()
    }


    @Test
    fun `should not insert cities from json file if document is not empty`() {
        // given
        cityFacade.importCities()

        // when
        val result: MvcResult = mockMvc.perform(
            post("$baseUrl/import")
                .withAuthenticatedAdmin()
        )
            .andExpect(status().isBadRequest())
            .andReturn()

    }

    @Test
    fun `should delete cities from database if document is not empty`() {
        // given
        cityFacade.importCities()

        // when
        val result: MvcResult = mockMvc.perform(
            delete("$baseUrl/clear")
                .withAuthenticatedAdmin()
        )

            .andExpect(status().isOk())
            .andReturn()
    }

    @Test
    fun `should not delete cities from database if document is empty`() {
        // given
        cityRepository.deleteAll()

        // when
        val result: MvcResult = mockMvc.perform(
            delete("$baseUrl/clear")
                .withAuthenticatedAdmin()
        )
            .andExpect(status().isBadRequest())
            .andReturn()
    }

    @Test
    @Disabled
    //TODO: Investigate this test
    fun `should search among cities with provided city name phrase and return array with cities`() {
        // given
        cityFacade.importCities()

        val selectedPage = 0
        val selectedPageSize = 10
        val selectedCityNamePhrase = "Abramowi"

        val expectedPageCount = 1
        val expectedFilteredCitiesCount = 3

        // when
        val result: MvcResult = mockMvc.perform(
            get("$baseUrl/search")
                .withAuthenticatedAuctioneer()
                .param("page", selectedPage.toString())
                .param("pageSize", selectedPageSize.toString())
                .param("searchCityName", selectedCityNamePhrase)
                .contentType(MediaType.APPLICATION_JSON)
        )

            .andExpect(status().isOk())
            .andReturn()

        // then
        val responseJson: String = result.response.contentAsString
        val pagedCities: PagedCities = objectMapper.readValue(responseJson, PagedCities::class.java)
        logger.info("Received response from rest controller: {}", responseJson)

        Assertions.assertThat(pagedCities.cities.size).isEqualTo(expectedFilteredCitiesCount)
        Assertions.assertThat(pagedCities.pageCount).isEqualTo(expectedPageCount)
    }
}