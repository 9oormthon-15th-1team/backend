package training.goorm.portholemapapi.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import training.goorm.portholemapapi.entity.Pothole
import java.time.LocalDateTime

@Schema(description = "포트홀 응답 데이터")
data class PotholeResponse(
    @Schema(description = "포트홀 식별자", example = "1")
    val id: Long,

    @Schema(description = "위도", example = "37.5665")
    val latitude: Double,

    @Schema(description = "경도", example = "126.9780")
    val longitude: Double,

    @Schema(description = "포트홀 설명", example = "도로에 큰 구멍이 있습니다")
    val description: String,

    @Schema(description = "이미지 URL", example = "https://example.com/images/pothole1.jpg")
    val imageUrl: String?,

    @Schema(description = "생성시각", example = "2025-09-25T15:30:00")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(pothole: Pothole): PotholeResponse {
            return PotholeResponse(
                id = pothole.id ?: throw IllegalArgumentException("Pothole ID cannot be null"),
                latitude = pothole.latitude,
                longitude = pothole.longitude,
                description = pothole.description,
                imageUrl = pothole.imageUrl,
                createdAt = pothole.createdAt
            )
        }
    }
}

@Schema(description = "포트홀 생성 요청 데이터")
data class CreatePotholeRequest(
    @Schema(description = "위도", example = "37.5665", required = true)
    val latitude: Double,

    @Schema(description = "경도", example = "126.9780", required = true)
    val longitude: Double,

    @Schema(description = "포트홀 설명", example = "도로에 큰 구멍이 있습니다", required = true)
    val description: String,

    @Schema(description = "이미지 URL", example = "https://example.com/images/pothole1.jpg")
    val imageUrl: String? = null
)

@Schema(description = "포트홀 수정 요청 데이터")
data class UpdatePotholeRequest(
    @Schema(description = "포트홀 설명", example = "수정된 포트홀 설명")
    val description: String? = null,

    @Schema(description = "이미지 URL", example = "https://example.com/images/pothole_updated.jpg")
    val imageUrl: String? = null
)
