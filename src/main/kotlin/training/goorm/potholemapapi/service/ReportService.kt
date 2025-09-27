package training.goorm.potholemapapi.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import training.goorm.potholemapapi.dto.CreateReportMultipartRequest
import training.goorm.potholemapapi.dto.CreateReportRequest
import training.goorm.potholemapapi.dto.ReportResponse
import training.goorm.potholemapapi.dto.UpdateReportRequest
import training.goorm.potholemapapi.entity.Pothole
import training.goorm.potholemapapi.entity.Report
import training.goorm.potholemapapi.exception.ReportNotFoundException
import training.goorm.potholemapapi.repository.PotholeRepository
import training.goorm.potholemapapi.repository.ReportRepository
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ReportService(
    private val reportRepository: ReportRepository,
    private val potholeRepository: PotholeRepository,
    private val naverGeocodingService: NaverGeocodingService
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
        val existingPothole = findOrCreatePothole(request.latitude, request.longitude, request.description)

        // address가 없을 경우 네이버 지오코딩으로 주소 생성
        val address = naverGeocodingService.getAddressFromCoordinates(request.latitude, request.longitude)

        val report = Report(
            latitude = request.latitude,
            longitude = request.longitude,
            address = address,
            imageUrls = request.imageUrls,
            description = request.description,
            pothole = existingPothole,
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
        val existingPothole = findOrCreatePothole(request.latitude, request.longitude, request.description)

        // 이미지 파일들을 저장하고 URL 목록 획득
        val imageUrls = fileStorageService.storeImageFiles(imageFiles)

        // 위도/경도로부터 주소 생성 (네이버 리버스 지오코딩 API 사용)
        val address = naverGeocodingService.getAddressFromCoordinates(request.latitude, request.longitude)

        val report = Report(
            latitude = request.latitude,
            longitude = request.longitude,
            address = address,
            imageUrls = imageUrls,
            description = request.description,
            pothole = existingPothole,
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
            updatedAt = LocalDateTime.now(),
            pothole = existingReport.pothole,
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

    /**
     * 1m 반경 내 기존 포트홀을 찾거나 새로 생성
     */
    private fun findOrCreatePothole(latitude: Double, longitude: Double, description: String?): Pothole {
        val nearbyPotholes = potholeRepository.findPotholesWithinRadius(latitude, longitude, 1.0)

        return if (nearbyPotholes.isNotEmpty()) {
            nearbyPotholes.first()
        } else {
            val newPothole = Pothole(
                latitude = latitude,
                longitude = longitude,
                description = description ?: "새로 발견된 포트홀",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            potholeRepository.save(newPothole)
        }
    }
}
