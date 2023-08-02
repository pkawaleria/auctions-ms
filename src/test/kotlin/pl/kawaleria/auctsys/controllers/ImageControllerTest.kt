package pl.kawaleria.auctsys.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.auctions.domain.Auction
import pl.kawaleria.auctsys.auctions.domain.AuctionRepository
import pl.kawaleria.auctsys.auctions.domain.Category
import pl.kawaleria.auctsys.images.domain.ImageRepository
import java.io.BufferedReader
import java.io.FileReader
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
        fun `image should upload successfully` () {

            // given
            val auctionId: String = thereIsAuction()
            val multipartFiles: MutableList<MultipartFile> = thereAreMultipartFiles()

            // when
            val result = mockMvc.perform(
                post("/auction-service/auctions/$auctionId/images")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .content(objectMapper.writeValueAsString(multipartFiles)))
                .andExpect(status().isOk())
                .andReturn()

//            // then
//            val response = result.response.contentAsString
//            logger.info("response = $response")
        }
    }

    @Nested
    inner class ImageDownloadTests {

    }

    @Nested
    inner class ImageDeletionTests {

    }

    private fun thereIsAuction(): String {
        val auction = Auction(
            name = "Wireless Samsung headphones",
            category = Category.MODA,
            description = "Best headphones you can have",
            price = 1.23,
            auctioneerId = "user-id"
        )

        return auctionRepository.save(auction).id!!
    }

    private fun thereAreMultipartFiles(): MutableList<MultipartFile> {
        val multipartFiles: MutableList<MultipartFile> = mutableListOf()

        val data = ClassPathResource("BinaryImagesData.txt")
        val reader = BufferedReader(FileReader(data.file))

        val imagesName: Array<String> = arrayOf("audi.jpg", "nissan.jpg", "skoda.jpg", "audi.png", "nissan.png", "skoda.png")
        val imagesContentTypes: Array<String> = arrayOf("index/jpeg", "index/jpeg", "index/jpeg", "index/png", "index/png", "index/png")
        var imageBinaryData: ByteArray

        for (i in 0 .. 5) {
            imageBinaryData = reader.readLine().toByteArray()
            multipartFiles.add(MockMultipartFile(imagesName[i], imagesName[i], imagesContentTypes[i], imageBinaryData))
        }

        return multipartFiles
    }




}