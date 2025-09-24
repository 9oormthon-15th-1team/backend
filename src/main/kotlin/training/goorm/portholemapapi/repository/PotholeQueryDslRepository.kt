package training.goorm.portholemapapi.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import training.goorm.portholemapapi.entity.Pothole
import training.goorm.portholemapapi.entity.QPothole

@Repository
class PotholeQueryDslRepository(
    private val queryFactory: JPAQueryFactory
) {
    private val pothole = QPothole.pothole

    fun findByLocationRange(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double
    ): List<Pothole> {
        return queryFactory
            .selectFrom(pothole)
            .where(
                pothole.latitude.between(minLatitude, maxLatitude)
                    .and(pothole.longitude.between(minLongitude, maxLongitude))
            )
            .fetch()
    }

    fun findByDescriptionContaining(keyword: String): List<Pothole> {
        return queryFactory
            .selectFrom(pothole)
            .where(pothole.description.containsIgnoreCase(keyword))
            .orderBy(pothole.createdAt.desc())
            .fetch()
    }

    fun countByLocationRange(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double
    ): Long {
        return queryFactory
            .select(pothole.count())
            .from(pothole)
            .where(
                pothole.latitude.between(minLatitude, maxLatitude)
                    .and(pothole.longitude.between(minLongitude, maxLongitude))
            )
            .fetchOne() ?: 0L
    }
}
