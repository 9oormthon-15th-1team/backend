package training.goorm.portholemapapi.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import training.goorm.portholemapapi.entity.Report
import java.time.LocalDateTime

@Schema(description = "포트홀 제보 응답 데이터")
data class ReportResponse(
    @Schema(description = "제보 ID", example = "1")
    val id: Long,

    @Schema(description = "위도", example = "37.5665")
    val latitude: Double,

    @Schema(description = "경도", example = "126.9780")
    val longitude: Double,

    @Schema(description = "주소", example = "서울특별시 중구 세종대로 110")
    val address: String,

    @Schema(description = "이미지 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    val imageUrls: List<String>,

    @Schema(description = "설명", example = "도로에 큰 구멍이 있어 위험합니다")
    val description: String?,

    @Schema(description = "생성시각", example = "2025-09-25T15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,

    @Schema(description = "수정시각", example = "2025-09-25T15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(report: Report): ReportResponse {
            return ReportResponse(
                id = report.id ?: throw IllegalArgumentException("Report ID cannot be null"),
                latitude = report.latitude,
                longitude = report.longitude,
                address = report.address,
                imageUrls = report.imageUrls,
                description = report.description,
                createdAt = report.createdAt,
                updatedAt = report.updatedAt
            )
        }
    }
}

@Schema(description = "포트홀 제보 생성 요청 데이터")
data class CreateReportRequest(
    @Schema(description = "위도", example = "37.5665", required = true)
    @field:NotNull(message = "위도는 필수 입력값입니다")
    @field:DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다")
    val latitude: Double,

    @Schema(description = "경도", example = "126.9780", required = true)
    @field:NotNull(message = "경도는 필수 입력값입니다")
    @field:DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다")
    val longitude: Double,

    @Schema(description = "이미지 URL 목록 (1장~6장)", example = "[\"https://example.com/image1.jpg\"]", required = true)
    @field:NotEmpty(message = "이미지는 최소 1장 이상 등록해야 합니다")
    @field:Size(min = 1, max = 6, message = "이미지는 1장에서 6장까지 등록 가능합니다")
    val imageUrls: List<@NotBlank(message = "이미지 URL은 빈 값일 수 없습니다") String>,

    @Schema(description = "설명", example = "도로에 큰 구멍이 있어 위험합니다")
    @field:Size(max = 2000, message = "설명은 2000자 이하여야 합니다")
    val description: String? = null
)

@Schema(description = "포트홀 제보 수정 요청 데이터")
data class UpdateReportRequest(
    @Schema(description = "주소", example = "서울특별시 중구 세종대로 110")
    @field:Size(max = 1000, message = "주소는 1000자 이하여야 합니다")
    val address: String? = null,

    @Schema(description = "이미지 URL 목록 (1장~6장)", example = "[\"https://example.com/image1.jpg\"]")
    @field:Size(min = 1, max = 6, message = "이미지는 1장에서 6장까지 등록 가능합니다")
    val imageUrls: List<@NotBlank(message = "이미지 URL은 빈 값일 수 없습니다") String>? = null,

    @Schema(description = "설명", example = "수정된 설명")
    @field:Size(max = 2000, message = "설명은 2000자 이하여야 합니다")
    val description: String? = null
)

@Schema(description = "포트홀 제보 생성 요청 데이터 (Multipart)")
data class CreateReportMultipartRequest(
    @Schema(description = "위도", example = "37.5665", required = true)
    @field:NotNull(message = "위도는 필수 입력값입니다")
    @field:DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다")
    val latitude: Double,

    @Schema(description = "경도", example = "126.9780", required = true)
    @field:NotNull(message = "경도는 필수 입력값입니다")
    @field:DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다")
    val longitude: Double,

    @Schema(description = "설명", example = "도로에 큰 구멍이 있어 위험합니다")
    @field:Size(max = 2000, message = "설명은 2000자 이하여야 합니다")
    val description: String? = null
)
