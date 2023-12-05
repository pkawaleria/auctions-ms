package pl.kawaleria.auctsys.configs

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import io.changock.migration.api.annotations.NonLockGuarded
import net.coobird.thumbnailator.Thumbnails
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationResults
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.query.Criteria
import pl.kawaleria.auctsys.auctions.domain.*
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.images.domain.Image
import pl.kawaleria.auctsys.images.domain.ImageRepository
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.Instant
import javax.imageio.ImageIO
import kotlin.random.Random

private const val TOTAL_IMAGES_COUNT = 72
private const val IMAGES_DIRECTORY = "/test-pics/"

@Profile("dev")
@ChangeLog(order = "004")
class AuctionInserterChangeLog {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ChangeSet(order = "001", id = "insertCompleteAuctionsWithImages", author = "lukasz-karasek")
    fun insertAuctions(
        @NonLockGuarded categoryFacade: CategoryFacade,
        auctionRepository: AuctionRepository,
        imageRepository: ImageRepository,
        mongockTemplate: MongockTemplate,
    ) {

        for (index: Int in 1..200) {

            logger.info("Adding auction number $index")
            val randomCategory = getRandomFinalCategory(mongockTemplate)
            val categoryPath: CategoryPath = categoryFacade.getFullCategoryPath(randomCategory.id)
                .toAuctionCategoryPathModel()

            val city: City = getRandomCity(mongockTemplate)

            val auctionId = ObjectId().toString()
            val imgIndex1 = index % (TOTAL_IMAGES_COUNT + 1)
            val imgIndex2 = (index + 1) % (TOTAL_IMAGES_COUNT + 1)
            val imgIndex3 = (index + 2 ) % (TOTAL_IMAGES_COUNT + 1)

            val imagePath1 = "$IMAGES_DIRECTORY$imgIndex1.jpg"
            val imagePath2 = "$IMAGES_DIRECTORY$imgIndex2.jpg"
            val imagePath3 = "$IMAGES_DIRECTORY$imgIndex3.jpg"

            val firstImg = saveImage(imagePath1, auctionId, imageRepository)
            saveImage(imagePath2, auctionId, imageRepository)
            saveImage(imagePath3, auctionId, imageRepository)

            val thumbnail = firstImg?.let { prepareThumbnail(it) }

            val auction = Auction(
                id = auctionId,
                name = "Aukcja nr ${index}",
                description = "Opis aukcji nr ${index}",
                price = Random.nextDouble(from = 200.0, until = 200000.0),
                auctioneerId = Random.nextInt(from = 1, until = 10).toString(),
                thumbnail = thumbnail ?: byteArrayOf(),
                expiresAt = Instant.now().plus(Duration.ofDays(365)),
                cityId = city.id,
                category = categoryPath.lastCategory(),
                categoryPath = categoryPath,
                productCondition = Condition.values().random(),
                cityName = city.name,
                province = city.province,
                location = GeoJsonPoint(city.longitude, city.latitude),
                phoneNumber = "123456789",
                status = AuctionStatus.ACCEPTED
            )
            auctionRepository.save(auction)
        }

    }

    private fun saveImage(imagePath: String, auctionId: String, imageRepository: ImageRepository): ByteArray? {
        val imageResource = ClassPathResource(imagePath)
        val imageBytes = imageResource.inputStream.readAllBytes()

        val image = Image(
            type = "image/jpeg",
            size = imageBytes.size.toLong(),
            binaryData = imageBytes,
            auctionId = auctionId
        )
        imageRepository.save(image)
        return imageBytes
    }


    private fun prepareThumbnail(image: ByteArray): ByteArray {
        val originalImage: BufferedImage = ImageIO.read(image.inputStream())

        val thumbnailWidth = 300
        val thumbnailHeight = 300

        val outputStream = ByteArrayOutputStream()

        Thumbnails.of(originalImage)
            .size(thumbnailWidth, thumbnailHeight)
            .outputFormat("png")
            .toOutputStream(outputStream)

        return outputStream.toByteArray()
    }

    fun getRandomCity(mongoTemplate: MongockTemplate): City {
        val sampleStage = Aggregation.sample(5)
        val aggregation = Aggregation.newAggregation(sampleStage)
        val output: AggregationResults<City> = mongoTemplate.aggregate(aggregation, "cities", City::class.java)

        val cities: List<City> = output.mappedResults
        return cities.shuffled().first()
    }

    fun getRandomFinalCategory(mongoTemplate: MongockTemplate): Category {
        val matchStage = Aggregation.match(Criteria.where("isFinalNode").`is`(true))
        val sampleStage = Aggregation.sample(5)
        val aggregation = Aggregation.newAggregation(matchStage, sampleStage)
        val output: AggregationResults<Category> =
            mongoTemplate.aggregate(aggregation, "categories", Category::class.java)

        val categories: List<Category> = output.mappedResults
        return categories.shuffled().first()
    }

}
