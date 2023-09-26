package pl.kawaleria.auctsys.categories.dto.exceptions

import org.springframework.http.HttpStatus

class InvalidCategoryActionException(message: String) :
        ApiException(code = HttpStatus.BAD_REQUEST.value(), message = message)