package pl.kawaleria.auctsys.auctions.domain

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

private const val AUCTION_BATCH_SIZE = 100

class AuctionCategoryDeleter(private val auctionRepository: AuctionRepository) {

    private val log = LoggerFactory.getLogger(AuctionCategoryDeleter::class.java)

    fun eraseCategoryFromAuctions(categoryName: String) {
        log.info("Starting to erase category with ID: $categoryName from auctions")
        var page = 0
        do {
            val pageable: PageRequest = PageRequest.of(page++, AUCTION_BATCH_SIZE)
            val auctions: Page<Auction> = auctionRepository.findAuctionsWithCategoryInPath(categoryName, pageable)
            log.info("Processing auctions with category in path ${auctions.content}")

            auctions.content
                    .forEach{ it.dropCategoryFromPath(categoryName) }
            auctions.content
                    .forEach{ auctionRepository.save(it) }

            log.debug("Processed page $page of auctions")
        } while (auctions.hasNext())

        log.info("Finished erasing category with ID: $categoryName from auctions")
    }
}
