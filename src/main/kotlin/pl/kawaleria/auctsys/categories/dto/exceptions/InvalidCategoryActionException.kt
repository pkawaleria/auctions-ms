package pl.kawaleria.auctsys.categories.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.exceptions.ApiException
import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class InvalidCategoryActionException(code: ServiceErrorResponseCode) : ApiException(code, HttpStatus.BAD_REQUEST)