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
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
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

private const val auctionCrudUrl: String = "/auction-service/users/user-id/auctions"
private const val auctionSearchUrl: String = "/auction-service/auctions"

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
        mongoTemplate.dropCollection("categories")
        mongoTemplate.dropCollection("cities")
    }

    @Nested
    inner class AuctionsSearchTests {

        @Test
        fun `should return selected page from all auctions when search phrase and search category are not specified`() {
            // given
            val existingAuctionsCount: Int = thereAreAuctions().first.size

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1

            // when
            val result: MvcResult = mockMvc.perform(
                    get(auctionSearchUrl)
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
                    get(auctionSearchUrl)
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
                    get(auctionSearchUrl)
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
                    get(auctionSearchUrl)
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
                auction.categoryPath.pathElements.map{ it.name }.any { it == selectedCategory }
            }
            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
        }

        @Test
        fun `should search among auctions with selected city and radius`() {
            // given
            val cities: List<City> = thereAreAuctions().second

            val selectedPage = 0
            val selectedPageSize = 10
            val selectedCityId: String = cities[0].id!!
            val selectedRadius = 16.0

            val expectedPageCount = 1
            val expectedFilteredAuctionsCount = 2

            // when
            val result: MvcResult = mockMvc.perform(
                get(auctionSearchUrl)
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("cityId", selectedCityId)
                    .param("radius", selectedRadius.toString())
                    .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            logger.info(responseJson)
        }
    }

    @Nested
    inner class AuctionsGettersTests {

        @Test
        fun `should return specific auction`() {
            // given
            val auction: Auction = thereIsAuction()
            val auctionId: String? = auction.id

            // when
            val result: MvcResult = mockMvc.perform(
                    get("$auctionCrudUrl/$auctionId")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val foundAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(foundAuction.id).isEqualTo(auction.id)
            Assertions.assertThat(foundAuction.name).isEqualTo(auction.name)
            Assertions.assertThat(foundAuction.cityId).isEqualTo(auction.cityId)
            Assertions.assertThat(foundAuction.productCondition).isEqualTo(auction.productCondition)
            Assertions.assertThat(foundAuction.category).isEqualTo(auction.category)
            Assertions.assertThat(foundAuction.description).isEqualTo(auction.description)
            Assertions.assertThat(foundAuction.price).isEqualTo(auction.price)
            Assertions.assertThat(foundAuction.auctioneerId).isEqualTo(auction.auctioneerId)
        }


        @Test
        fun `should return list of auctions belonging to the user`() {
            // given
            val expectedNumberOfAuctions: Int = thereAreAuctions().first.size

            // when
            val result: MvcResult = mockMvc.perform(
                    get(auctionCrudUrl)
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
                    get("$auctionCrudUrl/nonExistingAuctionId")
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

        @Test
        fun `should create auction`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val city: City = thereIsCity()
            val location = GeoJsonPoint(city.latitude, city.longitude)

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    description = "Best headphones you can have",
                    price = 1.23,
                    categoryId = category.id,
                    productCondition = Condition.NEW,
                    cityId = city.id!!,
                    cityName = city.name,
                    location = location
            )

            // when
            val result: MvcResult = mockMvc.perform(
                    post(auctionCrudUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(auctionRequestData)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val createdAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(createdAuction.name).isEqualTo(auctionRequestData.name)
            Assertions.assertThat(createdAuction.description).isEqualTo(auctionRequestData.description)
            Assertions.assertThat(createdAuction.price).isEqualTo(auctionRequestData.price)
            Assertions.assertThat(createdAuction.auctioneerId).isEqualTo("user-id")
            Assertions.assertThat(createdAuction.category!!.id).isEqualTo(category.id)
            Assertions.assertThat(createdAuction.productCondition).isEqualTo(auctionRequestData.productCondition)
            Assertions.assertThat(createdAuction.cityId).isEqualTo(auctionRequestData.cityId)
            Assertions.assertThat(createdAuction.cityName).isEqualTo(auctionRequestData.cityName)
            Assertions.assertThat(createdAuction.location).isEqualTo(auctionRequestData.location)
        }

        @Test
        fun `should not create auction with blank name`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val city: City = thereIsCity()
            val location = GeoJsonPoint(city.latitude, city.longitude)

            val auctionRequestData = CreateAuctionRequest(
                    name = "",
                    description = "Headphones",
                    price = 1.23,
                    categoryId = category.id,
                    productCondition = Condition.NEW,
                    cityId = city.id!!,
                    cityName = city.name,
                    location = location
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(auctionCrudUrl)
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
            val city: City = thereIsCity()
            val location = GeoJsonPoint(city.latitude, city.longitude)

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    description = "Headphones",
                    price = 1.23,
                    categoryId = category.id,
                    productCondition = Condition.USED,
                    cityId = city.id!!,
                    cityName = city.name,
                    location = location
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(auctionCrudUrl)
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
            val city: City = thereIsCity()
            val location = GeoJsonPoint(city.latitude, city.longitude)

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Extra Ultra Mega Best Giga Fastest Smoothest Cleanest Cheapest Samsung headphones with Bluetooth",
                    description = "Headphones",
                    price = 1.23,
                    categoryId = category.id,
                    productCondition = Condition.NOT_APPLICABLE,
                    cityId = city.id!!,
                    cityName = city.name,
                    location = location
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(auctionCrudUrl)
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
            val city: City = thereIsCity()
            val location = GeoJsonPoint(city.latitude, city.longitude)

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    description = "Best headphones you can have",
                    price = -13.0,
                    categoryId = category.id,
                    productCondition = Condition.USED,
                    cityId = city.id!!,
                    cityName = city.name,
                    location = location
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(auctionCrudUrl)
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
            val city: City = thereIsCity()
            val location = GeoJsonPoint(city.latitude, city.longitude)

            val auctionRequestData = CreateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    description = "Best headphones you can have;[,.[;.;~??",
                    price = 13.0,
                    categoryId = category.id,
                    productCondition = Condition.NOT_APPLICABLE,
                    cityId = city.id!!,
                    cityName = city.name,
                    location = location
            )

            val expectedErrorMessage = "CreateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    post(auctionCrudUrl)
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

        @Test
        fun `should update name in auction`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val expectedAuctionName = "Wireless Apple headphones"

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = expectedAuctionName,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!,
                    productCondition = oldAuction.productCondition,
                    cityId = oldAuction.cityId,
                    cityName = oldAuction.cityName,
                    location = oldAuction.location
            )

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$auctionCrudUrl/${oldAuction.id}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(expectedAuctionName)
            Assertions.assertThat(updatedAuction.description).isEqualTo(updateAuctionRequest.description)
            Assertions.assertThat(updatedAuction.price).isEqualTo(updateAuctionRequest.price)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(oldAuction.productCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(oldAuction.cityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(oldAuction.cityName)
            Assertions.assertThat(updatedAuction.location).isEqualTo(oldAuction.location)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
        }

        @Test
        fun `should update description and price in auction`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val expectedAuctionDescription = "Wireless headphones with charger and original box"
            val expectedAuctionPrice = 123.45

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = oldAuction.name!!,
                    description = expectedAuctionDescription,
                    price = expectedAuctionPrice,
                    productCondition = oldAuction.productCondition,
                    cityId = oldAuction.cityId,
                    cityName = oldAuction.cityName,
                    location = oldAuction.location
            )

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$auctionCrudUrl/${oldAuction.id}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(oldAuction.name)
            Assertions.assertThat(updatedAuction.description).isEqualTo(expectedAuctionDescription)
            Assertions.assertThat(updatedAuction.price).isEqualTo(expectedAuctionPrice)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(oldAuction.productCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(oldAuction.cityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(oldAuction.cityName)
            Assertions.assertThat(updatedAuction.location).isEqualTo(oldAuction.location)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
        }

        @Test
        fun `should update city in auction`() {
            // given
            val cities: List<City> = thereAreCities()
            val oldAuction: Auction = thereIsAuction()

            val expectedCityId: String = cities[1].id.toString()
            val expectedCityName: String = cities[1].name
            val expectedLocation = GeoJsonPoint(cities[1].latitude, cities[1].longitude)

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = oldAuction.name!!,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!,
                    productCondition = oldAuction.productCondition,
                    cityId = expectedCityId,
                    cityName = expectedCityName,
                    location = expectedLocation
            )

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$auctionCrudUrl/${oldAuction.id}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(oldAuction.name)
            Assertions.assertThat(updatedAuction.description).isEqualTo(oldAuction.description)
            Assertions.assertThat(updatedAuction.price).isEqualTo(oldAuction.price)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(oldAuction.productCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(expectedCityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(expectedCityName)
            Assertions.assertThat(updatedAuction.location).isEqualTo(expectedLocation)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
        }

        @Test
        fun `should update product condition in auction`() {
            // given
            // old auction has Condition.new condition
            val oldAuction: Auction = thereIsAuction()

            val expectedProductCondition: Condition = Condition.USED

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = oldAuction.name!!,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!,
                    productCondition = expectedProductCondition,
                    cityId = oldAuction.cityId,
                    cityName = oldAuction.cityName,
                    location = oldAuction.location
            )

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$auctionCrudUrl/${oldAuction.id}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(objectMapper.writeValueAsString(updateAuctionRequest)))
                    .andExpect(status().isOk())
                    .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse = objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(oldAuction.name)
            Assertions.assertThat(updatedAuction.description).isEqualTo(oldAuction.description)
            Assertions.assertThat(updatedAuction.price).isEqualTo(oldAuction.price)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(expectedProductCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(oldAuction.cityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(oldAuction.cityName)
            Assertions.assertThat(updatedAuction.location).isEqualTo(oldAuction.location)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
        }

        @Test
        fun `should not update auction because of negative new price`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val newPrice = -15.45387

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = oldAuction.name!!,
                    description = oldAuction.description!!,
                    price = newPrice,
                    productCondition = oldAuction.productCondition,
                    cityId = oldAuction.cityId,
                    cityName = oldAuction.cityName,
                    location = oldAuction.location
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$auctionCrudUrl/${oldAuction.id}")
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

            val newName = "Bike"

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = newName,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!,
                    productCondition = oldAuction.productCondition,
                    cityId = oldAuction.cityId,
                    cityName = oldAuction.cityName,
                    location = oldAuction.location
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$auctionCrudUrl/${oldAuction.id}")
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
                    price = oldAuction.price!!,
                    productCondition = oldAuction.productCondition,
                    cityId = oldAuction.cityId,
                    cityName = oldAuction.cityName,
                    location = oldAuction.location
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result:MvcResult = mockMvc.perform(
                    put("$auctionCrudUrl/${oldAuction.id}")
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

            val newName = "Headphones?"

            val newAuction = UpdateAuctionRequest(
                    name = newName,
                    description = oldAuction.description!!,
                    price = oldAuction.price!!,
                    productCondition = oldAuction.productCondition,
                    cityId = oldAuction.cityId,
                    cityName = oldAuction.cityName,
                    location = oldAuction.location
            )

            val expectedErrorMessage = "UpdateAuctionRequest is not valid"

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$auctionCrudUrl/${oldAuction.id}")
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
            val city: City = thereIsCity()
            val location = GeoJsonPoint(city.latitude, city.longitude)

            val updateAuctionRequest = UpdateAuctionRequest(
                    name = "Wireless Samsung headphones",
                    description = "Best headphones you can have",
                    price = 1.23,
                    productCondition = Condition.USED,
                    cityId = city.id!!,
                    cityName = city.name,
                    location = location
            )

            val expectedErrorMessage = "Accessed auction does not exist"

            // when
            val result: MvcResult = mockMvc.perform(
                    put("$auctionCrudUrl/nonExistingAuctionId")
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

        @Test
        fun `should delete auction`() {
            // given
            val auctionId: String = thereIsAuction().id!!

            // when
            mockMvc.perform(
                    delete("$auctionCrudUrl/$auctionId")
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
                    delete("$auctionCrudUrl/nonExistingAuctionId")
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

    private fun thereAreAuctions(): Pair<List<Auction>, List<City>> {
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
        val tShirts = Category(UUID.randomUUID().toString(), "TShirts")
        val tShirtsCategoryPath = CategoryPath(
                pathElements = mutableListOf(clothing, unisexClothing, tShirts)
        )

        val cities: List<City> = thereAreCities()

        val auctions: List<Auction> = listOf(
                Auction(
                        name = "Wireless Samsung headphones",
                        description = "Best headphones you can have",
                        price = 1.23,
                        auctioneerId = "user-id",
                        category = wirelessHeadphones,
                        categoryPath = wirelessHeadphonesCategoryPath,
                        productCondition = Condition.NEW,
                        cityId = cities[0].id!!,
                        cityName = cities[0].name,
                        location = GeoJsonPoint(cities[0].latitude, cities[0].longitude),
                        expiresAt = Instant.now().plusSeconds(Duration.ofDays(1).toSeconds()),
                ),
                Auction(
                        name = "Wireless JBL headphones",
                        description = "Worst headphones you can have",
                        price = 4.56,
                        auctioneerId = "user-id",
                        category = speakers,
                        categoryPath = speakersCategoryPath,
                        productCondition = Condition.USED,
                        cityId = cities[1].id!!,
                        cityName = cities[1].name,
                        location = GeoJsonPoint(cities[1].latitude, cities[1].longitude),
                        expiresAt = defaultExpiration(),
                ),
                Auction(
                        name = "Wireless Sony headphones",
                        description = "Best sony headphones you can have",
                        price = 78.9,
                        auctioneerId = "user-id",
                        category = tShirts,
                        categoryPath = tShirtsCategoryPath,
                        productCondition = Condition.USED,
                        cityId = cities[2].id!!,
                        cityName = cities[2].name,
                        location = GeoJsonPoint(cities[2].latitude, cities[2].longitude),
                        expiresAt = defaultExpiration(),
                ),
                Auction(
                        name = "Wireless Apple headphones",
                        description = "Worst apple headphones you can have",
                        price = 159.43,
                        auctioneerId = "user-id",
                        category = tShirts,
                        categoryPath = tShirtsCategoryPath,
                        productCondition = Condition.NOT_APPLICABLE,
                        cityId = cities[3].id!!,
                        cityName = cities[3].name,
                        location = GeoJsonPoint(cities[3].latitude, cities[3].longitude),
                        expiresAt = defaultExpiration(),
                )
        )

        return Pair(auctionRepository.saveAll(auctions), cities)
    }

    private fun thereAreCities(): List<City> {
        return cityRepository.saveAll(listOf(
                City(
                        name = "Lublin",
                        type = "village",
                        province = "Province-1",
                        district = "District-1",
                        commune = "Commune-1",
                        latitude = 51.25,
                        longitude = 22.5666
                        ),
                City(
                        name = "Świdnik",
                        type = "village",
                        province = "Province-2",
                        district = "District-2",
                        commune = "Commune-2",
                        latitude = 51.2197,
                        longitude = 22.7
                ),
                City(
                        name = "Dorohucz",
                        type = "village",
                        province = "Province-3",
                        district = "District-3",
                        commune = "Commune-3",
                        latitude = 51.1625,
                        longitude = 23.0088
                ),
                City(
                        name = "Chełm",
                        type = "village",
                        province = "Province-4",
                        district = "District-4",
                        commune = "Commune-4",
                        latitude = 51.1322,
                        longitude = 23.4777
                )
        ))
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
