package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class InvalidAuctionCreationRequestException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Invalid CreateAuctionRequest")