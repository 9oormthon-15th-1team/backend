package training.goorm.potholemapapi.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "마커 상태")
enum class MarkerStatus(
    @Schema(description = "상태 표시명")
    val displayName: String
) {
    @Schema(description = "위험")
    DANGER("위험"),

    @Schema(description = "주의")
    CAUTION("주의"),

    @Schema(description = "검증필요")
    VERIFICATION_REQUIRED("검증필요"),
}