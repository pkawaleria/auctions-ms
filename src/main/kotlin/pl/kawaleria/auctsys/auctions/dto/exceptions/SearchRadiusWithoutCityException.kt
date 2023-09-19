package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class SearchRadiusWithoutCityException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Cannot found auctions with radius only")