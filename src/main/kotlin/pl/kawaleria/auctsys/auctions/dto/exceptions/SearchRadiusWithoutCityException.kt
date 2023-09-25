package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus

class SearchRadiusWithoutCityException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "City for search request is not specified or cannot be found")