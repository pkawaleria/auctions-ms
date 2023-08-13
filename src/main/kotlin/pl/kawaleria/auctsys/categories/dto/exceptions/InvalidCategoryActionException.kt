package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class InvalidCategoryActionException(message: String) :
        ApiException(code = HttpStatus.BAD_REQUEST.value(), message = message)