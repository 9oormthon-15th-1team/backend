package training.goorm.portholemapapi.exception

/**
 * 포트홀을 찾을 수 없을 때 발생하는 예외
 */
class PotholeNotFoundException(
    potholeId: Long? = null
) : ResourceNotFoundException(
    if (potholeId != null) "ID $potholeId 에 해당하는 포트홀을 찾을 수 없습니다"
    else "포트홀을 찾을 수 없습니다"
)

/**
 * 포트홀 관련 비즈니스 로직 예외
 */
class PotholeBusinessException(
    message: String = "포트홀 비즈니스 로직 오류가 발생했습니다"
) : BusinessLogicException(message)

/**
 * 포트홀 데이터 유효성 검증 예외
 */
class PotholeValidationException(
    message: String = "포트홀 데이터 유효성 검증에 실패했습니다",
    val field: String? = null
) : ValidationException(message)
