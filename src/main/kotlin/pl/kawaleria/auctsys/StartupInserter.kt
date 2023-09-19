package pl.kawaleria.auctsys

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.stereotype.Component
import pl.kawaleria.auctsys.auctions.domain.*
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.categories.domain.Category
import pl.kawaleria.auctsys.categories.domain.CategoryRepository

// TODO: This class serves only for development (inserts predefined test data). It will be better to extract that into
//  files (dev-inserts.json or whatever) and load this files at startup
@Component
class StartupInserter(private val categoryRepository: CategoryRepository,
                      private val cityRepository: CityRepository,
                      private val auctionRepository: AuctionRepository,
                      private val auctionFacade: AuctionFacade) {

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReadyEvent() {
        insertTestData()
    }

    private fun insertTestData() {
        categoryRepository.deleteAll()
        cityRepository.deleteAll()
        auctionRepository.deleteAll()

        val electronics = Category(
                name = "Electronics",
                description = "This is nice category",
                isTopLevel = true,
                isFinalNode = false
        )

        val clothing = Category(
                name = "Clothing",
                description = "This is nice category",
                isTopLevel = true,
                isFinalNode = false
        )
        categoryRepository.saveAll(mutableListOf(electronics, clothing))

        val smartphones = Category(
                name = "Smartphones",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = false,
                parentCategoryId = electronics.id
        )
        val laptops = Category(
                name = "Laptops",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = false,
                parentCategoryId = electronics.id
        )

        val tShirts = Category(
                name = "T-Shirts",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = false,
                parentCategoryId = clothing.id
        )
        val jeans = Category(
                name = "Jeans",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = false,
                parentCategoryId = clothing.id
        )
        categoryRepository.saveAll(mutableListOf(smartphones, laptops, tShirts, jeans))

        val gamingLaptops = Category(
                name = "gaming laptops",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = true,
                parentCategoryId = laptops.id
        )
        val businessLaptops = Category(
                name = "business laptops",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = true,
                parentCategoryId = laptops.id
        )
        categoryRepository.saveAll(mutableListOf(gamingLaptops, businessLaptops))

        val cities: List<City> = listOf(
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
                    name = "Świdnik testowy",
                    type = "village",
                    province = "Wojewodztwo drugie",
                    district = "Powiat drugi",
                    commune = "Gmina druga",
                    latitude = 51.2197,
                    longitude = 22.7000
                ),
                City(
                    name = "Dorohucza testowa",
                    type = "village",
                    province = "Wojewodztwo trzecie",
                    district = "Powiat trzeci",
                    commune = "Gmina trzecia",
                    latitude = 51.1625,
                    longitude = 23.0088
                ),
                City(
                    name = "Chełm testowy",
                    type = "village",
                    province = "Wojewodztwo czwarte",
                    district = "Powiat czwarty",
                    commune = "Gmina czwarta",
                    latitude = 51.1322,
                    longitude = 23.4777
                )
        )
        cityRepository.saveAll(cities)


        val auctionRequests: List<CreateAuctionRequest> = listOf(
                CreateAuctionRequest(
                        name = "Modern Dell laptop",
                        description = "Modern dell laptop with radeon graphics",
                        price = 12.4,
                        categoryId = gamingLaptops.id,
                        productCondition = Condition.NEW,
                        cityId = cities[0].id!!,
                        cityName = cities[0].name,
                        location = GeoJsonPoint(cities[0].latitude, cities[0].longitude)
                ),
                CreateAuctionRequest(
                        name = "Modern HP laptop",
                        description = "Modern hp laptop with nvidia graphics",
                        price = 35.7,
                        categoryId = gamingLaptops.id,
                        productCondition = Condition.USED,
                        cityId = cities[1].id!!,
                        cityName = cities[1].name,
                        location = GeoJsonPoint(cities[1].latitude, cities[1].longitude)
                ),
                CreateAuctionRequest(
                        name = "Modern Lenovo laptop",
                        description = "Modern lenovo laptop with nvidia and radeon graphics",
                        price = 35.7,
                        categoryId = gamingLaptops.id,
                        productCondition = Condition.USED,
                        cityId = cities[2].id!!,
                        cityName = cities[2].name,
                        location = GeoJsonPoint(cities[2].latitude, cities[2].longitude)
                ),
                CreateAuctionRequest(
                        name = "Modern Asus laptop",
                        description = "Modern asus laptop with two radeon graphics",
                        price = 35.7,
                        categoryId = gamingLaptops.id,
                        productCondition = Condition.USED,
                        cityId = cities[3].id!!,
                        cityName = cities[3].name,
                        location = GeoJsonPoint(cities[3].latitude, cities[3].longitude)
                )
        )

        for (i: Int in auctionRequests.indices) {
            auctionFacade.addNewAuction(auctionRequests[i], "user-id-$i")
        }
    }
}