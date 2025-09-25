package training.goorm.portholemapapi.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import training.goorm.portholemapapi.dto.CreatePotholeRequest
import training.goorm.portholemapapi.dto.PotholeResponse
import training.goorm.portholemapapi.dto.UpdatePotholeRequest
import training.goorm.portholemapapi.entity.Pothole
import training.goorm.portholemapapi.exception.PotholeNotFoundException
import training.goorm.portholemapapi.repository.PotholeRepository
import java.time.LocalDateTime
import kotlin.math.*

@Service
@Transactional(readOnly = true)
class PotholeService(
    private val potholeRepository: PotholeRepository,
    private val reportRepository: training.goorm.portholemapapi.repository.ReportRepository,
    private val naverGeocodingService: NaverGeocodingService
) {

    /**
     * 모든 포트홀 목록 조회
     */
    fun getAllPotholes(): List<PotholeResponse> {
        return potholeRepository.findAll()
            .map { pothole ->
                val imageUrls = getImageUrlsForPothole(pothole.id!!)
                PotholeResponse.from(pothole, imageUrls)
            }
    }

    /**
     * 포트홀 상세 정보 조회
     */
    fun getPotholeById(id: Long): PotholeResponse {
        val pothole = potholeRepository.findByIdOrNull(id)
            ?: throw PotholeNotFoundException(id)
        val imageUrls = getImageUrlsForPothole(id)
        return PotholeResponse.from(pothole, imageUrls)
    }

    /**
     * 거리와 함께 포트홀 상세 정보 조회
     */
    fun getPotholeByIdWithDistance(id: Long, latitude: Double, longitude: Double): PotholeResponse {
        val response = getPotholeById(id)
        response.distance = calculateDistance(latitude, longitude, response.latitude, response.longitude)
        return response
    }

    /**
     * 위치 범위 기반 포트홀 검색
     */
    fun getPotholesByLocationRange(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double
    ): List<PotholeResponse> {
        return potholeRepository.findByLocationRange(minLatitude, maxLatitude, minLongitude, maxLongitude)
            .map { pothole ->
                val imageUrls = getImageUrlsForPothole(pothole.id!!)
                PotholeResponse.from(pothole, imageUrls)
            }
    }

    /**
     * 기준 좌표에서 지정된 반경 내의 포트홀 검색 (거리순 정렬)
     */
    fun getPotholesWithinRadius(
        latitude: Double,
        longitude: Double,
        radiusInMeters: Double
    ): List<PotholeResponse> {
        return potholeRepository.findPotholesWithinRadius(latitude, longitude, radiusInMeters)
            .map { pothole ->
                val imageUrls = getImageUrlsForPothole(pothole.id!!)
                PotholeResponse.from(pothole, imageUrls)
            }
    }

    /**
     * 키워드로 포트홀 검색
     */
    fun searchPotholesByKeyword(keyword: String): List<PotholeResponse> {
        return potholeRepository.findByDescriptionContainingIgnoreCase(keyword)
            .map { pothole ->
                val imageUrls = getImageUrlsForPothole(pothole.id!!)
                PotholeResponse.from(pothole, imageUrls)
            }
    }

    /**
     * 이미지가 있는 포트홀만 조회
     */
    fun getPotholesWithImage(): List<PotholeResponse> {
        return potholeRepository.findByImageUrlIsNotNull()
            .map { pothole ->
                val imageUrls = getImageUrlsForPothole(pothole.id!!)
                PotholeResponse.from(pothole, imageUrls)
            }
    }

    /**
     * 최근 등록된 포트홀 10개 조회
     */
    fun getRecentPotholes(): List<PotholeResponse> {
        return potholeRepository.findTop10ByOrderByCreatedAtDesc()
            .map { pothole ->
                val imageUrls = getImageUrlsForPothole(pothole.id!!)
                PotholeResponse.from(pothole, imageUrls)
            }
    }

    /**
     * 포트홀 생성
     */
    @Transactional
    fun createPothole(request: CreatePotholeRequest): PotholeResponse {
        // 네이버 지오코딩 서비스를 통해 주소 가져오기
        val address = try {
            naverGeocodingService.getAddressFromCoordinates(request.latitude, request.longitude)
        } catch (e: Exception) {
            null
        }

        val pothole = Pothole(
            latitude = request.latitude,
            longitude = request.longitude,
            description = request.description,
            imageUrl = request.imageUrl,
            address = address,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedPothole = potholeRepository.save(pothole)
        val imageUrls = getImageUrlsForPothole(savedPothole.id!!)
        return PotholeResponse.from(savedPothole, imageUrls)
    }

    /**
     * 포트홀 수정
     */
    @Transactional
    fun updatePothole(id: Long, request: UpdatePotholeRequest): PotholeResponse {
        val existingPothole = potholeRepository.findByIdOrNull(id)
            ?: throw PotholeNotFoundException(id)

        val updatedPothole = Pothole(
            id = existingPothole.id,
            latitude = existingPothole.latitude,
            longitude = existingPothole.longitude,
            description = request.description ?: existingPothole.description,
            imageUrl = request.imageUrl ?: existingPothole.imageUrl,
            address = existingPothole.address,
            createdAt = existingPothole.createdAt,
            updatedAt = LocalDateTime.now()
        )

        val savedPothole = potholeRepository.save(updatedPothole)
        val imageUrls = getImageUrlsForPothole(savedPothole.id!!)
        return PotholeResponse.from(savedPothole, imageUrls)
    }

    /**
     * 포트홀 삭제
     */
    @Transactional
    fun deletePothole(id: Long) {
        if (!potholeRepository.existsById(id)) {
            throw PotholeNotFoundException(id)
        }
        potholeRepository.deleteById(id)
    }

    /**
     * 포트홀 존재 여부 확인
     */
    fun existsPotholeById(id: Long): Boolean {
        return potholeRepository.existsById(id)
    }

    /**
     * 전체 포트홀 개수 조회
     */
    fun getTotalPotholeCount(): Long {
        return potholeRepository.count()
    }

    /**
     * 거리와 함께 모든 포트홀 목록 조회
     */
    fun getAllPotholesWithDistance(latitude: Double, longitude: Double): List<PotholeResponse> {
        return getAllPotholes()
            .map { response ->
                response.distance = calculateDistance(latitude, longitude, response.latitude, response.longitude)
                response
            }
            .sortedBy { it.distance }
    }

    /**
     * 두 지점 간의 거리를 계산 (Haversine 공식 사용)
     * @param lat1 첫 번째 지점의 위도
     * @param lon1 첫 번째 지점의 경도
     * @param lat2 두 번째 지점의 위도
     * @param lon2 두 번째 지점의 경도
     * @return 거리 (미터)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // 지구 반지름 (미터)

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLatRad = Math.toRadians(lat2 - lat1)
        val deltaLonRad = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLonRad / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * 특정 포트홀에 연관된 제보들의 모든 이미지 URL을 가져옴
     */
    private fun getImageUrlsForPothole(potholeId: Long): List<String> {
        return reportRepository.findByPotholeId(potholeId)
            .flatMap { it.imageUrls }
    }
}
