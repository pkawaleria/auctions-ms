package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class InvalidAuctionUpdateRequestException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Invalid UpdateAuctionRequest")