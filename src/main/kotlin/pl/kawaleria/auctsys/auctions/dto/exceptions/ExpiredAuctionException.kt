package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class ExpiredAuctionException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Cannot perform operation on expired auction")