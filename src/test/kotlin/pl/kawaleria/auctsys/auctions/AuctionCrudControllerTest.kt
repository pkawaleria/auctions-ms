package pl.kawaleria.auctsys.auctions

import com.fasterxml.jackson.databind.ObjectMapper
import com.redis.testcontainers.RedisContainer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.*
import pl.kawaleria.auctsys.auctions.domain.*
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.AuctionDetailedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.AuctionSimplifiedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.PagedAuctions
import pl.kawaleria.auctsys.auctions.dto.responses.toDetailedResponse
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.categories.domain.CategoryRepository
import pl.kawaleria.auctsys.categories.dto.requests.CategoryCreateRequest
import pl.kawaleria.auctsys.categories.dto.responses.CategoryResponse
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.log

private const val baseUrl: String = "/auction-service/auctions"

/*To run this test you need running Docker environment*/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuctionControllerTest {

    private val redis: RedisContainer = RedisTestContainer.instance
    private val mongo: MongoDBContainer = MongoTestContainer.instance

    init {
        System.setProperty("spring.data.mongodb.uri", mongo.replicaSetUrl)
        System.setProperty("auction.text.verification.enabled", "false")
        System.setProperty("spring.data.redis.host", redis.host)
        System.setProperty("spring.data.redis.port", redis.firstMappedPort?.toString() ?: "6379")
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
    private lateinit var categoryRepository: CategoryRepository

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

        private val auctionsSearchUrl: String = "$baseUrl/search"

        @Test
        fun `should return selected page from all auctions when search phrase and search category are not specified`() {
            // given
            val existingAuctionsCount: Int = thereAreAuctions().first.size

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1

            // when
            val result: MvcResult = mockMvc.perform(
                get(auctionsSearchUrl)
                    .withAuthenticatedAuctioneer()
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .contentType(MediaType.APPLICATION_JSON)
            )

                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received responses from rest controller: {}", responseJson)

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
                get(auctionsSearchUrl)
                    .withAuthenticatedAuctioneer()
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("searchPhrase", selectedSearchPhrase)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received responses from rest controller: {}", responseJson)


            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.name.contains(selectedSearchPhrase, ignoreCase = true)
            }
            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
        }

        @Test
        fun `should return sorted auctions`() {
            // given
            thereAreAuctions()

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1
            val expectedFilteredAuctionsCount = 4

            // when
            val result: MvcResult = mockMvc.perform(
                get(auctionsSearchUrl)
                    .withAuthenticatedAuctioneer()
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("sortBy", "NAME")
                    .param("sortOrder", "ASC")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received responses from rest controller: {}", responseJson)

            val auctionNames = pagedAuctions.auctions.map { it.name }
            Assertions.assertThat(auctionNames).isSorted()
            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
        }

        @Test
        fun `should search auctions in price range`() {
            // given
            thereAreAuctions()

            val selectedPage = 0
            val selectedPageSize = 10
            val expectedPageCount = 1
            val expectedFilteredAuctionsCount = 2

            // when
            val result: MvcResult = mockMvc.perform(
                get(auctionsSearchUrl)
                    .withAuthenticatedAuctioneer()
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("priceFrom", "1")
                    .param("priceTo", "5")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received responses from rest controller: {}", responseJson)

            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
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
                get(auctionsSearchUrl)
                    .withAnonymousUser()
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("searchPhrase", selectedSearchPhrase)
                    .param("categoryNamePhrase", selectedCategory)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received responses from rest controller: {}", responseJson)

            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.name.contains(selectedSearchPhrase, ignoreCase = true)
            }
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.categoryPath.pathElements.map { it.name }.any { it == selectedCategory }
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
                get(auctionsSearchUrl)
                    .withAuthenticatedAuctioneer()
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("searchPhrase", selectedSearchPhrase)
                    .param("categoryNamePhrase", selectedCategory)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)
            logger.info("Received responses from rest controller: {}", responseJson)

            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
            Assertions.assertThat(pagedAuctions.auctions).allMatch { auction ->
                auction.categoryPath.pathElements.map { it.name }.any { it == selectedCategory }
            }
            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.pageNumber).isEqualTo(selectedPage)
        }

        @Test
        fun `should search among auctions with selected city and without radius`() {
            // given
            val cities: List<City> = thereAreAuctions().second

            val selectedPage = 0
            val selectedPageSize = 10
            val selectedCityId: String = cities[0].id

            val expectedPageCount = 1
            val expectedFilteredAuctionsCount = 1

            // when
            val result: MvcResult = mockMvc.perform(
                get(auctionsSearchUrl)
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("cityId", selectedCityId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)

            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
        }

        @Test
        fun `should search among auctions with selected city and radius`() {
            // given
            val cities: List<City> = thereAreAuctions().second

            val selectedPage = 0
            val selectedPageSize = 10
            val selectedCityId: String = cities.first().id
            val selectedRadius = 16.0

            val expectedPageCount = 1
            val expectedFilteredAuctionsCount = 2

            // when
            val result: MvcResult = mockMvc.perform(
                get(auctionsSearchUrl)
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("cityId", selectedCityId)
                    .param("radius", selectedRadius.toString())
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)

            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
        }

        @Test
        fun `should search among auctions with selected province`() {
            // given
            val cities: List<City> = thereAreAuctions().second

            val selectedPage = 0
            val selectedPageSize = 10
            val selectedProvince: String = cities.first().province

            val expectedPageCount = 1
            val expectedFilteredAuctionsCount = 1
            // when

            val result: MvcResult = mockMvc.perform(
                get(auctionsSearchUrl)
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("province", selectedProvince)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val pagedAuctions: PagedAuctions = objectMapper.readValue(responseJson, PagedAuctions::class.java)

            Assertions.assertThat(pagedAuctions.pageCount).isEqualTo(expectedPageCount)
            Assertions.assertThat(pagedAuctions.auctions.size).isEqualTo(expectedFilteredAuctionsCount)
        }

        @Test
        fun `should not search among auctions with radius only`() {
            // given
            thereAreAuctions()

            val selectedPage = 0
            val selectedPageSize = 10
            val selectedRadius = 16.0

            // when then
            mockMvc.perform(
                get(auctionsSearchUrl)
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("radius", selectedRadius.toString())
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should not search among auctions with selected city and radius out of bounds`() {
            val cities: List<City> = thereAreAuctions().second

            val selectedPage = 0
            val selectedPageSize = 10
            val selectedCityId: String = cities.first().id
            val selectedRadius = 55.0

            // when then
            mockMvc.perform(
                get(auctionsSearchUrl)
                    .param("page", selectedPage.toString())
                    .param("pageSize", selectedPageSize.toString())
                    .param("cityId", selectedCityId)
                    .param("radius", selectedRadius.toString())
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    inner class AuctionsGettersTests {
        private val singleAuctionBaseUrl: String = "/auction-service/auctions"
        private val userAuctionsBaseUrl: String = "/auction-service/users/$AUCTIONEER_ID_UNDER_TEST/auctions"

        @Test
        fun `should return specific auction`() {
            // given
            val auction: Auction = thereIsAuction()

            // when
            val result: MvcResult = mockMvc.perform(
                get("$singleAuctionBaseUrl/${auction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val foundAuction: AuctionDetailedResponse =
                objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            // Custom comparator for OffsetDateTime to ignore microseconds
            val offsetDateTimeComparator = Comparator<Instant> { a, b ->
                a.truncatedTo(ChronoUnit.MILLIS).compareTo(b.truncatedTo(ChronoUnit.MILLIS))
            }

            Assertions.assertThat(foundAuction)
                .usingRecursiveComparison()
                .withComparatorForType(offsetDateTimeComparator, Instant::class.java)
                .isEqualTo(auction.toDetailedResponse(viewCount = 1L))
        }

        @ParameterizedTest
        @EnumSource(AuctionStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["ACCEPTED"])
        fun `should return not found trying to get auction with status other than accepted`(status: AuctionStatus) {
            // given
            val auction: Auction = thereIsAuction(status)

            // when
            mockMvc.perform(
                get("$singleAuctionBaseUrl/${auction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return list of auctions belonging to the user`() {
            // given
            val expectedNumberOfAuctions: Int = thereAreAuctions().first.size

            // when
            val result: MvcResult = mockMvc.perform(
                get(userAuctionsBaseUrl)
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val mappedAuctions: List<AuctionSimplifiedResponse> = objectMapper.readValue(
                responseJson,
                objectMapper.typeFactory.constructCollectionType(
                    List::class.java,
                    AuctionSimplifiedResponse::class.java
                )
            )
            val responseNumberOfAuctions: Int = mappedAuctions.size

            Assertions.assertThat(responseNumberOfAuctions).isEqualTo(expectedNumberOfAuctions)
        }

        @Test
        fun `should return empty list of auctions of non-existing user`() {
            // given
            val expectedNumberOfAuctions = 0

            // when
            val nonexistentUserId = "nonExistingUserId"
            val result: MvcResult = mockMvc.perform(
                get("/auction-service/users/$nonexistentUserId/auctions")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val mappedAuctions: List<AuctionSimplifiedResponse> = objectMapper.readValue(
                responseJson,
                objectMapper.typeFactory.constructCollectionType(
                    List::class.java,
                    AuctionSimplifiedResponse::class.java
                )
            )
            val responseNumberOfAuctions: Int = mappedAuctions.size

            Assertions.assertThat(responseNumberOfAuctions).isEqualTo(expectedNumberOfAuctions)
        }

        @Test
        fun `should not return a non-existing auction`() {
            // given
            val nonexistentAuctionId = "nonExistingAuctionId"

            // when then
            mockMvc.perform(
                get("$singleAuctionBaseUrl/$nonexistentAuctionId")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class AuctionsCreationTests {

        @Test
        fun `should create auction`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val city: City = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                name = "Wireless Samsung headphones",
                categoryId = category.id,
                description = "Best headphones you can have",
                price = 1.23,
                productCondition = Condition.NEW,
                cityId = city.id,
                phoneNumber = "123456780"
            )

            // when
            val result: MvcResult = mockMvc.perform(
                post(baseUrl)
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData))
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val createdAuction: AuctionDetailedResponse =
                objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(createdAuction.name).isEqualTo(auctionRequestData.name)
            Assertions.assertThat(createdAuction.cityId).isEqualTo(auctionRequestData.cityId)
            Assertions.assertThat(createdAuction.productCondition).isEqualTo(auctionRequestData.productCondition)
            Assertions.assertThat(createdAuction.description).isEqualTo(auctionRequestData.description)
            Assertions.assertThat(createdAuction.price).isEqualTo(auctionRequestData.price)
            Assertions.assertThat(createdAuction.auctioneerId).isEqualTo(AUCTIONEER_ID_UNDER_TEST)
        }

        @Test
        fun `should not create auction with blank name`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val city: City = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                name = "",
                description = "Headphones",
                price = 1.23,
                categoryId = category.id,
                productCondition = Condition.NEW,
                cityId = city.id,
                phoneNumber = "123456780"
            )

            // when then
            mockMvc.perform(
                post(baseUrl)
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should not authorize anonymous user to create auction`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val city: City = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                name = "That is just excellent name",
                description = "Headphones".repeat(4),
                price = 1.23,
                categoryId = category.id,
                productCondition = Condition.NEW,
                cityId = city.id,
                phoneNumber = "123456780"
            )

            // when
            mockMvc.perform(
                post(baseUrl)
                    .withAnonymousUser()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData))
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `should not create auction with description containing less than 20 characters`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val city: City = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                name = "Wireless Samsung headphones",
                categoryId = category.id,
                description = "Headphones",
                price = 1.23,
                cityId = city.id,
                productCondition = Condition.USED,
                phoneNumber = "123456780"
            )

            // when then
            mockMvc.perform(
                post(baseUrl)
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should not create auction with name containing more than 100 characters`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val city: City = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                name = "Wireless Extra Ultra Mega Best Giga Fastest Smoothest Cleanest Cheapest Samsung headphones with Bluetooth",
                description = "Headphones",
                price = 1.23,
                categoryId = category.id,
                productCondition = Condition.NOT_APPLICABLE,
                cityId = city.id,
                phoneNumber = "123456780"
            )

            // when then
            mockMvc.perform(
                post(baseUrl)
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should not create auction with negative price`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val city: City = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                name = "Wireless Samsung headphones",
                description = "Best headphones you can have",
                price = -13.0,
                categoryId = category.id,
                productCondition = Condition.USED,
                cityId = city.id,
                phoneNumber = "123456780"
            )

            // when then
            mockMvc.perform(
                post(baseUrl)
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should not create auction with too short description`() {
            // given
            val category: CategoryResponse = thereIsSampleCategoryTree()
            val city: City = thereIsCity()

            val auctionRequestData = CreateAuctionRequest(
                name = "Wireless Samsung headphones",
                description = "too short",
                price = 13.0,
                categoryId = category.id,
                productCondition = Condition.NOT_APPLICABLE,
                cityId = city.id,
                phoneNumber = "123456780"
            )

            // when then
            mockMvc.perform(
                post(baseUrl)
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(auctionRequestData))
            )
                .andExpect(status().isBadRequest)
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
                description = oldAuction.description,
                price = oldAuction.price,
                productCondition = oldAuction.productCondition,
                cityId = oldAuction.cityId,
                categoryId = oldAuction.category.id,
                phoneNumber = oldAuction.phoneNumber
            )

            // when
            val result: MvcResult = mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse =
                objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(expectedAuctionName)
            Assertions.assertThat(updatedAuction.description).isEqualTo(updateAuctionRequest.description)
            Assertions.assertThat(updatedAuction.price).isEqualTo(updateAuctionRequest.price)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(oldAuction.productCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(oldAuction.cityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(oldAuction.cityName)
            Assertions.assertThat(updatedAuction.latitude).isEqualTo(oldAuction.location.y)
            Assertions.assertThat(updatedAuction.longitude).isEqualTo(oldAuction.location.x)
            Assertions.assertThat(updatedAuction.category.id).isEqualTo(oldAuction.category.id)
            Assertions.assertThat(updatedAuction.phoneNumber).isEqualTo(oldAuction.phoneNumber)
        }

        @Test
        fun `should update description and price in auction`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val expectedAuctionDescription = "Wireless headphones with charger and original box Wireless headphones with charger and original box"
            val expectedAuctionPrice = 123.45

            val updateAuctionRequest = UpdateAuctionRequest(
                name = oldAuction.name,
                description = expectedAuctionDescription,
                price = expectedAuctionPrice,
                productCondition = oldAuction.productCondition,
                cityId = oldAuction.cityId,
                categoryId = oldAuction.category.id,
                phoneNumber = oldAuction.phoneNumber
            )

            // when
            val result: MvcResult = mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse =
                objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)


            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(oldAuction.name)
            Assertions.assertThat(updatedAuction.description).isEqualTo(expectedAuctionDescription)
            Assertions.assertThat(updatedAuction.price).isEqualTo(expectedAuctionPrice)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(oldAuction.productCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(oldAuction.cityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(oldAuction.cityName)
            Assertions.assertThat(updatedAuction.latitude).isEqualTo(oldAuction.location.y)
            Assertions.assertThat(updatedAuction.longitude).isEqualTo(oldAuction.location.x)
            Assertions.assertThat(updatedAuction.category.id).isEqualTo(oldAuction.category.id)
            Assertions.assertThat(updatedAuction.phoneNumber).isEqualTo(oldAuction.phoneNumber)
        }

        @Test
        fun `should update product condition in auction`() {
            // given
            // old auction has Condition.new condition
            val oldAuction: Auction = thereIsAuction()

            val expectedProductCondition: Condition = Condition.USED

            val updateAuctionRequest = UpdateAuctionRequest(
                name = oldAuction.name,
                description = oldAuction.description,
                price = oldAuction.price,
                productCondition = expectedProductCondition,
                cityId = oldAuction.cityId,
                categoryId = oldAuction.category.id,
                phoneNumber = oldAuction.phoneNumber
            )

            // when
            val result: MvcResult = mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse =
                objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(oldAuction.name)
            Assertions.assertThat(updatedAuction.description).isEqualTo(oldAuction.description)
            Assertions.assertThat(updatedAuction.price).isEqualTo(oldAuction.price)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(expectedProductCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(oldAuction.cityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(oldAuction.cityName)
            Assertions.assertThat(updatedAuction.latitude).isEqualTo(oldAuction.location.y)
            Assertions.assertThat(updatedAuction.longitude).isEqualTo(oldAuction.location.x)
            Assertions.assertThat(updatedAuction.category.id).isEqualTo(oldAuction.category.id)
            Assertions.assertThat(updatedAuction.phoneNumber).isEqualTo(oldAuction.phoneNumber)
        }

        @Test
        fun `should update city in auction`() {
            // given
            val cities: List<City> = thereAreCities()
            val oldAuction: Auction = thereIsAuction()

            val expectedCityId: String = cities.first().id
            val expectedCityName: String = cities.first().name
            val expectedLongitude: Double = cities.first().longitude
            val expectedLatitude: Double = cities.first().latitude

            val updateAuctionRequest = UpdateAuctionRequest(
                name = oldAuction.name,
                description = oldAuction.description,
                price = oldAuction.price,
                productCondition = oldAuction.productCondition,
                cityId = expectedCityId,
                categoryId = oldAuction.category.id,
                phoneNumber = oldAuction.phoneNumber
            )

            // when
            val result: MvcResult = mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse =
                objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)


            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(oldAuction.name)
            Assertions.assertThat(updatedAuction.description).isEqualTo(oldAuction.description)
            Assertions.assertThat(updatedAuction.price).isEqualTo(oldAuction.price)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(oldAuction.productCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(expectedCityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(expectedCityName)
            Assertions.assertThat(updatedAuction.latitude).isEqualTo(expectedLatitude)
            Assertions.assertThat(updatedAuction.longitude).isEqualTo(expectedLongitude)
//            Assertions.assertThat(updatedAuction.category.id).isEqualTo(oldAuction.category.id)
            Assertions.assertThat(updatedAuction.phoneNumber).isEqualTo(oldAuction.phoneNumber)
        }

        @Test
        fun `should update category in auction`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val categoryResponse: CategoryResponse = thereIsSampleCategoryTree()
            val expectedCategoryId: String = categoryResponse.id

            val updateAuctionRequest = UpdateAuctionRequest(
                name = oldAuction.name,
                description = oldAuction.description,
                price = oldAuction.price,
                productCondition = oldAuction.productCondition,
                cityId = oldAuction.cityId,
                categoryId = expectedCategoryId,
                phoneNumber = oldAuction.phoneNumber
            )

            // when
            val result: MvcResult = mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse =
                objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(oldAuction.name)
            Assertions.assertThat(updatedAuction.description).isEqualTo(oldAuction.description)
            Assertions.assertThat(updatedAuction.price).isEqualTo(oldAuction.price)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(oldAuction.productCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(oldAuction.cityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(oldAuction.cityName)
            Assertions.assertThat(updatedAuction.latitude).isEqualTo(oldAuction.location.y)
            Assertions.assertThat(updatedAuction.longitude).isEqualTo(oldAuction.location.x)
            Assertions.assertThat(updatedAuction.category.id).isEqualTo(expectedCategoryId)
            Assertions.assertThat(updatedAuction.phoneNumber).isEqualTo(oldAuction.phoneNumber)
        }

        @Test
        fun `should update phone number in auction`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val expectedPhoneNumber = "123456789"

            val updateAuctionRequest = UpdateAuctionRequest(
                name = oldAuction.name,
                description = oldAuction.description,
                price = oldAuction.price,
                productCondition = oldAuction.productCondition,
                cityId = oldAuction.cityId,
                categoryId = oldAuction.category.id,
                phoneNumber = expectedPhoneNumber
            )

            // when
            val result: MvcResult = mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isOk)
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val updatedAuction: AuctionDetailedResponse =
                objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)


            Assertions.assertThat(updatedAuction.id).isEqualTo(oldAuction.id)
            Assertions.assertThat(updatedAuction.name).isEqualTo(oldAuction.name)
            Assertions.assertThat(updatedAuction.description).isEqualTo(oldAuction.description)
            Assertions.assertThat(updatedAuction.price).isEqualTo(oldAuction.price)
            Assertions.assertThat(updatedAuction.auctioneerId).isEqualTo(oldAuction.auctioneerId)
            Assertions.assertThat(updatedAuction.productCondition).isEqualTo(oldAuction.productCondition)
            Assertions.assertThat(updatedAuction.cityId).isEqualTo(oldAuction.cityId)
            Assertions.assertThat(updatedAuction.cityName).isEqualTo(oldAuction.cityName)
            Assertions.assertThat(updatedAuction.latitude).isEqualTo(oldAuction.location.y)
            Assertions.assertThat(updatedAuction.longitude).isEqualTo(oldAuction.location.x)
            Assertions.assertThat(updatedAuction.category.id).isEqualTo(oldAuction.category.id)
            Assertions.assertThat(updatedAuction.phoneNumber).isEqualTo(expectedPhoneNumber)
        }

        @Test
        fun `should not update auction because of negative new price`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val newPrice = -15.45387

            val updateAuctionRequest = UpdateAuctionRequest(
                name = oldAuction.name,
                description = oldAuction.description,
                price = newPrice,
                productCondition = oldAuction.productCondition,
                cityId = oldAuction.cityId,
                categoryId = oldAuction.category.id,
                phoneNumber = oldAuction.phoneNumber
                )

            // when then
            mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should not update auction because of too short new name`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val newName = "Bike"

            val updateAuctionRequest = UpdateAuctionRequest(
                name = newName,
                description = oldAuction.description,
                price = oldAuction.price,
                productCondition = oldAuction.productCondition,
                cityId = oldAuction.cityId,
                categoryId = oldAuction.category.id,
                phoneNumber = oldAuction.phoneNumber
                )

            // when then
            mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should not update auction because of too long description`() {
            // given
            val oldAuction: Auction = thereIsAuction()
            // this description has 525 chars
            val newDescription: String = "a".repeat(525)

            val updateAuctionRequest = UpdateAuctionRequest(
                name = oldAuction.name,
                description = newDescription,
                price = oldAuction.price,
                productCondition = oldAuction.productCondition,
                cityId = oldAuction.cityId,
                categoryId = oldAuction.category.id,
                phoneNumber = oldAuction.phoneNumber
                )

            // when then
            mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should not update auction when too long name is set`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val newName = "a".repeat(200)

            val newAuction = UpdateAuctionRequest(
                name = newName,
                description = oldAuction.description,
                price = oldAuction.price,
                productCondition = oldAuction.productCondition,
                cityId = oldAuction.cityId,
                categoryId = oldAuction.category.id,
                phoneNumber = oldAuction.phoneNumber
                )

            // when then
            mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newAuction))
            )
                .andExpect(status().isBadRequest)

        }

        @Test
        fun `should not update because of non-existing city`() {
            // given
            val oldAuction: Auction = thereIsAuction()

            val nonExistingCityId = "nonExistingCityId"

            val newAuction = UpdateAuctionRequest(
                name = oldAuction.name,
                description = oldAuction.description,
                price = oldAuction.price,
                productCondition = oldAuction.productCondition,
                cityId = nonExistingCityId,
                categoryId = oldAuction.category.id,
                phoneNumber = oldAuction.phoneNumber
            )

            // when then
            mockMvc.perform(
                put("$baseUrl/${oldAuction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newAuction))
            )
                .andExpect(status().isNotFound)
                .andReturn()
        }

        @Test
        fun `should not update non-existing auction`() {
            // given
            val city: City = thereIsCity()

            val updateAuctionRequest = UpdateAuctionRequest(
                name = "Wireless Samsung headphones",
                description = "Best headphones you can have",
                price = 1.23,
                productCondition = Condition.USED,
                cityId = city.id,
                categoryId = "nonExistingCategoryId",
                phoneNumber = "123456789"
            )

            // when then
            mockMvc.perform(
                put("$baseUrl/nonExistingAuctionId")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateAuctionRequest))
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class AuctionsDeleteTests {

        @Test
        fun `should delete auction`() {
            // given
            val auction: Auction = thereIsAuction()

            // when
            mockMvc.perform(
                delete("$baseUrl/${auction.id}")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNoContent)

            // then
            val doesAuctionExists: Boolean = auctionRepository.existsById(auction.id)

            Assertions.assertThat(doesAuctionExists).isFalse()
        }

        @Test
        fun `should return not found trying to delete non-existing auction`() {
            // given
            val nonExistingAuctionId = "nonExistingAuctionId"

            // when then
            mockMvc.perform(
                delete("$baseUrl/$nonExistingAuctionId")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class AuctionViewsCounterTest {
        private val singleAuctionBaseUrl: String = "/auction-service/auctions"

        @Test
        fun `should count auction views`() {
            // given
            val auction: Auction = thereIsAuction()
            val firstIpAddress = "123.123.123.123"
            val secondIpAddress = "122.123.123.123"

            // when
            mockMvc.perform(
                get("$singleAuctionBaseUrl/${auction.id}")
                    .withAuthenticatedAuctioneer()
                    .header("X-Real-IP", firstIpAddress)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            val result = mockMvc.perform(
                get("$singleAuctionBaseUrl/${auction.id}")
                    .withAuthenticatedAuctioneer()
                    .header("X-Real-IP", secondIpAddress)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn()

            val responseJson: String = result.response.contentAsString
            val foundAuction: AuctionDetailedResponse =
                objectMapper.readValue(responseJson, AuctionDetailedResponse::class.java)

            // then
            Assertions.assertThat(foundAuction.viewCount).isEqualTo(2)
        }
    }

    private fun thereIsAuction(status: AuctionStatus = AuctionStatus.ACCEPTED): Auction {
        val category = pl.kawaleria.auctsys.categories.domain.Category(
            name = "test",
            description = "Przykladowy opis kategorii",
            isTopLevel = true,
            isFinalNode = false
        )

        val createdCategory = categoryRepository.save(category)
        val subCategory = pl.kawaleria.auctsys.categories.domain.Category(
            name = "test2",
            description = "Przykladowy opis podkategorii",
            isTopLevel = false,
            isFinalNode = true,
            parentCategoryId = createdCategory.id
        )
        categoryRepository.save(subCategory)

        val categoryPath: CategoryPath =
            categoryFacade.getFullCategoryPath(category.id).toAuctionCategoryPathModel()

        val city: City = thereIsCity()

        val auction = Auction(
            name = "Wireless Samsung headphones",
            description = "Best headphones you can have",
            price = 1.23,
            auctioneerId = AUCTIONEER_ID_UNDER_TEST,
            category = categoryPath.lastCategory(),
            categoryPath = categoryPath,
            productCondition = Condition.NEW,
            cityId = city.id,
            cityName = city.name,
            province = city.province,
            location = GeoJsonPoint(city.longitude, city.latitude),
            expiresAt = defaultExpiration(),
            status = status,
            thumbnail = byteArrayOf(),
            phoneNumber = "123456780"
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

    private fun thereAreAuctions(status: AuctionStatus = AuctionStatus.ACCEPTED): Pair<List<Auction>, List<City>> {
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
                auctioneerId = AUCTIONEER_ID_UNDER_TEST,
                category = clothing,
                categoryPath = tShirtsCategoryPath,
                productCondition = Condition.NEW,
                cityId = cities[0].id,
                cityName = cities[0].name,
                province = cities[0].province,
                location = GeoJsonPoint(cities[0].longitude, cities[0].latitude),
                expiresAt = defaultExpiration(),
                status = status,
                thumbnail = byteArrayOf(),
                phoneNumber = "123456780"
            ),
            Auction(
                name = "Wireless JBL headphones",
                description = "Worst headphones you can have",
                price = 4.56,
                auctioneerId = AUCTIONEER_ID_UNDER_TEST,
                category = wirelessHeadphones,
                categoryPath = wirelessHeadphonesCategoryPath,
                productCondition = Condition.USED,
                cityId = cities[1].id,
                cityName = cities[1].name,
                province = cities[1].province,
                location = GeoJsonPoint(cities[1].longitude, cities[1].latitude),
                expiresAt = defaultExpiration(),
                status = status,
                thumbnail = byteArrayOf(),
                phoneNumber = "123456780"
            ),
            Auction(
                name = "Wireless Sony headphones",
                description = "Best sony headphones you can have",
                price = 78.9,
                auctioneerId = AUCTIONEER_ID_UNDER_TEST,
                category = headphones,
                categoryPath = speakersCategoryPath,
                productCondition = Condition.USED,
                cityId = cities[2].id,
                cityName = cities[2].name,
                province = cities[2].province,
                location = GeoJsonPoint(cities[2].longitude, cities[2].latitude),
                expiresAt = defaultExpiration(),
                status = status,
                thumbnail = byteArrayOf(),
                phoneNumber = "123456780"
            ),
            Auction(
                name = "Wireless Jbl headphones",
                description = "Worst jbl headphones you can have",
                price = 159.43,
                auctioneerId = AUCTIONEER_ID_UNDER_TEST,
                category = electronics,
                categoryPath = wirelessHeadphonesCategoryPath,
                productCondition = Condition.NOT_APPLICABLE,
                cityId = cities[3].id,
                cityName = cities[3].name,
                province = cities[3].province,
                location = GeoJsonPoint(cities[3].longitude, cities[3].latitude),
                expiresAt = defaultExpiration(),
                status = status,
                thumbnail = byteArrayOf(),
                phoneNumber = "123456780"
            )
        )

        return Pair(auctionRepository.saveAll(auctions), cities)
    }

    private fun thereAreCities(): List<City> {
        return cityRepository.saveAll(
            mutableListOf(
                City(
                    name = "Lublin testowy",
                    type = "village",
                    province = "Wojewodztwo pierwsze",
                    district = "Powiat pierwszy",
                    commune = "Gmina pierwsza",
                    latitude = 51.25,
                    longitude = 22.5666
                ),
                City(
                    name = "Swidnik testowy",
                    type = "village",
                    province = "Wojewodztwo drugie",
                    district = "Powiat drugi",
                    commune = "Gmina druga",
                    latitude = 51.2197,
                    longitude = 22.7
                ),
                City(
                    name = "Dorohucza testowy",
                    type = "village",
                    province = "Wojewodztwo trzecie",
                    district = "Powiat trzeci",
                    commune = "Gmina trzecia",
                    latitude = 51.1625,
                    longitude = 23.0088
                ),
                City(
                    name = "Chelm testowy",
                    type = "village",
                    province = "Wojewodztwo czwarte",
                    district = "Powiat czwarty",
                    commune = "Gmina czwarta",
                    latitude = 51.1322,
                    longitude = 23.4777
                )
            )
        )
    }

    private fun defaultExpiration(): Instant = Instant.now().plus(Duration.ofDays(10))

    private fun thereIsSampleCategoryTree(): CategoryResponse {
        val topLevelCategory: CategoryResponse = categoryFacade.create(
            request = CategoryCreateRequest(
                name = "Top level category",
                description = "Just top level category",
                parentCategoryId = null,
                isTopLevel = true,
                isFinalNode = false
            )
        )

        val secondLevelCategory: CategoryResponse = categoryFacade.create(
            request = CategoryCreateRequest(
                name = "Second level category",
                description = "Just second level category",
                parentCategoryId = topLevelCategory.id,
                isTopLevel = false,
                isFinalNode = false
            )
        )

        return categoryFacade.create(
            request = CategoryCreateRequest(
                name = "Final level category",
                description = "Nice final level category",
                parentCategoryId = secondLevelCategory.id,
                isTopLevel = false,
                isFinalNode = true
            )
        )

    }

}
