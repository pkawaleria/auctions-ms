package pl.kawaleria.auctsys.verifications

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class ContentVerificationClientConfiguration(@Value("\${verification-ms.url}") val verificationMsUrl: String) {

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
                .baseUrl(verificationMsUrl)
                .build()
    }

    @Bean
    fun postClient(): ContentVerificationClient {
        val httpServiceProxyFactory: HttpServiceProxyFactory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient()))
                .build()

        return httpServiceProxyFactory.createClient(ContentVerificationClient::class.java)
    }
}