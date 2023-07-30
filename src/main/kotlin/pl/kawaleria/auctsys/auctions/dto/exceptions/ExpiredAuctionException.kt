package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class ExpiredAuctionException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Cannot perform operation on expired auction")