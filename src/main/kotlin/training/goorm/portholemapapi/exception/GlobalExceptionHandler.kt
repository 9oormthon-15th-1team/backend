package training.goorm.portholemapapi.exception

import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import training.goorm.portholemapapi.dto.ApiResponse

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ApiResponse<Nothing> {
        return ApiResponse.badRequest(ex.message ?: "잘못된 요청입니다")
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ApiResponse<Nothing> {
        return ApiResponse.notFound(ex.message ?: "리소스를 찾을 수 없습니다")
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): ApiResponse<Nothing> {
        return ApiResponse.notFound("요청한 API를 찾을 수 없습니다")
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ApiResponse<Nothing> {
        return ApiResponse.internalServerError("서버 내부 오류가 발생했습니다")
    }
}
