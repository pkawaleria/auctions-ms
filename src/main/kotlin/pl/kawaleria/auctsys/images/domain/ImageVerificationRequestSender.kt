package pl.kawaleria.auctsys.images.domain


interface ImageVerificationRequestSender {
    fun sendToVerification(auctionId: String, imagesToVer: ImagesVerificationEvent)
}