package training.goorm.potholemapapi.exception

import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import training.goorm.potholemapapi.dto.ApiResponse

@RestControllerAdvice
class GlobalExceptionHandler {

    // 다형성을 활용한 generic 예외 처리
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ApiResponse<Unit> {
        return ApiResponse.notFound("리소스를 찾을 수 없습니다")
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ApiResponse<Unit> {
        return ApiResponse.badRequest("입력 데이터가 올바르지 않습니다")
    }

    @ExceptionHandler(BusinessLogicException::class)
    fun handleBusinessLogicException(ex: BusinessLogicException): ApiResponse<Unit> {
        return ApiResponse.badRequest("비즈니스 로직 오류가 발생했습니다")
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ApiResponse<Unit> {
        return ApiResponse.badRequest("요청을 처리할 수 없습니다")
    }

    // 기존 Spring 표준 예외들
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ApiResponse<Unit> {
        return ApiResponse.badRequest("잘못된 요청입니다")
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ApiResponse<Unit> {
        return ApiResponse.notFound("리소스를 찾을 수 없습니다")
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): ApiResponse<Unit> {
        return ApiResponse.notFound("요청한 API를 찾을 수 없습니다")
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ApiResponse<Unit> {
        return ApiResponse.internalServerError("서버 내부 오류가 발생했습니다")
    }
}
