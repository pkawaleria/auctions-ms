package pl.kawaleria.auctsys.auctions.dto.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode

open class ApiException(val httpStatusCode: HttpStatusCode = HttpStatus.BAD_REQUEST, val errorCodes: List<ServiceErrorResponseCode>) :
    RuntimeException(joinErrorCodesMessages(errorCodes)) {
    constructor(errorCodes: List<ServiceErrorResponseCode>, httpStatusCode: HttpStatusCode = HttpStatus.BAD_REQUEST)
            : this(httpStatusCode, errorCodes)
    constructor(errorCode: ServiceErrorResponseCode, httpStatusCode: HttpStatusCode = HttpStatus.BAD_REQUEST) :
            this(httpStatusCode, listOf(errorCode))
}

fun joinErrorCodesMessages(errorCodes: List<ServiceErrorResponseCode>) =
    errorCodes.joinToString("; ") { it.message }
