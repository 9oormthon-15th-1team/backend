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

    @Operation(summary = "포트홀 목록 조회", description = "등록된 포트홀 목록을 조회합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "성공")
    ])
    @GetMapping
    fun getPotholes(): ApiResponse<List<PotholeResponse>> {
        val potholes = potholeService.getAllPotholes()
        return ApiResponse.success(potholes, "포트홀 목록 조회 성공")
    }

    @Operation(summary = "포트홀 상세 조회", description = "특정 포트홀의 상세 정보를 조회합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "성공"),
        SwaggerApiResponse(responseCode = "404", description = "포트홀을 찾을 수 없음")
    ])
    @GetMapping("/{id}")
    fun getPothole(@PathVariable id: Long): ApiResponse<PotholeResponse> {
        val pothole = potholeService.getPotholeById(id)
        return ApiResponse.success(pothole, "포트홀 상세 정보 조회 성공")
    }

    @Operation(summary = "포트홀 등록", description = "새로운 포트홀을 등록합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "201", description = "생성됨"),
        SwaggerApiResponse(responseCode = "400", description = "잘못된 요청")
    ])
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPothole(@RequestBody request: CreatePotholeRequest): ApiResponse<PotholeResponse> {
        val createdPothole = potholeService.createPothole(request)
        return ApiResponse.success(createdPothole, "포트홀 등록 성공")
    }

    @Operation(summary = "포트홀 수정", description = "기존 포트홀 정보를 수정합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "성공"),
        SwaggerApiResponse(responseCode = "404", description = "포트홀을 찾을 수 없음")
    ])
    @PutMapping("/{id}")
    fun updatePothole(
        @PathVariable id: Long,
        @RequestBody request: UpdatePotholeRequest
    ): ApiResponse<PotholeResponse> {
        val updatedPothole = potholeService.updatePothole(id, request)
        return ApiResponse.success(updatedPothole, "포트홀 수정 성공")
    }

    @Operation(summary = "포트홀 삭제", description = "포트홀을 삭제합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "성공"),
        SwaggerApiResponse(responseCode = "404", description = "포트홀을 찾을 수 없음")
    ])
    @DeleteMapping("/{id}")
    fun deletePothole(@PathVariable id: Long): ApiResponse<Void> {
        potholeService.deletePothole(id)
        return ApiResponse.success(message = "포트홀 삭제 성공")
    }

    @Operation(summary = "위치 기반 포트홀 검색", description = "지정된 위도/경도 범위 내의 포트홀을 검색합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "성공")
    ])
    @GetMapping("/search/location")
    fun searchPotholesByLocation(
        @Parameter(description = "최소 위도", example = "37.5000")
        @RequestParam minLatitude: Double,
        @Parameter(description = "최대 위도", example = "37.6000")
        @RequestParam maxLatitude: Double,
        @Parameter(description = "최소 경도", example = "126.9000")
        @RequestParam minLongitude: Double,
        @Parameter(description = "최대 경도", example = "127.0000")
        @RequestParam maxLongitude: Double
    ): ApiResponse<List<PotholeResponse>> {
        val potholes = potholeService.getPotholesByLocationRange(
            minLatitude, maxLatitude, minLongitude, maxLongitude
        )
        return ApiResponse.success(potholes, "위치 기반 포트홀 검색 성공")
    }

    @Operation(summary = "키워드 기반 포트홀 검색", description = "설명에 특정 키워드가 포함된 포트홀을 검색합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "성공")
    ])
    @GetMapping("/search")
    fun searchPotholesByKeyword(
        @Parameter(description = "검색 키워드", example = "큰 구멍")
        @RequestParam keyword: String
    ): ApiResponse<List<PotholeResponse>> {
        val potholes = potholeService.searchPotholesByKeyword(keyword)
        return ApiResponse.success(potholes, "키워드 검색 성공")
    }

    @Operation(summary = "이미지가 있는 포트홀 조회", description = "이미지가 등록된 포트홀만 조회합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "성공")
    ])
    @GetMapping("/with-images")
    fun getPotholesWithImages(): ApiResponse<List<PotholeResponse>> {
        val potholes = potholeService.getPotholesWithImage()
        return ApiResponse.success(potholes, "이미지 포함 포트홀 조회 성공")
    }

    @Operation(summary = "최근 등록된 포트홀 조회", description = "최근 등록된 포트홀 10개를 조회합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "성공")
    ])
    @GetMapping("/recent")
    fun getRecentPotholes(): ApiResponse<List<PotholeResponse>> {
        val potholes = potholeService.getRecentPotholes()
        return ApiResponse.success(potholes, "최근 포트홀 조회 성공")
    }

    @Operation(summary = "포트홀 통계 조회", description = "전체 포트홀 개수를 조회합니다.")
    @ApiResponses(value = [
        SwaggerApiResponse(responseCode = "200", description = "성공")
    ])
    @GetMapping("/stats")
    fun getPotholeStats(): ApiResponse<Map<String, Long>> {
        val totalCount = potholeService.getTotalPotholeCount()
        val stats = mapOf("totalCount" to totalCount)
        return ApiResponse.success(stats, "포트홀 통계 조회 성공")
    }
}
