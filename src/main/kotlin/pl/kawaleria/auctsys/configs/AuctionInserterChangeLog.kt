package pl.kawaleria.auctsys.configs

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import pl.kawaleria.auctsys.auctions.domain.*
import pl.kawaleria.auctsys.categories.domain.CategoryRepository

@Profile("dev")
@ChangeLog(order = "004")
class AuctionInserterChangeLog {

    @ChangeSet(order = "001", id = "insertAuctions", author = "filip-kaminski")
    fun insertAuctions(
        auctionRepository: AuctionRepository,
        categoryRepository: CategoryRepository,
        cityRepository: CityRepository
    ) {

        for (index: Int in 1..150) {
            AuctionBuilder(
                auctionRepository = auctionRepository,
                categoryRepository = categoryRepository,
                cityRepository = cityRepository
            )
                .name("Aukcja numer $index")
                .description("Opis aukcji number $index")
                .price(150.5 * index)
                .categoryId()
                .productCondition(Condition.NEW)
                .cityId()
                .save()
        }

        AuctionBuilder(
            auctionRepository = auctionRepository,
            categoryRepository = categoryRepository,
            cityRepository = cityRepository
        )
            .name("Aukcja inna niż pozostałe")
            .description("Opis aukcji innej niż pozostałe")
            .price(1257.87)
            .categoryWithMoreThenTwoSubcategoriesId()
            .productCondition(Condition.NEW)
            .cityId()
            .save()
    }

    @ChangeSet(order = "002", id = "updatePhoneNumberInAuctions", author = "lukasz-karasek")
    fun updatePhoneNumberInAuctions(mongockTemplate: MongockTemplate) {
        val query = Query()
        val update: Update = Update().set("phoneNumber", "901234874")
        mongockTemplate.updateMulti(query, update, Auction::class.java)
    }
}
