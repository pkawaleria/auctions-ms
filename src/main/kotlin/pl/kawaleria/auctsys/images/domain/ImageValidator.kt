package pl.kawaleria.auctsys.images.domain

import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.images.dto.exceptions.InvalidFileContentTypeException
import pl.kawaleria.auctsys.images.dto.exceptions.InvalidFileExtensionException
import pl.kawaleria.auctsys.images.dto.exceptions.InvalidFileSizeException
import pl.kawaleria.auctsys.images.dto.exceptions.InvalidFileTypeException


 class ImageValidator(
        private val JPG: String = "jpg",
        private val PNG: String = "png",
        private val CONTENT_TYPE_JPG: String = "image/jpg",
        private val CONTENT_TYPE_JPEG: String = "image/jpeg",
        private val CONTENT_TYPE_PNG: String = "image/png",
        private val MAX_FILE_SIZE: Long = 1024 * 1024 * 10) {

    fun validateMultipartFiles(files: List<MultipartFile>) {
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
        if (fileType != PNG && fileType != JPG) throw InvalidFileTypeException()
    }

    private fun getFileType(fileData: ByteArray): String {
        return when {
            fileData.getOrNull(0)?.toUByte() == 0x89.toUByte() && fileData.getOrNull(1)?.toUByte() == 0x50.toUByte() -> PNG
            fileData.getOrNull(0)?.toUByte() == 0xFF.toUByte() && fileData.getOrNull(1)?.toUByte() == 0xD8.toUByte() -> JPG
            else -> "Unsupported file type"
        }
    }

    private fun validateContentType(file: MultipartFile) {
        if (file.contentType != CONTENT_TYPE_JPG
            && file.contentType != CONTENT_TYPE_PNG
            && file.contentType != CONTENT_TYPE_JPEG) throw InvalidFileContentTypeException()
    }

    private fun validateFileExtension(file: MultipartFile) {
        val fileExtension: String = getFileExtension(file)
        if (fileExtension != JPG && fileExtension != PNG) throw InvalidFileExtensionException()
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
        if (file.size > MAX_FILE_SIZE) throw InvalidFileSizeException()
    }
}