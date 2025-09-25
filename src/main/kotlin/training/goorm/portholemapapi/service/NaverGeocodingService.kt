package training.goorm.portholemapapi.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class NaverGeocodingService(
    @Value("\${naver.geocoding.client-id}") private val clientId: String,
    @Value("\${naver.geocoding.client-secret}") private val clientSecret: String,
    @Value("\${naver.geocoding.url}") private val geocodingUrl: String,
    private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(NaverGeocodingService::class.java)

    fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return try {
            val headers = HttpHeaders().apply {
                set("X-NCP-APIGW-API-KEY-ID", clientId)
                set("X-NCP-APIGW-API-KEY", clientSecret)
            }

            val url = "$geocodingUrl?coords=$longitude,$latitude&output=json&orders=roadaddr,addr"
            val entity = HttpEntity<String>(headers)

            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NaverGeocodingResponse::class.java
            )

            response.body?.let { geocodingResponse ->
                if (geocodingResponse.status.code == 0 && geocodingResponse.results.isNotEmpty()) {
                    val result = geocodingResponse.results[0]
                    result.region.area1.name + " " +
                    result.region.area2.name + " " +
                    result.region.area3.name + " " +
                    result.land.addition0.value
                } else {
                    logger.warn("네이버 지오코딩 API 응답 오류: ${geocodingResponse.status}")
                    "위도: $latitude, 경도: $longitude"
                }
            } ?: run {
                logger.warn("네이버 지오코딩 API 응답이 null입니다")
                "위도: $latitude, 경도: $longitude"
            }
        } catch (e: Exception) {
            logger.error("네이버 지오코딩 API 호출 중 오류 발생", e)
            "위도: $latitude, 경도: $longitude"
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class NaverGeocodingResponse(
    val status: Status,
    val results: List<Result>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Status(
    val code: Int,
    val name: String,
    val message: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Result(
    val name: String,
    val code: Code,
    val region: Region,
    val land: Land
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Code(
    val id: String,
    val type: String,
    val mappingId: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Region(
    val area0: Area,
    val area1: Area,
    val area2: Area,
    val area3: Area,
    val area4: Area
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Land(
    val type: String,
    val number1: String,
    val number2: String,
    val addition0: Addition,
    val addition1: Addition,
    val addition2: Addition,
    val addition3: Addition,
    val addition4: Addition
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Area(
    val name: String,
    val coords: Coords
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Addition(
    val type: String,
    val value: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Coords(
    val center: Center
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Center(
    val crs: String,
    val x: Double,
    val y: Double
)