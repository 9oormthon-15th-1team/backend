package training.goorm.portholemapapi.exception

/**
 * 비즈니스 예외의 기본 클래스
 */
abstract class BusinessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
abstract class ResourceNotFoundException(
    message: String = "리소스를 찾을 수 없습니다"
) : BusinessException(message)

/**
 * 데이터 유효성 검증 실패 시 발생하는 예외
 */
abstract class ValidationException(
    message: String = "데이터 유효성 검증에 실패했습니다"
) : BusinessException(message)

/**
 * 비즈니스 로직 오류 시 발생하는 예외
 */
abstract class BusinessLogicException(
    message: String = "비즈니스 로직 오류가 발생했습니다"
) : BusinessException(message)
