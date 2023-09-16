package pl.kawaleria.auctsys.auctions

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.assertj.core.api.Assertions
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
import pl.kawaleria.auctsys.auctions.domain.CityFacade
import pl.kawaleria.auctsys.auctions.domain.CityRepository
import pl.kawaleria.auctsys.auctions.dto.responses.PagedCities

private const val baseUrl = "/admin"

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

        val expectedMessage = "Successfully added cities"

        // when
        val result: MvcResult = mockMvc.perform(
                post("$baseUrl/import-cities"))
                .andExpect(status().isOk())
                .andReturn()

        // then
        Assertions.assertThat(result.response.contentAsString).isEqualTo(expectedMessage)
    }

    @Test
    fun `should not insert cities from json file if document is not empty`() {
        // given
        cityFacade.importCities()

        val expectedMessage = "Cities document is not empty"

        // when
        val result: MvcResult = mockMvc.perform(
                post("$baseUrl/import-cities"))
                .andExpect(status().isBadRequest())
                .andReturn()

        // then
        Assertions.assertThat(result.response.contentAsString).isEqualTo(expectedMessage)
    }

    @Test
    fun `should delete cities from database if document is not empty`() {
        // given
        cityFacade.importCities()

        val expectedMessage = "Successfully deleted cities"

        // when
        val result: MvcResult = mockMvc.perform(
                delete("$baseUrl/delete-cities"))
                .andExpect(status().isOk())
                .andReturn()

        // then
        Assertions.assertThat(result.response.contentAsString).isEqualTo(expectedMessage)
    }

    @Test
    fun `should not delete cities from database if document is empty`() {
        // given
        cityRepository.deleteAll()

        val expectedMessage = "Cities document is empty"

        // when
        val result: MvcResult = mockMvc.perform(
                delete("$baseUrl/delete-cities"))
                .andExpect(status().isBadRequest())
                .andReturn()

        // then
        Assertions.assertThat(result.response.contentAsString).isEqualTo(expectedMessage)
    }

    @Test
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
                get("$baseUrl/cities")
                        .param("page", selectedPage.toString())
                        .param("pageSize", selectedPageSize.toString())
                        .param("searchCityName", selectedCityNamePhrase)
                        .contentType(MediaType.APPLICATION_JSON))
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