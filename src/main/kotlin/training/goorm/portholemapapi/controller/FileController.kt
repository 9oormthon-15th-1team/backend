package training.goorm.portholemapapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import training.goorm.portholemapapi.dto.ApiResponse
import training.goorm.portholemapapi.service.FileStorageService
import java.net.URI

@Tag(name = "Files", description = "파일 관리 API (레거시 호환)")
@RestController
@RequestMapping("/files")
class FileController(
    private val fileStorageService: FileStorageService
) {

    @Operation(
        summary = "파일 리다이렉트 (Deprecated)",
        description = "레거시 지원을 위한 리다이렉트. /api/images/ 경로 사용을 권장합니다.",
        deprecated = true
    )
    @GetMapping("/{filename:.+}")
    fun redirectToImageApi(@Parameter(description = "파일명") @PathVariable filename: String): ResponseEntity<Void> {
        // /api/images/ 경로로 리다이렉트
        val redirectUrl = "https://goormthon-1.goorm.training/api/images/reports/$filename"
        return ResponseEntity.status(302)
            .location(URI.create(redirectUrl))
            .build()
    }

    @Operation(summary = "파일 정보 조회", description = "파일 존재 여부와 메타데이터를 확인합니다.")
    @GetMapping("/info/{filename:.+}")
    fun getFileInfo(@Parameter(description = "파일명") @PathVariable filename: String): ApiResponse<Map<String, Any>> {
        try {
            val fileUrl = "https://goormthon-1.goorm.training/api/images/reports/$filename"

            if (fileStorageService.doesFileExist(fileUrl)) {
                val fileInfo = mapOf(
                    "filename" to filename,
                    "url" to fileUrl,
                    "exists" to true
                )
                return ApiResponse.success(fileInfo)
            } else {
                return ApiResponse.notFound("파일을 찾을 수 없습니다")
            }
        } catch (ex: Exception) {
            return ApiResponse.internalServerError("파일 정보 조회 중 오류가 발생했습니다")
        }
    }
}
