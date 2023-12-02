package pl.kawaleria.auctsys.images.dto.exceptions

import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode


data class ImageViolation(val imageName: String, val serviceErrorCodes: List<ServiceErrorResponseCode>)
class ImagesValidationException(val imagesViolations: List<ImageViolation>) : RuntimeException()