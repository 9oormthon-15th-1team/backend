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

@Service
@Transactional(readOnly = true)
class PotholeService(
    private val potholeRepository: PotholeRepository
) {

    /**
     * 모든 포트홀 목록 조회
     */
    fun getAllPotholes(): List<PotholeResponse> {
        return potholeRepository.findAll()
            .map { PotholeResponse.from(it) }
    }

    /**
     * 포트홀 상세 정보 조회
     */
    fun getPotholeById(id: Long): PotholeResponse {
        val pothole = potholeRepository.findByIdOrNull(id)
            ?: throw PotholeNotFoundException(id)
        return PotholeResponse.from(pothole)
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
            .map { PotholeResponse.from(it) }
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
            .map { PotholeResponse.from(it) }
    }

    /**
     * 키워드로 포트홀 검색
     */
    fun searchPotholesByKeyword(keyword: String): List<PotholeResponse> {
        return potholeRepository.findByDescriptionContainingIgnoreCase(keyword)
            .map { PotholeResponse.from(it) }
    }

    /**
     * 이미지가 있는 포트홀만 조회
     */
    fun getPotholesWithImage(): List<PotholeResponse> {
        return potholeRepository.findByImageUrlIsNotNull()
            .map { PotholeResponse.from(it) }
    }

    /**
     * 최근 등록된 포트홀 10개 조회
     */
    fun getRecentPotholes(): List<PotholeResponse> {
        return potholeRepository.findTop10ByOrderByCreatedAtDesc()
            .map { PotholeResponse.from(it) }
    }

    /**
     * 포트홀 생성
     */
    @Transactional
    fun createPothole(request: CreatePotholeRequest): PotholeResponse {
        val pothole = Pothole(
            latitude = request.latitude,
            longitude = request.longitude,
            description = request.description,
            imageUrl = request.imageUrl,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedPothole = potholeRepository.save(pothole)
        return PotholeResponse.from(savedPothole)
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
            createdAt = existingPothole.createdAt,
            updatedAt = LocalDateTime.now()
        )

        val savedPothole = potholeRepository.save(updatedPothole)
        return PotholeResponse.from(savedPothole)
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
}
