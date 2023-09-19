package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class SearchRadiusOutOfBoundsException :
        ApiException(HttpStatus.BAD_REQUEST.value(), "Search radius is out of bounds")