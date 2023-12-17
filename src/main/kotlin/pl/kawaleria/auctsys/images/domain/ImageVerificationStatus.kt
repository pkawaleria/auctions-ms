package pl.kawaleria.auctsys.images.domain

enum class ImageVerificationStatus {
    PENDING, REJECTED, ACCEPTED, REQUIRES_MANUAL_VERIFICATION, VERIFICATION_OMITTED
}