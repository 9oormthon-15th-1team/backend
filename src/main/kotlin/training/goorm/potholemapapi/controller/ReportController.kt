package training.goorm.potholemapapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import training.goorm.potholemapapi.dto.ApiResponse
import training.goorm.potholemapapi.dto.CreateReportMultipartRequest
import training.goorm.potholemapapi.dto.CreateReportRequest
import training.goorm.potholemapapi.dto.ReportResponse
import training.goorm.potholemapapi.dto.UpdateReportRequest
import training.goorm.potholemapapi.service.FileStorageService
import training.goorm.potholemapapi.service.ReportService

@Tag(name = "Reports", description = "포트홀 제보 관련 API")
@RestController
@RequestMapping("/reports")
class ReportController(
    private val reportService: ReportService,
    private val fileStorageService: FileStorageService
) {

    @Operation(summary = "제보 목록 조회", description = "등록된 포트홀 제보 목록을 조회합니다.")
    @GetMapping
    fun getReports(): ApiResponse<List<ReportResponse>> {
        val reports = reportService.getAllReports()
        return ApiResponse.success(reports)
    }

    @Operation(summary = "제보 상세 조회", description = "특정 포트홀 제보의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    fun getReport(@PathVariable id: Long): ApiResponse<ReportResponse> {
        val report = reportService.getReportById(id)
        return ApiResponse.success(report)
    }

    @Operation(summary = "제보 등록", description = "새로운 포트홀 제보를 등록합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReport(@Valid @RequestBody request: CreateReportRequest): ApiResponse<ReportResponse> {
        val createdReport = reportService.createReport(request)
        return ApiResponse.success(createdReport)
    }

    @Operation(summary = "제보 수정", description = "기존 포트홀 제보 정보를 수정합니다.")
    @PutMapping("/{id}")
    fun updateReport(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateReportRequest
    ): ApiResponse<ReportResponse> {
        val updatedReport = reportService.updateReport(id, request)
        return ApiResponse.success(updatedReport)
    }

    @Operation(summary = "제보 삭제", description = "포트홀 제보를 삭제합니다.")
    @DeleteMapping("/{id}")
    fun deleteReport(@PathVariable id: Long): ApiResponse<Unit> {
        reportService.deleteReport(id)
        return ApiResponse.success()
    }

    @Operation(summary = "위치 기반 제보 검색", description = "지정된 위도/경도 범위 내의 제보를 검색합니다.")
    @GetMapping("/search/location")
    fun searchReportsByLocation(
        @Parameter(description = "최소 위도", example = "37.5000")
        @RequestParam minLatitude: Double,
        @Parameter(description = "최대 위도", example = "37.6000")
        @RequestParam maxLatitude: Double,
        @Parameter(description = "최소 경도", example = "126.9000")
        @RequestParam minLongitude: Double,
        @Parameter(description = "최대 경도", example = "127.0000")
        @RequestParam maxLongitude: Double
    ): ApiResponse<List<ReportResponse>> {
        val reports = reportService.getReportsByLocationRange(
            minLatitude, maxLatitude, minLongitude, maxLongitude
        )
        return ApiResponse.success(reports)
    }

    @Operation(summary = "주소 기반 제보 검색", description = "주소에 특정 키워드가 포함된 제보를 검색합니다.")
    @GetMapping("/search/address")
    fun searchReportsByAddress(
        @Parameter(description = "주소 검색 키워드", example = "서울")
        @RequestParam keyword: String
    ): ApiResponse<List<ReportResponse>> {
        val reports = reportService.searchReportsByAddress(keyword)
        return ApiResponse.success(reports)
    }

    @Operation(summary = "설명 기반 제보 검색", description = "설명에 특정 키워드가 포함된 제보를 검색합니다.")
    @GetMapping("/search/description")
    fun searchReportsByDescription(
        @Parameter(description = "설명 검색 키워드", example = "큰 구멍")
        @RequestParam keyword: String
    ): ApiResponse<List<ReportResponse>> {
        val reports = reportService.searchReportsByDescription(keyword)
        return ApiResponse.success(reports)
    }

    @Operation(summary = "최근 제보 조회", description = "최근 등록된 제보 10개를 조회합니다.")
    @GetMapping("/recent")
    fun getRecentReports(): ApiResponse<List<ReportResponse>> {
        val reports = reportService.getRecentReports()
        return ApiResponse.success(reports)
    }

    @Operation(summary = "제보 통계 조회", description = "전체 제보 개수를 조회합니다.")
    @GetMapping("/stats")
    fun getReportStats(): ApiResponse<Map<String, Long>> {
        val totalCount = reportService.getTotalReportCount()
        val stats = mapOf("totalCount" to totalCount)
        return ApiResponse.success(stats)
    }

    @Operation(summary = "제보 등록 (파일 업로드)", description = "이미지 파일과 함께 새로운 포트홀 제보를 등록합니다.")
    @PostMapping("/with-files", consumes = ["multipart/form-data"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createReportWithFiles(
        @Parameter(description = "위도", required = true) @RequestParam latitude: Double,
        @Parameter(description = "경도", required = true) @RequestParam longitude: Double,
        @Parameter(description = "설명") @RequestParam(required = false) description: String?,
        @Parameter(description = "이미지 파일들 (최대 6장)", required = true)
        @RequestParam("images") imageFiles: List<MultipartFile>
    ): ApiResponse<ReportResponse> {
        val request = CreateReportMultipartRequest(
            latitude = latitude,
            longitude = longitude,
            description = description
        )

        val createdReport = reportService.createReportWithFiles(request, imageFiles, fileStorageService)
        return ApiResponse.success(createdReport)
    }
}
