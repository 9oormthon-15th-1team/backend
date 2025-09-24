package training.goorm.portholemapapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
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
                    .description("포트홀 지도 서비스 API")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("9oormthon Team")
                            .email("contact@example.com")
                    )
            )
    }
}
