package pl.kawaleria.auctsys.commons

enum class ServiceErrorResponseCode(var message: String) {

    // Auction module response codes
    AUCT01("Name should contain only alphabetic characters and be of length 1-100"),
    AUCT02("Description should be of length 20-500"),
    AUCT03("Price should be a positive number"),
    AUCT04("Accessed auction does not exist"),
    AUCT06("Cannot perform operation on an expired auction"),
    AUCT07("Found inappropriate content in auction name or description"),
    AUCT08("Cannot change auction to assign an empty category path"),
    AUCT09("Provided radius to search auctions by localization is out of bounds"),
    AUCT10("City for auction search requests is not specified or cannot be found"),
    AUCT11("Invalid operation performed on an auction"),

    // City module response codes
    CIT01("Accessed city does not exist"),
    CIT02("Cannot import cities, found existing cities, the collection should be emptied first"),

    // Category module response codes
    CAT01("Accessed category cannot be found"),
    CAT02("Parent category cannot be found"),
    CAT03("Top-level (root) category cannot be deleted"),

    // Images module response codes
    IMG01("Accessed image cannot be found"),

    IMG00_GENERAL("Image validation failed"),
    IMG02("Image validation failed. Detected file with an invalid content type. Should be IMAGE_PNG or IMAGE_JPG"),
    IMG03("Image validation failed. Detected file with an invalid extension. Should be jpg, jpeg, or png"),
    IMG04("Image validation failed. Detected magic bytes manipulation"),
    IMG05("Image size is too large. Should be less than 10 Megabytes"),

}
