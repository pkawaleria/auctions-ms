package pl.kawaleria.auctsys.auctions.dto.events

data class AuctionTextVerificationReceivedEvent(
    val auctionId: String,
    val titleVerificationResult: SingleVerificationResult,
    val descriptionVerificationResult: SingleVerificationResult
) {
    fun isAccepted(): Boolean {
        return this.titleVerificationResult.verificationStatus == VerificationStatus.POSITIVE &&
                this.descriptionVerificationResult.verificationStatus == VerificationStatus.POSITIVE
    }

    fun isNegative() : Boolean {
        return this.titleVerificationResult.verificationStatus == VerificationStatus.NEGATIVE ||
                this.descriptionVerificationResult.verificationStatus == VerificationStatus.NEGATIVE
    }
    fun hasFailed() : Boolean {
        return this.titleVerificationResult.verificationStatus == VerificationStatus.FAILED ||
                this.descriptionVerificationResult.verificationStatus == VerificationStatus.FAILED
    }

    fun getVerificationMessage(): String {
        return "Title: ${titleVerificationResult}, Description: ${descriptionVerificationResult}"
    }
}


data class SingleVerificationResult(
    val verificationStatus: VerificationStatus,
    val message: String
)


enum class VerificationStatus {
    POSITIVE, NEGATIVE, FAILED
}