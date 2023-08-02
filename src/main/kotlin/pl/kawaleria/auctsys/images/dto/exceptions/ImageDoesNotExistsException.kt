package pl.kawaleria.auctsys.images.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

class ImageDoesNotExistsException : ApiException(HttpStatus.NOT_FOUND.value(), "Image does not exists")