package pl.kawaleria.auctsys.images.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class InvalidFileContentTypeException : ApiException(HttpStatus.BAD_REQUEST.value(), "Invalid content type")