package training.goorm.portholemapapi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "naver.geocoding.client-id=test",
    "naver.geocoding.client-secret=test"
])
class PortholemapApiApplicationTests {

    @Test
    fun contextLoads() {
    }

}
