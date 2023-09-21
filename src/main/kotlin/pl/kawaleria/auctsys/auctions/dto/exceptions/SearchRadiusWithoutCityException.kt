package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class SearchRadiusWithoutCityException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Cannot found auctions with radius only")