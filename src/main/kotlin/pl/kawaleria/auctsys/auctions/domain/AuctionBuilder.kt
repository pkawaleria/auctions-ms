package pl.kawaleria.auctsys.auctions.domain

import org.bson.types.ObjectId
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import pl.kawaleria.auctsys.auctions.dto.exceptions.CityNotFoundException
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.categories.domain.CategoryRepository
import pl.kawaleria.auctsys.categories.dto.exceptions.CategoryNotFound
import pl.kawaleria.auctsys.categories.dto.responses.CategoryNameResponse
import pl.kawaleria.auctsys.categories.dto.responses.CategoryPathResponse
import pl.kawaleria.auctsys.categories.dto.responses.SimpleCategoryResponse
import java.time.Duration
import java.time.Instant

class AuctionBuilder(
    private val auctionRepository: AuctionRepository,
    private val categoryRepository: CategoryRepository,
    private val cityRepository: CityRepository
) {

    private var currentCreateAuctionRequest: CreateAuctionRequestForDev? = null
    private val userId: String = ObjectId().toString()

    fun name(name: String): AuctionBuilder {
        currentCreateAuctionRequest = CreateAuctionRequestForDev(
            name = name,
            description = "",
            price = 0.0,
            categoryId = "",
            productCondition = Condition.NEW,
            cityId = ""
        )

        return this
    }

    fun description(description: String): AuctionBuilder {
        currentCreateAuctionRequest?.description = description
        return this
    }

    fun price(price: Double): AuctionBuilder {
        currentCreateAuctionRequest?.price = price
        return this
    }

    fun categoryId(): AuctionBuilder {
        currentCreateAuctionRequest?.categoryId = categoryRepository.getFinalCategories().first().id
        return this
    }

    fun productCondition(productCondition: Condition): AuctionBuilder {
        currentCreateAuctionRequest?.productCondition = productCondition
        return this
    }

    fun cityId(): AuctionBuilder {
        val pageRequest: PageRequest = PageRequest.of(0, 10)
        currentCreateAuctionRequest?.cityId = cityRepository.findAll(pageRequest).first().id
        return this
    }

    fun save(): Auction {
        val standardCreateAuctionRequest: CreateAuctionRequest? = currentCreateAuctionRequest?.toCreateAuctionRequest()
        val categoryPath: CategoryPath =
            getFullCategoryPath(standardCreateAuctionRequest!!.categoryId).toAuctionCategoryPathModel()

        val city: City =
            cityRepository.findById(standardCreateAuctionRequest.cityId).orElseThrow { CityNotFoundException() }

        val auction = Auction(
            name = standardCreateAuctionRequest.name,
            description = standardCreateAuctionRequest.description,
            price = standardCreateAuctionRequest.price,
            auctioneerId = userId,
            thumbnail = byteArrayOf(),
            expiresAt = Instant.now().plusSeconds(Duration.ofDays(100).toSeconds()),
            cityId = standardCreateAuctionRequest.cityId,
            category = categoryPath.lastCategory(),
            categoryPath = categoryPath,
            productCondition = standardCreateAuctionRequest.productCondition,
            cityName = city.name,
            province = city.province,
            location = GeoJsonPoint(city.longitude, city.latitude),
            status = AuctionStatus.ACCEPTED
        )

        return auctionRepository.save(auction)
    }

    private fun getFullCategoryPath(categoryId: String): CategoryPathResponse {
        val category: pl.kawaleria.auctsys.categories.domain.Category =
            categoryRepository.findById(categoryId).orElseThrow { CategoryNotFound() }
        val categoryPath: MutableList<CategoryNameResponse> = mutableListOf(category.toCategoryNameResponse())

        var parentCategoryId: String? = category.parentCategoryId
        while (parentCategoryId != null) {
            val parentCategory: pl.kawaleria.auctsys.categories.domain.Category =
                categoryRepository.findById(parentCategoryId).orElseThrow { CategoryNotFound() }
            parentCategoryId = parentCategory.parentCategoryId
            categoryPath.add(parentCategory.toCategoryNameResponse())
        }

        return CategoryPathResponse(
            requestedCategory = category.toSimpleCategoryResponse(),
            path = categoryPath.reversed()
        )
    }

    private fun CategoryPathResponse.toAuctionCategoryPathModel(): CategoryPath {
        return CategoryPath(this.path.map { it.toAuctionCategoryModel() }.toMutableList())
    }

    private fun CategoryNameResponse.toAuctionCategoryModel(): Category {
        return Category(this.id, this.name)
    }

    private fun pl.kawaleria.auctsys.categories.domain.Category.toCategoryNameResponse(): CategoryNameResponse =
        CategoryNameResponse(
            id = this.id,
            name = this.name
        )

    private fun pl.kawaleria.auctsys.categories.domain.Category.toSimpleCategoryResponse(): SimpleCategoryResponse =
        SimpleCategoryResponse(
            id = this.id,
            name = this.name,
            isTopLevel = this.isTopLevel,
            isFinalNode = this.isFinalNode,
            description = this.description,
        )

}

data class CreateAuctionRequestForDev(
    var name: String,
    var description: String,
    var price: Double,
    var categoryId: String,
    var productCondition: Condition,
    var cityId: String,
)

fun CreateAuctionRequestForDev.toCreateAuctionRequest(): CreateAuctionRequest =
    CreateAuctionRequest(name, description, price, categoryId, productCondition, cityId)