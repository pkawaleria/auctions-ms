package pl.kawaleria.auctsys.auctions.dto.responses

import org.springframework.web.server.ResponseStatusException

class ApiException(code: Int, message: String): ResponseStatusException(code, message, null)