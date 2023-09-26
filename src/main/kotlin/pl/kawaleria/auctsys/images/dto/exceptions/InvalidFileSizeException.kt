package pl.kawaleria.auctsys.images.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.exceptions.ApiException

class InvalidFileSizeException :
    ApiException(HttpStatus.BAD_REQUEST.value(), "File size is too big")