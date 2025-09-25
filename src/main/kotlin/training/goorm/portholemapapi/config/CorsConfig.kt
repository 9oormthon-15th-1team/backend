package training.goorm.portholemapapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(*getAllowedOrigins().toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }

    private fun getAllowedOrigins(): List<String> {
        return listOf( // dev 및 기본값
            "http://localhost*",
            "https://localhost*",
            "http://127.0.0.1*",
            "https://127.0.0.1*"
        )
    }
}
