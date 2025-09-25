package training.goorm.portholemapapi.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import training.goorm.portholemapapi.dto.CreateReportMultipartRequest
import training.goorm.portholemapapi.dto.CreateReportRequest
import training.goorm.portholemapapi.dto.ReportResponse
import training.goorm.portholemapapi.dto.UpdateReportRequest
import training.goorm.portholemapapi.entity.Report
import training.goorm.portholemapapi.exception.ReportNotFoundException
import training.goorm.portholemapapi.repository.ReportRepository
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ReportService(
    private val reportRepository: ReportRepository
) {

    /**
     * 모든 제보 목록 조회 (생성일 역순)
     */
    fun getAllReports(): List<ReportResponse> {
        return reportRepository.findAllByOrderByCreatedAtDesc()
            .map { ReportResponse.from(it) }
    }

    /**
     * 제보 상세 정보 조회
     */
    fun getReportById(id: Long): ReportResponse {
        val report = reportRepository.findByIdOrNull(id)
            ?: throw ReportNotFoundException(id)
        return ReportResponse.from(report)
    }

    /**
     * 위치 범위 기반 제보 검색
     */
    fun getReportsByLocationRange(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double
    ): List<ReportResponse> {
        return reportRepository.findByLocationRange(minLatitude, maxLatitude, minLongitude, maxLongitude)
            .map { ReportResponse.from(it) }
    }

    /**
     * 주소로 제보 검색
     */
    fun searchReportsByAddress(keyword: String): List<ReportResponse> {
        return reportRepository.findByAddressContainingIgnoreCase(keyword)
            .map { ReportResponse.from(it) }
    }

    /**
     * 설명으로 제보 검색
     */
    fun searchReportsByDescription(keyword: String): List<ReportResponse> {
        return reportRepository.findByDescriptionContainingIgnoreCase(keyword)
            .map { ReportResponse.from(it) }
    }

    /**
     * 최근 등록된 제보 10개 조회
     */
    fun getRecentReports(): List<ReportResponse> {
        return reportRepository.findTop10ByOrderByCreatedAtDesc()
            .map { ReportResponse.from(it) }
    }

    /**
     * 제보 생성
     */
    @Transactional
    fun createReport(request: CreateReportRequest): ReportResponse {
        val report = Report(
            latitude = request.latitude,
            longitude = request.longitude,
            address = request.address,
            imageUrls = request.imageUrls,
            description = request.description,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedReport = reportRepository.save(report)
        return ReportResponse.from(savedReport)
    }

    /**
     * 제보 생성 (Multipart 파일 업로드)
     */
    @Transactional
    fun createReportWithFiles(
        request: CreateReportMultipartRequest,
        imageFiles: List<MultipartFile>,
        fileStorageService: FileStorageService
    ): ReportResponse {
        // 이미지 파일들을 저장하고 URL 목록 획득
        val imageUrls = fileStorageService.storeImageFiles(imageFiles)

        // 위도/경도로부터 주소 생성 (임시로 좌표 문자열 사용)
        val address = "위도: ${request.latitude}, 경도: ${request.longitude}"

        val report = Report(
            latitude = request.latitude,
            longitude = request.longitude,
            address = address,
            imageUrls = imageUrls,
            description = request.description,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedReport = reportRepository.save(report)
        return ReportResponse.from(savedReport)
    }

    /**
     * 제보 수정
     */
    @Transactional
    fun updateReport(id: Long, request: UpdateReportRequest): ReportResponse {
        val existingReport = reportRepository.findByIdOrNull(id)
            ?: throw ReportNotFoundException(id)

        val updatedReport = Report(
            id = existingReport.id,
            latitude = existingReport.latitude,
            longitude = existingReport.longitude,
            address = request.address ?: existingReport.address,
            imageUrls = request.imageUrls ?: existingReport.imageUrls,
            description = request.description ?: existingReport.description,
            createdAt = existingReport.createdAt,
            updatedAt = LocalDateTime.now()
        )

        val savedReport = reportRepository.save(updatedReport)
        return ReportResponse.from(savedReport)
    }

    /**
     * 제보 삭제
     */
    @Transactional
    fun deleteReport(id: Long) {
        if (!reportRepository.existsById(id)) {
            throw ReportNotFoundException(id)
        }
        reportRepository.deleteById(id)
    }

    /**
     * 제보 존재 여부 확인
     */
    fun existsReportById(id: Long): Boolean {
        return reportRepository.existsById(id)
    }

    /**
     * 전체 제보 개수 조회
     */
    fun getTotalReportCount(): Long {
        return reportRepository.count()
    }
}
