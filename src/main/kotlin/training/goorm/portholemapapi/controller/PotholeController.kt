package training.goorm.portholemapapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import training.goorm.portholemapapi.dto.ApiResponse
import training.goorm.portholemapapi.dto.CreatePotholeRequest
import training.goorm.portholemapapi.dto.PotholeResponse
import training.goorm.portholemapapi.dto.UpdatePotholeRequest
import training.goorm.portholemapapi.service.PotholeService

@Tag(name = "Potholes", description = "포트홀 관련 API")
@RestController
@RequestMapping("/potholes")
class PortholeController(
    private val potholeService: PotholeService
) {

    @Operation(summary = "포트홀 목록 조회", description = "등록된 포트홀 목록을 조회합니다. 위도와 경도를 제공하면 해당 위치로부터의 거리가 포함됩니다.")
    @GetMapping
    fun getPotholes(
        @Parameter(description = "기준 위도", example = "33.450208")
        @RequestParam(required = false) latitude: Double?,
        @Parameter(description = "기준 경도", example = "126.918355")
        @RequestParam(required = false) longitude: Double?
    ): ApiResponse<List<PotholeResponse>> {
        val potholes = if (latitude != null && longitude != null) {
            potholeService.getAllPotholesWithDistance(latitude, longitude)
        } else {
            potholeService.getAllPotholes()
        }
        return ApiResponse.success(potholes)
    }

    @Operation(summary = "포트홀 상세 조회", description = "특정 포트홀의 상세 정보를 조회합니다. 위도와 경도를 제공하면 해당 위치로부터의 거리가 포함됩니다.")
    @GetMapping("/{id}")
    fun getPothole(
        @PathVariable id: Long,
        @Parameter(description = "기준 위도", example = "33.450208")
        @RequestParam(required = false) latitude: Double?,
        @Parameter(description = "기준 경도", example = "126.918355")
        @RequestParam(required = false) longitude: Double?
    ): ApiResponse<PotholeResponse> {
        val pothole = if (latitude != null && longitude != null) {
            potholeService.getPotholeByIdWithDistance(id, latitude, longitude)
        } else {
            potholeService.getPotholeById(id)
        }
        return ApiResponse.success(pothole)
    }

    @Operation(summary = "포트홀 등록", description = "새로운 포트홀을 등록합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPothole(@RequestBody request: CreatePotholeRequest): ApiResponse<PotholeResponse> {
        val createdPothole = potholeService.createPothole(request)
        return ApiResponse.success(createdPothole)
    }

    @Operation(summary = "포트홀 수정", description = "기존 포트홀 정보를 수정합니다.")
    @PutMapping("/{id}")
    fun updatePothole(
        @PathVariable id: Long,
        @RequestBody request: UpdatePotholeRequest
    ): ApiResponse<PotholeResponse> {
        val updatedPothole = potholeService.updatePothole(id, request)
        return ApiResponse.success(updatedPothole)
    }

    @Operation(summary = "포트홀 삭제", description = "포트홀을 삭제합니다.")
    @DeleteMapping("/{id}")
    fun deletePothole(@PathVariable id: Long): ApiResponse<Unit> {
        potholeService.deletePothole(id)
        return ApiResponse.success()
    }

    @Operation(summary = "위치 기반 포트홀 검색", description = "기준 좌표에서 지정된 거리 내의 포트홀을 거리순으로 검색합니다.")
    @GetMapping("/search/location")
    fun searchPotholesByLocation(
        @Parameter(description = "기준 위도", example = "37.5665")
        @RequestParam latitude: Double,
        @Parameter(description = "기준 경도", example = "126.9780")
        @RequestParam longitude: Double,
        @Parameter(description = "검색 반경 (미터)", example = "1000")
        @RequestParam distance: Double
    ): ApiResponse<List<PotholeResponse>> {
        val potholes = potholeService.getPotholesWithinRadius(
            latitude, longitude, distance
        )
        return ApiResponse.success(potholes)
    }

    @Operation(summary = "키워드 기반 포트홀 검색", description = "설명에 특정 키워드가 포함된 포트홀을 검색합니다.")
    @GetMapping("/search")
    fun searchPotholesByKeyword(
        @Parameter(description = "검색 키워드", example = "큰 구멍")
        @RequestParam keyword: String
    ): ApiResponse<List<PotholeResponse>> {
        val potholes = potholeService.searchPotholesByKeyword(keyword)
        return ApiResponse.success(potholes)
    }

    @Operation(summary = "이미지가 있는 포트홀 조회", description = "이미지가 등록된 포트홀만 조회합니다.")
    @GetMapping("/with-images")
    fun getPotholesWithImages(): ApiResponse<List<PotholeResponse>> {
        val potholes = potholeService.getPotholesWithImage()
        return ApiResponse.success(potholes)
    }

    @Operation(summary = "최근 등록된 포트홀 조회", description = "최근 등록된 포트홀 10개를 조회합니다.")
    @GetMapping("/recent")
    fun getRecentPotholes(): ApiResponse<List<PotholeResponse>> {
        val potholes = potholeService.getRecentPotholes()
        return ApiResponse.success(potholes)
    }

    @Operation(summary = "포트홀 통계 조회", description = "전체 포트홀 개수를 조회합니다.")
    @GetMapping("/stats")
    fun getPotholeStats(): ApiResponse<Map<String, Long>> {
        val totalCount = potholeService.getTotalPotholeCount()
        val stats = mapOf("totalCount" to totalCount)
        return ApiResponse.success(stats)
    }
}
