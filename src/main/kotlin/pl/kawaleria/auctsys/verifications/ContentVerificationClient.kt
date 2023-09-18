package pl.kawaleria.auctsys.verifications

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.core.io.Resource

import org.springframework.http.MediaType.*
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange
@EnableAsync
interface ContentVerificationClient {

    @PostExchange("/image/verify_image", contentType = MULTIPART_FORM_DATA_VALUE, accept = [APPLICATION_JSON_VALUE])
    fun verifyImage(@RequestPart("image") image: Resource): VerificationResult

    @PostExchange("/desc/verify_text", contentType = APPLICATION_JSON_VALUE, accept = [APPLICATION_JSON_VALUE])
    fun verifyText(@RequestBody text: TextRequest): VerificationResult
}

data class TextRequest(
        val text: String
)

data class VerificationResult(
        @JsonProperty("has_inappropriate_content") val isInappropriate: Boolean)

