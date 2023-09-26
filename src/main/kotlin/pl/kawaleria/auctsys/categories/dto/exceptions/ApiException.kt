package pl.kawaleria.auctsys.categories.dto.exceptions

import org.springframework.http.HttpStatusCode
import org.springframework.web.server.ResponseStatusException

open class ApiException(code: Int, message: String): ResponseStatusException(HttpStatusCode.valueOf(code), message)