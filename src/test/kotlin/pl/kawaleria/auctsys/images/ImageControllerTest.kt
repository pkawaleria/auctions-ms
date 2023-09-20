package pl.kawaleria.auctsys.images

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
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.*
import pl.kawaleria.auctsys.auctions.domain.*
import pl.kawaleria.auctsys.images.domain.Image
import pl.kawaleria.auctsys.images.domain.ImageRepository
import pl.kawaleria.auctsys.images.dto.responses.AuctionImagesResponse
import pl.kawaleria.auctsys.images.dto.responses.ImageDetailedResponse
import java.time.Duration
import java.time.Instant
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageControllerTest {

    private val mongo: MongoDBContainer = MongoDBContainer("mongo").apply {
        start()
    }

    init {
        System.setProperty("spring.data.mongodb.uri", mongo.replicaSetUrl)
    }

    companion object {
        val logger: Logger = getLogger(ImageControllerTest::class.java)
    }

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var imageRepository: ImageRepository

    @Autowired
    private lateinit var auctionRepository: AuctionRepository

    @Autowired
    private lateinit var cityRepository: CityRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var mockMvc: MockMvc

    @AfterEach
    fun cleanUp() {
        mongoTemplate.dropCollection("images")
        mongoTemplate.dropCollection("auctions")
    }

    @Nested
    inner class ImageUploadTests {

        @Test
        fun `should upload images for auction`() {
            // given
            val auctionId: String = thereIsAuction()
            val imageFiles: List<String> = listOf("audi.jpg", "skoda.png", "nissan.jpg")
            val imagePart: List<MockMultipartFile> = imageFiles.map { createMockMultipartFile(it) }

            // when
            val result: MvcResult = mockMvc.perform(
                multipart("/auction-service/auctions/$auctionId/images")
                    .file(imagePart[0])
                    .file(imagePart[1])
                    .file(imagePart[2])
                    .withAuthenticatedAuctioneer()
            )
                .andExpect(status().isOk())
                .andReturn()

            // then
            val response: String = result.response.contentAsString
            logger.info("response = $response")
            val mappedImages: List<ImageDetailedResponse> =
                objectMapper.readValue(
                    response,
                    objectMapper.typeFactory.constructCollectionType(
                        List::class.java,
                        ImageDetailedResponse::class.java
                    )
                )

            Assertions.assertThat(mappedImages).hasSize(3)
        }

        @Test
        fun `should return bad request for uploading file other than jpg or png`() {
            // given
            val auctionId: String = thereIsAuction()

            val txtContent = "This is sample txt file"
            val txtFile = MockMultipartFile(
                "files",
                "mock.txt",
                MediaType.TEXT_PLAIN_VALUE,
                txtContent.toByteArray()
            )

            // when
            val result: MvcResult = mockMvc.perform(
                multipart("/auction-service/auctions/$auctionId/images")
                    .file(txtFile)
                    .withAuthenticatedAuctioneer()
            )
                // then
                .andExpect(status().isBadRequest())
                .andReturn()

            Assertions.assertThat(result.response.errorMessage).contains("Invalid file type")
        }

        @Test
        fun `should return bad request when magic bytes of uploaded files does not correspond to png or jpeg magic bytes`() {
            // given
            val auctionId: String = thereIsAuction()

            val txtContent = "This is sample txt file that pretends to be JPEG file"
            val txtFile = MockMultipartFile(
                "files",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                txtContent.toByteArray()
            )

            // when
            val result: MvcResult = mockMvc.perform(
                multipart("/auction-service/auctions/$auctionId/images")
                    .file(txtFile)
                    .withAuthenticatedAuctioneer()
            )
                // then
                .andExpect(status().isBadRequest())
                .andReturn()

            Assertions.assertThat(result.response.errorMessage).contains("Invalid file type")
        }

        @Test
        fun `should return bad request when content type is not of type IMAGE_PNG nor IMAGE_JPEG`() {
            // given
            val auctionId: String = thereIsAuction()

            val file: MockMultipartFile = createMockMultipartFileWithInvalidContentType("audi.jpg")

            // when
            val result: MvcResult = mockMvc.perform(
                multipart("/auction-service/auctions/$auctionId/images")
                    .file(file)
                    .withAuthenticatedAuctioneer()
            )
                // then
                .andExpect(status().isBadRequest())
                .andReturn()

            Assertions.assertThat(result.response.errorMessage).contains("Invalid content type")
        }
    }

    @Nested
    inner class ImageRetrievalTest {
        @Test
        fun `should get information about auction images`() {
            // given
            val auctionId: String = thereIsAuction()
            val images: List<Image> = thereAreImagesOfAuction(auctionId)

            // when
            val result: MvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/auction-service/auctions/$auctionId/images")
                    .contentType(MediaType.APPLICATION_JSON)
                    .withAuthenticatedAuctioneer()
            )
                .andExpect(status().isOk())
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val imagesResponse: AuctionImagesResponse =
                objectMapper.readValue(responseJson, AuctionImagesResponse::class.java)

            Assertions.assertThat(imagesResponse.imagesCount).isEqualTo(images.size)
            Assertions.assertThat(imagesResponse.imageIDs).containsExactlyInAnyOrderElementsOf(images.map { it.id })
        }

        @Test
        fun `should return empty information about auction images for auction with no images`() {
            // given
            val auctionId: String = thereIsAuction()

            // when
            val result: MvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/auction-service/auctions/$auctionId/images")
                    .contentType(MediaType.APPLICATION_JSON)
                    .withAuthenticatedAuctioneer()
            )
                .andExpect(status().isOk())
                .andReturn()

            // then
            val responseJson: String = result.response.contentAsString
            val imagesResponse: AuctionImagesResponse =
                objectMapper.readValue(responseJson, AuctionImagesResponse::class.java)

            Assertions.assertThat(imagesResponse.imagesCount).isZero()
            Assertions.assertThat(imagesResponse.imageIDs).isEmpty()
        }

        @Test
        fun `should get single image`() {
            // given
            val auctionId: String = thereIsAuction()
            val images: List<Image> = thereAreImagesOfAuction(auctionId)

            val selectedImageId: String? = images[0].id
            val selectedImageBinary: ByteArray = images[0].binaryData

            // when
            val result: MvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/auction-service/auctions/$auctionId/images/$selectedImageId")
                    .contentType(MediaType.IMAGE_JPEG_VALUE)
                    .withAuthenticatedAuctioneer()
            )
                .andExpect(status().isOk())
                .andReturn()

            // then
            val responseByteArray: ByteArray = result.response.contentAsByteArray

            Assertions.assertThat(responseByteArray).isEqualTo(selectedImageBinary)
        }

        @Test
        fun `should return not found trying to get non-existing image`() {
            // given
            val auctionId: String = thereIsAuction()
            val nonExistingImageId = "fake-image-id"

            // when
            val result: MvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/auction-service/auctions/$auctionId/images/$nonExistingImageId")
                    .contentType(MediaType.IMAGE_JPEG_VALUE)
                    .withAuthenticatedAuctioneer()
            )
                .andExpect(status().isNotFound)
                .andReturn()
            // then
            Assertions.assertThat(result.response.errorMessage).contains("Image does not exists")
        }

    }

    @Nested
    inner class ImageDeletionTests {

        @Test
        fun `should delete image`() {
            // given
            val auctionId: String = thereIsAuction()
            val images: List<Image> = thereAreImagesOfAuction(auctionId)

            val selectedImageId: String? = images[0].id

            // when
            mockMvc.perform(
                MockMvcRequestBuilders.delete("/auction-service/auctions/$auctionId/images/$selectedImageId")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNoContent)

            // then
            val existsAfterDelete: Boolean? = selectedImageId?.let { imageRepository.existsById(it) }
            Assertions.assertThat(existsAfterDelete).isFalse
        }

        @Test
        fun `should return not found trying to delete non-existing image`() {
            // given
            val auctionId: String = thereIsAuction()
            val nonExistingImageId = "fake-image-id"

            // when
            val result: MvcResult = mockMvc.perform(
                MockMvcRequestBuilders.delete("/auction-service/auctions/$auctionId/images/$nonExistingImageId")
                    .withAuthenticatedAuctioneer()
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andReturn()
            // then
            Assertions.assertThat(result.response.errorMessage).contains("Image does not exists")
        }
    }

    private fun thereIsAuction(): String {
        val electronics = Category(UUID.randomUUID().toString(), "Electronics")
        val headphones = Category(UUID.randomUUID().toString(), "Headphones")
        val wirelessHeadphones = Category(UUID.randomUUID().toString(), "Wireless Headphones")
        val categoryPath = CategoryPath(
            pathElements = mutableListOf(electronics, headphones, wirelessHeadphones)
        )

        val cityId: String = thereIsCity()

        val auction = Auction(
            name = "Wireless Samsung headphones",
            category = wirelessHeadphones,
            categoryPath = categoryPath,
            description = "Best headphones you can have",
            price = 1.23,
            auctioneerId = "user-id",
            expiresAt = Instant.now().plusSeconds(Duration.ofDays(1).toSeconds()),
            cityId = cityId,
            productCondition = Condition.`NOT_APPLICABLE`
        )

        return auctionRepository.save(auction).id!!
    }

    private fun thereIsCity(): String {
        val city = City(
            name = "Miasto1",
            type = "village",
            province = "Województwo",
            district = "Powiat",
            commune = "Gmina",
            latitude = 1.23,
            longitude = 4.56
        )

        return cityRepository.save(city).id.toString()
    }

    private fun thereAreImagesOfAuction(auctionId: String): List<Image> {
        val imageFiles: List<String> = listOf("audi.jpg", "skoda.png", "nissan.jpg")

        val images: List<Image> = imageFiles.map { fileName ->
            val resource = ClassPathResource("test_images/$fileName")
            val contentType: String = MediaType.IMAGE_JPEG_VALUE
            val content: ByteArray = resource.inputStream.readAllBytes()

            val image = Image(
                type = contentType,
                size = content.size.toLong(),
                binaryData = content,
                auctionId = auctionId
            )
            imageRepository.save(image)
        }
        return images
    }

    private fun createMockMultipartFile(fileName: String): MockMultipartFile {
        val resource = ClassPathResource("test_images/$fileName")
        val originalFileName: String = fileName
        val contentType: String = MediaType.IMAGE_PNG_VALUE
        val content: ByteArray = resource.inputStream.readAllBytes()
        return MockMultipartFile("files", originalFileName, contentType, content)
    }

    private fun createMockMultipartFileWithInvalidContentType(fileName: String): MockMultipartFile {
        val resource = ClassPathResource("test_images/$fileName")
        val originalFileName: String = fileName
        val contentType: String = MediaType.APPLICATION_OCTET_STREAM_VALUE
        val content: ByteArray = resource.inputStream.readAllBytes()
        return MockMultipartFile("files", originalFileName, contentType, content)
    }

}