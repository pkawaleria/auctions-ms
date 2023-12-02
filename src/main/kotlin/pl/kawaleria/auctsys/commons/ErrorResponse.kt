package pl.kawaleria.auctsys.commons

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.kawaleria.auctsys.auctions.dto.exceptions.ApiException
import pl.kawaleria.auctsys.auctions.dto.exceptions.joinErrorCodesMessages
import pl.kawaleria.auctsys.images.dto.exceptions.ImageViolation
import pl.kawaleria.auctsys.images.dto.exceptions.ImagesValidationException

data class ErrorResponse(
    val timestamp: Long,
    val path: String,
    val errorCodes: List<String>,
    val message: String,
    val httpCode: HttpStatusCode,
)

data class ImagesValidationErrorResponse(
    val timestamp: Long,
    val path: String,
    val errorCode: ServiceErrorResponseCode,
    val violations: List<ImageViolation>,
    val httpCode: HttpStatusCode,
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        exception: ApiException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorCodes = exception.errorCodes.map { it.name }
        val joinedMessages = joinErrorCodesMessages(exception.errorCodes)

        val errorResponse = ErrorResponse(
            timestamp = System.currentTimeMillis(),
            path = request.requestURI,
            errorCodes = errorCodes,
            message = joinedMessages,
            httpCode = exception.httpStatusCode
        )
        return ResponseEntity(errorResponse, exception.httpStatusCode)
    }

    @ExceptionHandler(ImagesValidationException::class)
    fun handleImagesViolationException(
        exception: ImagesValidationException,
        request: HttpServletRequest
    ): ResponseEntity<ImagesValidationErrorResponse> {

        val errorResponse = ImagesValidationErrorResponse(
            timestamp = System.currentTimeMillis(),
            path = request.requestURI,
            errorCode = ServiceErrorResponseCode.IMG00_GENERAL,
            violations = exception.imagesViolations,
            httpCode = HttpStatus.BAD_REQUEST
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

}
