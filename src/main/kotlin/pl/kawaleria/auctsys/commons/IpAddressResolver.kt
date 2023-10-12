package pl.kawaleria.auctsys.commons

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class IpAddressResolver {

    fun getIpAddress(req: HttpServletRequest): String {
        return req.getHeader("X-Real-IP")?.takeIf { it.isNotBlank() } ?: req.remoteAddr.takeIf { it.isNotBlank() } ?: "DEFAULT"
    }
}