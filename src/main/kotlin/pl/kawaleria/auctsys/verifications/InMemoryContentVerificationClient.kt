package pl.kawaleria.auctsys.verifications

import org.springframework.core.io.Resource

// For test purposes, always states that contents are secure
class InMemoryContentVerificationClient : ContentVerificationClient {
    override fun verifyImage(image: Resource): VerificationResult {
        return VerificationResult(false)
    }

    override fun verifyText(text: TextRequest): VerificationResult {
        return VerificationResult(false)
    }
}