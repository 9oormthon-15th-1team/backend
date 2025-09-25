package training.goorm.portholemapapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "API 공통 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    @Schema(description = "응답 코드", example = "SUCCESS")
    val code: ResultCode,

    @Schema(description = "응답 메시지", example = "성공")
    val message: String,

    @Schema(description = "응답 데이터")
    val data: T? = null,
) {
    companion object {
        // 데이터만 있는 성공 응답 (기본 메시지 "성공" 사용)
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(
                code = ResultCode.SUCCESS,
                message = ResultCode.SUCCESS.message,
                data = data
            )
        }

        // 메시지만 있는 성공 응답 (data는 null)
        fun success(message: String = ResultCode.SUCCESS.message): ApiResponse<Nothing> {
            return ApiResponse(
                code = ResultCode.SUCCESS,
                message = message,
                data = null
            )
        }

        // 데이터와 메시지가 모두 있는 성공 응답
        fun <T> success(data: T, message: String): ApiResponse<T> {
            return ApiResponse(
                code = ResultCode.SUCCESS,
                message = message,
                data = data
            )
        }

        fun <T> failure(resultCode: ResultCode, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                code = resultCode,
                message = message ?: resultCode.message,
                data = null
            )
        }

        fun <T> badRequest(message: String? = null): ApiResponse<T> {
            return failure(ResultCode.BAD_REQUEST, message)
        }

        fun <T> notFound(message: String? = null): ApiResponse<T> {
            return failure(ResultCode.NOT_FOUND, message)
        }

        fun <T> internalServerError(message: String? = null): ApiResponse<T> {
            return failure(ResultCode.INTERNAL_SERVER_ERROR, message)
        }

        fun <T> unauthorized(message: String? = null): ApiResponse<T> {
            return failure(ResultCode.UNAUTHORIZED, message)
        }

        fun <T> forbidden(message: String? = null): ApiResponse<T> {
            return failure(ResultCode.FORBIDDEN, message)
        }

        fun <T> validationFailed(message: String? = null): ApiResponse<T> {
            return failure(ResultCode.VALIDATION_FAILED, message)
        }
    }
}
