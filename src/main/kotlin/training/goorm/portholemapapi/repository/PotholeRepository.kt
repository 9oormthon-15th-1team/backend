package training.goorm.portholemapapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import training.goorm.portholemapapi.entity.Pothole

@Repository
interface PotholeRepository : JpaRepository<Pothole, Long> {

    /**
     * 특정 위도/경도 범위 내의 포트홀 목록 조회
     */
    @Query("""
        SELECT p FROM Pothole p 
        WHERE p.latitude BETWEEN :minLat AND :maxLat 
        AND p.longitude BETWEEN :minLng AND :maxLng
        ORDER BY p.createdAt DESC
    """)
    fun findByLocationRange(
        @Param("minLat") minLatitude: Double,
        @Param("maxLat") maxLatitude: Double,
        @Param("minLng") minLongitude: Double,
        @Param("maxLng") maxLongitude: Double
    ): List<Pothole>

    /**
     * 설명에 특정 키워드가 포함된 포트홀 검색
     */
    fun findByDescriptionContainingIgnoreCase(keyword: String): List<Pothole>

    /**
     * 이미지가 있는 포트홀만 조회
     */
    fun findByImageUrlIsNotNull(): List<Pothole>

    /**
     * 최근 등록된 포트홀 목록 조회 (제한 개수)
     */
    fun findTop10ByOrderByCreatedAtDesc(): List<Pothole>

    /**
     * 지정된 좌표 주변 특정 반경 내의 포트홀 검색 (Haversine 공식 사용)
     * @param latitude 기준 위도
     * @param longitude 기준 경도
     * @param radiusInMeters 검색 반경 (미터)
     */
    @Query("""
        SELECT p FROM Pothole p
        WHERE 6371000 * acos(
            cos(radians(:latitude)) * cos(radians(p.latitude)) *
            cos(radians(p.longitude) - radians(:longitude)) +
            sin(radians(:latitude)) * sin(radians(p.latitude))
        ) <= :radiusInMeters
        ORDER BY 6371000 * acos(
            cos(radians(:latitude)) * cos(radians(p.latitude)) *
            cos(radians(p.longitude) - radians(:longitude)) +
            sin(radians(:latitude)) * sin(radians(p.latitude))
        )
    """)
    fun findPotholesWithinRadius(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radiusInMeters") radiusInMeters: Double
    ): List<Pothole>
}
