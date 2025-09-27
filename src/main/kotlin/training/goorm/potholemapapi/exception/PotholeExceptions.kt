package training.goorm.potholemapapi.exception

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

/**
 * 포트홀 제보를 찾을 수 없을 때 발생하는 예외
 */
class ReportNotFoundException(
    reportId: Long? = null
) : ResourceNotFoundException(
    if (reportId != null) "ID $reportId 에 해당하는 제보를 찾을 수 없습니다"
    else "제보를 찾을 수 없습니다"
)

/**
 * 포트홀 제보 관련 비즈니스 로직 예외
 */
class ReportBusinessException(
    message: String = "제보 비즈니스 로직 오류가 발생했습니다"
) : BusinessLogicException(message)

/**
 * 포트홀 제보 데이터 유효성 검증 예외
 */
class ReportValidationException(
    message: String = "제보 데이터 유효성 검증에 실패했습니다",
    val field: String? = null
) : ValidationException(message)
