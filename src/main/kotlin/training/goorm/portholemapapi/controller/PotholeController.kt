package training.goorm.portholemapapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import training.goorm.portholemapapi.dto.ApiResponse

@Tag(name = "Potholes", description = "포트홀 관련 API")
@RestController
@RequestMapping("/api/potholes")
class PortholeController {

    @Operation(summary = "포트홀 목록 조회", description = "등록된 포트홀 목록을 조회합니다.")
    @GetMapping
    fun getPotholes(): ApiResponse<List<String>> {
        val potholeList = listOf("Pothole 1", "Pothole 2", "Pothole 3")
        return ApiResponse.success(potholeList, "포트홀 목록 조회 성공")
    }

    @Operation(summary = "포트홀 상세 조회", description = "특정 포트홀의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    fun getPothole(@PathVariable id: Long): ApiResponse<String> {
        return if (id > 0) {
            ApiResponse.success("Pothole $id details", "포트홀 상세 정보 조회 성공")
        } else {
            ApiResponse.badRequest("유효하지 않은 ID입니다")
        }
    }

    @Operation(summary = "포트홀 등록", description = "새로운 포트홀을 등록합니다.")
    @PostMapping
    fun createPothole(@RequestBody request: Map<String, Any>): ApiResponse<Map<String, Any>> {
        val createdPothole = mapOf(
            "id" to 1,
            "message" to "Pothole created successfully"
        )
        return ApiResponse.success(createdPothole, "포트홀 등록 성공")
    }
}
