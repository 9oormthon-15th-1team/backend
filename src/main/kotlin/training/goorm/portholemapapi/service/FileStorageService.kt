package training.goorm.portholemapapi.service

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import training.goorm.portholemapapi.config.S3Properties
import training.goorm.portholemapapi.exception.ReportValidationException
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
@EnableConfigurationProperties(S3Properties::class)
class FileStorageService(
    private val s3Properties: S3Properties
) {

    private val uploadDir = "uploads"

    init {
        // 업로드 디렉토리 생성
        try {
            Files.createDirectories(Paths.get(uploadDir))
        } catch (ex: IOException) {
            throw RuntimeException("Could not create upload directory!", ex)
        }
    }

    /**
     * 이미지 파일들을 저장하고 URL 목록을 반환 (임시로 로컬 저장)
     */
    fun storeImageFiles(files: List<MultipartFile>): List<String> {
        if (files.isEmpty()) {
            throw ReportValidationException("이미지는 최소 1장 이상 등록해야 합니다")
        }

        if (files.size > 6) {
            throw ReportValidationException("이미지는 최대 6장까지만 등록 가능합니다")
        }

        return files.mapIndexed { index, file ->
            storeImageFile(file, index)
        }
    }

    /**
     * 단일 이미지 파일을 저장하고 URL을 반환
     */
    private fun storeImageFile(file: MultipartFile, index: Int): String {
        if (file.isEmpty) {
            throw ReportValidationException("빈 파일은 업로드할 수 없습니다")
        }

        // 파일 확장자 검증
        val originalFilename = file.originalFilename ?: throw ReportValidationException("파일명이 없습니다")
        val extension = getFileExtension(originalFilename)

        if (!isValidImageExtension(extension)) {
            throw ReportValidationException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp만 지원)")
        }

        // 파일 크기 검증 (5MB 제한)
        if (file.size > 5 * 1024 * 1024) {
            throw ReportValidationException("이미지 파일 크기는 5MB 이하여야 합니다")
        }

        // 고유한 파일명 생성
        val timestamp = System.currentTimeMillis()
        val uniqueFilename = "report_${timestamp}_${index}${extension}"
        val relativePath = "reports/$uniqueFilename"

        try {
            // 디렉토리 생성
            val reportsDir = Paths.get(uploadDir, "reports")
            Files.createDirectories(reportsDir)

            // 파일 저장
            val targetLocation = reportsDir.resolve(uniqueFilename)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            // URL 반환 (API 서버 경로 사용)
            return "https://goormthon-1.goorm.training/api/images/${relativePath}"

        } catch (ex: IOException) {
            throw RuntimeException("파일 저장 중 오류가 발생했습니다", ex)
        }
    }

    /**
     * 로컬 파일시스템에서 파일을 가져와서 바이너리 데이터로 반환
     */
    fun getFileFromStorage(objectKey: String): Pair<ByteArray, String> {
        try {
            val filePath = Paths.get(uploadDir, objectKey)

            if (!Files.exists(filePath)) {
                throw RuntimeException("파일을 찾을 수 없습니다: $objectKey")
            }

            val fileBytes = Files.readAllBytes(filePath)
            val contentType = getContentTypeFromPath(objectKey)

            return Pair(fileBytes, contentType)

        } catch (ex: Exception) {
            throw RuntimeException("파일을 가져오는 중 오류가 발생했습니다: ${ex.message}")
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    fun doesFileExist(fileUrl: String): Boolean {
        return try {
            val objectKey = extractObjectKeyFromUrl(fileUrl)
            val filePath = Paths.get(uploadDir, objectKey)
            Files.exists(filePath)
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * 파일 삭제
     */
    fun deleteFile(fileUrl: String): Boolean {
        return try {
            val objectKey = extractObjectKeyFromUrl(fileUrl)
            val filePath = Paths.get(uploadDir, objectKey)
            Files.deleteIfExists(filePath)
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * URL에서 객체 키 추출
     */
    private fun extractObjectKeyFromUrl(fileUrl: String): String {
        return if (fileUrl.contains("goormthon-1.goorm.training/api/images/")) {
            fileUrl.substringAfter("goormthon-1.goorm.training/api/images/")
        } else {
            throw ReportValidationException("유효하지 않은 파일 URL입니다")
        }
    }

    /**
     * 파일 확장자 추출
     */
    private fun getFileExtension(filename: String): String {
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex > 0 && lastDotIndex < filename.length - 1) {
            filename.substring(lastDotIndex).lowercase()
        } else {
            ""
        }
    }

    /**
     * 유효한 이미지 확장자인지 검증
     */
    private fun isValidImageExtension(extension: String): Boolean {
        val validExtensions = setOf(".jpg", ".jpeg", ".png", ".gif", ".webp")
        return validExtensions.contains(extension)
    }

    /**
     * 파일 경로에서 Content-Type 결정
     */
    private fun getContentTypeFromPath(path: String): String {
        val extension = getFileExtension(path)
        return when (extension.lowercase()) {
            ".jpg", ".jpeg" -> "image/jpeg"
            ".png" -> "image/png"
            ".gif" -> "image/gif"
            ".webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}
