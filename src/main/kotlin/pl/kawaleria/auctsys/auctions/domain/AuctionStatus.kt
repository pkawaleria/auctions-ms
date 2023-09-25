package pl.kawaleria.auctsys.auctions.domain

enum class AuctionStatus(
        private val operation: AuctionStatusOperations,
        val operationName: String,
        val statusName: String): AuctionStatusOperations {

    NEW(operation = NewAso(), operationName = "creating new auction", statusName = "newly created"),
    AWAITING_VERIFICATION(operation = AwaitingVerificationAso(), operationName = "awaiting verification", statusName = "awaits verification" ),
    ACCEPTED(operation = AcceptedAso(), operationName = "acceptance", statusName = "accepted"),
    REJECTED(operation = RejectedAso(), operationName = "rejection", statusName = "rejected"),
    ARCHIVED(operation = ArchivedAso(), operationName = "archiving", statusName = "archived");

    override fun accept(auction: Auction): AuctionStatus = operation.accept(auction)

    override fun reject(auction: Auction): AuctionStatus = operation.reject(auction)

    override fun archive(auction: Auction): AuctionStatus = operation.archive(auction)
}