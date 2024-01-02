package pl.kawaleria.auctsys.images.domain

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode
import pl.kawaleria.auctsys.images.dto.exceptions.ImageViolation
import pl.kawaleria.auctsys.images.dto.exceptions.ImagesValidationException

@Component
class ImageValidator(
        private val JPG: String = "jpg",
        private val PNG: String = "png",
        private val CONTENT_TYPE_JPG: String = "image/jpg",
        private val CONTENT_TYPE_JPEG: String = "image/jpeg",
        private val CONTENT_TYPE_PNG: String = "image/png",
        private val MAX_FILE_SIZE: Long = 1024 * 1024 * 10) {

    fun validateMultipartFiles(files: List<MultipartFile>) {
        val imageViolations: List<ImageViolation> = files.mapNotNull { file ->
            val errorCode: ServiceErrorResponseCode? = validateMultipartFile(file)
            errorCode?.let {
                val imageName: String = file.originalFilename ?: "Unknown"
                ImageViolation(imageName, listOf(it))
            }
        }

        imageViolations.takeIf { it.isNotEmpty() }?.let {
            throw ImagesValidationException(it)
        }
    }

    // Will return the first error code that occurs. Methods are ordered by validation priority
    private fun validateMultipartFile(file: MultipartFile): ServiceErrorResponseCode? {
        return validateContentType(file)
            ?: validateFileExtension(file)
            ?: validateFileSize(file)
            ?: validateFileType(file)
    }

    private fun validateFileType(file: MultipartFile) : ServiceErrorResponseCode? {
        val fileType: String = getFileType(file.bytes)
        return if (fileType != PNG && fileType != JPG) ServiceErrorResponseCode.IMG04 else null

    }

    private fun validateContentType(file: MultipartFile): ServiceErrorResponseCode? {
        return if (file.contentType != CONTENT_TYPE_JPG && file.contentType != CONTENT_TYPE_PNG && file.contentType != CONTENT_TYPE_JPEG)
            ServiceErrorResponseCode.IMG02
        else null
    }

    private fun validateFileExtension(file: MultipartFile): ServiceErrorResponseCode? {
        val fileExtension: String = getFileExtension(file)
        return if (fileExtension != JPG && fileExtension != PNG) ServiceErrorResponseCode.IMG03 else null
    }

    private fun validateFileSize(file: MultipartFile): ServiceErrorResponseCode? {
        return if (file.size > MAX_FILE_SIZE) ServiceErrorResponseCode.IMG05 else null
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

    private fun getFileType(fileData: ByteArray): String {
        return when {
            fileData.getOrNull(0)?.toUByte() == 0x89.toUByte() && fileData.getOrNull(1)?.toUByte() == 0x50.toUByte() -> PNG
            fileData.getOrNull(0)?.toUByte() == 0xFF.toUByte() && fileData.getOrNull(1)?.toUByte() == 0xD8.toUByte() -> JPG
            else -> "Unsupported file type"
        }
    }
}