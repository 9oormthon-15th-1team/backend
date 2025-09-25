package training.goorm.portholemapapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Pothole Map API")
                    .version("v1.0.0")
                    .description("포트홀 지도 서비스 API - 도로 위의 포트홀 정보를 관리하는 RESTful API")
            )
    }
}
