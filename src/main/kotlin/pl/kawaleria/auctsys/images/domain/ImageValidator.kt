package pl.kawaleria.auctsys.images.domain

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException

@Component
class ImageValidator(
    private val JPG: String = "jpg",
    private val PNG: String = "png",
    private val CONTENT_TYPE_JPG: String = "image/jpg",
    private val CONTENT_TYPE_PNG: String = "image/png",
    private val MAX_FILE_SIZE: Long = 1024 * 1024 * 10) {

    fun validateMultipartFileList(files: List<MultipartFile>) {
        for (file: MultipartFile in files) {
            validateMultipartFile(file)
        }
    }
    private fun validateMultipartFile(file: MultipartFile) {
        validateFileType(file)
        validateContentType(file)
        validateFileExtension(file)
        validateFileSize(file)
    }

    private fun validateFileType(file: MultipartFile) {
        val fileType: String = getFileType(file.bytes)
        if (fileType != PNG && fileType != JPG) throw ApiException(400, "Invalid file type")
    }

    private fun getFileType(fileData: ByteArray): String {
        return when {
            fileData.getOrNull(0)?.toUByte() == 0x89.toUByte() && fileData.getOrNull(1)?.toUByte() == 0x50.toUByte() -> PNG
            fileData.getOrNull(0)?.toUByte() == 0xFF.toUByte() && fileData.getOrNull(1)?.toUByte() == 0xD8.toUByte() -> JPG
            else -> "Unsupported file type"
        }
    }

    private fun validateContentType(file: MultipartFile) {
        if (file.contentType != CONTENT_TYPE_JPG && file.contentType != CONTENT_TYPE_PNG) throw ApiException(400, "Invalid file content type")
    }

    private fun validateFileExtension(file: MultipartFile) {
        val fileExtension: String = getFileExtension(file)
        if (fileExtension != JPG && fileExtension != PNG) throw ApiException(400, "Invalid file extension")
    }

    private fun getFileExtension(file: MultipartFile): String {
        val originalFile: String = file.originalFilename ?: return ""
        val lastDotIndex: Int = originalFile.lastIndexOf('.')
        return if (lastDotIndex == -1) {
            ""
        } else {
            originalFile.substring(lastDotIndex + 1)
        }
    }

    private fun validateFileSize(file: MultipartFile) {
        if (file.size > MAX_FILE_SIZE) throw ApiException(400, "File size is too big")
    }
}