package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class InappropriateContentException :
    ApiException(HttpStatus.BAD_REQUEST.value(), "Auction name or description is inappropriate")