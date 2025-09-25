package training.goorm.portholemapapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import training.goorm.portholemapapi.service.FileStorageService

@Tag(name = "Images", description = "이미지 파일 접근 API")
@RestController
@RequestMapping("/images")
class ImageController(
    private val fileStorageService: FileStorageService
) {

    @Operation(summary = "이미지 파일 조회", description = "저장된 이미지 파일을 바이너리로 반환합니다.")
    @GetMapping("/reports/{filename:.+}")
    fun getReportImage(
        @Parameter(description = "이미지 파일명", example = "report_1727259600000_0.jpg")
        @PathVariable filename: String
    ): ResponseEntity<ByteArray> {
        try {
            val objectKey = "reports/$filename"
            val (fileBytes, contentType) = fileStorageService.getFileFromStorage(objectKey)

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(fileBytes.size.toLong())
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000") // 1년 캐시
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
                .body(fileBytes)

        } catch (ex: RuntimeException) {
            return ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "이미지 파일 조회 (일반 경로)", description = "S3 버킷 경로를 따라 이미지 파일에 접근합니다.")
    @GetMapping("/{path:.+}")
    fun getImage(
        @Parameter(description = "파일 경로", example = "reports/report_1727259600000_0.jpg")
        @PathVariable path: String
    ): ResponseEntity<ByteArray> {
        try {
            val (fileBytes, contentType) = fileStorageService.getFileFromStorage(path)

            // 파일명 추출
            val filename = path.substringAfterLast("/")

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(fileBytes.size.toLong())
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000") // 1년 캐시
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
                .header(HttpHeaders.ETAG, "\"${path.hashCode()}\"") // ETag 추가
                .body(fileBytes)

        } catch (ex: RuntimeException) {
            return ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "이미지 메타데이터 조회", description = "이미지 파일의 메타데이터만 조회합니다.")
    @RequestMapping("/{path:.+}", method = [RequestMethod.HEAD])
    fun getImageMetadata(
        @Parameter(description = "S3 객체 키 전체 경로")
        @PathVariable path: String
    ): ResponseEntity<Unit> {
        try {
            if (fileStorageService.doesFileExist("https://goormthon-1.goorm.training/api/images/$path")) {
                val filename = path.substringAfterLast("/")
                val contentType = getContentTypeFromExtension(filename)

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                    .header(HttpHeaders.ETAG, "\"${path.hashCode()}\"")
                    .build()
            } else {
                return ResponseEntity.notFound().build()
            }
        } catch (ex: Exception) {
            return ResponseEntity.notFound().build()
        }
    }

    private fun getContentTypeFromExtension(filename: String): String {
        val extension = filename.substringAfterLast(".").lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}
