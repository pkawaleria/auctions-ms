package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class InvalidAuctionCreationRequestException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Invalid CreateAuctionRequest")