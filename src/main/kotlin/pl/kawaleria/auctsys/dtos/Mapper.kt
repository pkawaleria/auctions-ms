package pl.kawaleria.auctsys.dtos

import pl.kawaleria.auctsys.models.Auction

fun Auction.toDto(): AuctionDto = AuctionDto(id, name, category, description, price, auctioneerId)