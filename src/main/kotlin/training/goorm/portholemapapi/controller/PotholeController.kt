package training.goorm.portholemapapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Potholes", description = "포트홀 관련 API")
@RestController
@RequestMapping("/api/potholes")
class PortholeController {

    @Operation(summary = "포트홀 목록 조회", description = "등록된 포트홀 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "500", description = "서버 오류")
        ]
    )
    @GetMapping
    fun getPotholes(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(listOf("Pothole 1", "Pothole 2", "Pothole 3"))
    }

    @Operation(summary = "포트홀 상세 조회", description = "특정 포트홀의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 ID"),
            ApiResponse(responseCode = "404", description = "포트홀을 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류")
        ]
    )
    @GetMapping("/{id}")
    fun getPothole(@PathVariable id: Long): ResponseEntity<String> {
        return ResponseEntity.ok("Pothole $id details")
    }

    @Operation(summary = "포트홀 등록", description = "새로운 포트홀을 등록합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "500", description = "서버 오류")
        ]
    )
    @PostMapping
    fun createPothole(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        return ResponseEntity.ok("Pothole created successfully")
    }
}
