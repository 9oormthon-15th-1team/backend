package training.goorm.portholemapapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    private val environment: Environment
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        val activeProfile = environment.activeProfiles.firstOrNull() ?: "dev"

        registry.addMapping("/**")
            .allowedOriginPatterns(*getAllowedOrigins(activeProfile).toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600) // 1시간 캐시
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        val activeProfile = environment.activeProfiles.firstOrNull() ?: "dev"

        configuration.allowedOriginPatterns = getAllowedOrigins(activeProfile)
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    private fun getAllowedOrigins(profile: String): List<String> {
        return when (profile) {
            "prod" -> listOf(
                "https://yourdomain.com",
                "https://www.yourdomain.com"
            )
            else -> listOf( // dev 및 기본값
                "http://localhost*",
                "https://localhost*",
                "http://127.0.0.1*",
                "https://127.0.0.1*"
            )
        }
    }
}
