package training.goorm.potholemapapi.dto

enum class ResultCode(val code: String, val message: String) {
    SUCCESS("SUCCESS", "성공"),
    BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다"),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다"),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다"),
    NOT_FOUND("NOT_FOUND", "리소스를 찾을 수 없습니다"),
    CONFLICT("CONFLICT", "리소스 충돌이 발생했습니다"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다"),

    // 비즈니스 로직 관련 에러코드
    VALIDATION_FAILED("VALIDATION_FAILED", "유효성 검증에 실패했습니다"),
    DUPLICATE_DATA("DUPLICATE_DATA", "중복된 데이터입니다"),
    INVALID_PARAMETER("INVALID_PARAMETER", "유효하지 않은 매개변수입니다")
}
