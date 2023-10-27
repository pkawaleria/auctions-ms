package pl.kawaleria.auctsys.configs

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import org.springframework.context.annotation.Profile
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

        AuctionBuilder(
            auctionRepository = auctionRepository,
            categoryRepository = categoryRepository,
            cityRepository = cityRepository
        )
            .name("Przykladowa aukcja")
            .description("Przykladowy opis aukcji")
            .price(123.5)
            .categoryId()
            .productCondition(Condition.NEW)
            .cityId()
            .save()
    }
}
