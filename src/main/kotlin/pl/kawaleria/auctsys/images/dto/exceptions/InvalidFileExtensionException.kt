package pl.kawaleria.auctsys.images.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class InvalidFileExtensionException : ApiException(HttpStatus.BAD_REQUEST.value(), "Invalid file extension")