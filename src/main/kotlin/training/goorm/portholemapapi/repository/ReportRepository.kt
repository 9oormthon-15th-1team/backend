package training.goorm.portholemapapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import training.goorm.portholemapapi.entity.Report

@Repository
interface ReportRepository : JpaRepository<Report, Long> {

    /**
     * 특정 위도/경도 범위 내의 제보 목록 조회
     */
    @Query("""
        SELECT r FROM Report r 
        WHERE r.latitude BETWEEN :minLat AND :maxLat 
        AND r.longitude BETWEEN :minLng AND :maxLng
        ORDER BY r.createdAt DESC
    """)
    fun findByLocationRange(
        @Param("minLat") minLatitude: Double,
        @Param("maxLat") maxLatitude: Double,
        @Param("minLng") minLongitude: Double,
        @Param("maxLng") maxLongitude: Double
    ): List<Report>

    /**
     * 주소에 특정 키워드가 포함된 제보 검색
     */
    fun findByAddressContainingIgnoreCase(keyword: String): List<Report>

    /**
     * 설명에 특정 키워드가 포함된 제보 검색
     */
    fun findByDescriptionContainingIgnoreCase(keyword: String): List<Report>

    /**
     * 최근 등록된 제보 목록 조회 (제한 개수)
     */
    fun findTop10ByOrderByCreatedAtDesc(): List<Report>

    /**
     * 생성일 기준 정렬된 전체 제보 목록
     */
    fun findAllByOrderByCreatedAtDesc(): List<Report>
}
