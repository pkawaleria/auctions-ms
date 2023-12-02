package pl.kawaleria.auctsys.images.dto.exceptions

import org.springframework.http.HttpStatus
import pl.kawaleria.auctsys.auctions.dto.exceptions.ApiException
import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

class ImageDoesNotExistsException : ApiException(ServiceErrorResponseCode.IMG01, HttpStatus.NOT_FOUND)