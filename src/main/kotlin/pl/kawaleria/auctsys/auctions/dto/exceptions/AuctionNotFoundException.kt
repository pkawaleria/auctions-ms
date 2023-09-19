package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class AuctionNotFoundException :
        ApiException(HttpStatus.NOT_FOUND.value(), "Accessed auction does not exist")