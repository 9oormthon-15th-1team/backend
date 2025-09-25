package training.goorm.portholemapapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import training.goorm.portholemapapi.entity.Pothole
import training.goorm.portholemapapi.repository.PotholeRepository
import java.time.LocalDateTime

@Component
class PotholeDataInitializer(
    private val potholeRepository: PotholeRepository,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(PotholeDataInitializer::class.java)

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun initializePotholeData() {
        logger.info("=== Pothole 데이터 초기화 시작 ===")

        try {
            // 현재 데이터 개수 확인
            val currentCount = potholeRepository.count()
            logger.info("현재 Pothole 데이터 개수: $currentCount")

            // 이미 데이터가 있으면 초기화하지 않음
            if (currentCount > 0) {
                logger.info("Pothole 데이터가 이미 존재합니다. 초기화를 건너뜁니다.")
                return
            }

            logger.info("pothole_data.json 파일에서 초기 데이터를 로드합니다...")

            // ClassPath에서 pothole_data.json 파일 읽기
            val resource = ClassPathResource("data/pothole_data.json")

            if (!resource.exists()) {
                logger.error("pothole_data.json 파일을 찾을 수 없습니다!")
                return
            }

            logger.info("pothole_data.json 파일을 찾았습니다: ${resource.uri}")

            val potholeDataList: List<PotholeInitData> = objectMapper.readValue(resource.inputStream)
            logger.info("JSON에서 ${potholeDataList.size}개의 포트홀 데이터를 읽었습니다.")

            // Pothole 엔티티로 변환하여 저장
            val potholes = potholeDataList.mapIndexed { index, data ->
                logger.debug("변환 중: ${index + 1}번째 포트홀 - 위치=(${data.latitude}, ${data.longitude})")
                Pothole(
                    latitude = data.latitude,
                    longitude = data.longitude,
                    description = data.description,
                    imageUrl = data.imageUrl,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            }

            val savedPotholes = potholeRepository.saveAll(potholes)
            logger.info("✅ ${savedPotholes.size}개의 Pothole 초기 데이터가 성공적으로 저장되었습니다.")

            // 저장된 데이터 확인
            savedPotholes.forEach { pothole ->
                logger.info("저장된 Pothole: ID=${pothole.id}, 위치=(${pothole.latitude}, ${pothole.longitude}), 설명=${pothole.description}")
            }

            logger.info("=== Pothole 데이터 초기화 완료 ===")

        } catch (ex: Exception) {
            logger.error("❌ Pothole 초기 데이터 로드 중 오류가 발생했습니다: ${ex.message}", ex)
        }
    }

    /**
     * JSON 파일에서 읽어올 데이터 구조
     */
    data class PotholeInitData(
        val latitude: Double,
        val longitude: Double,
        val description: String,
        val imageUrl: String?
    )
}
