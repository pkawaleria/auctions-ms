package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class InvalidAuctionCategoryPathException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Cannot set empty category path to auction")