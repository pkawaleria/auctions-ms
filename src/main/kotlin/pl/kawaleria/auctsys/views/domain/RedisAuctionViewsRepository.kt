package pl.kawaleria.auctsys.views.domain

import org.springframework.data.keyvalue.repository.KeyValueRepository
import org.springframework.stereotype.Repository

@Repository
interface RedisAuctionViewsRepository : AuctionViewsRepository, KeyValueRepository<AuctionViews, String>
