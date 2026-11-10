package com.magao.backend.common.handler;

import com.magao.backend.common.exception.*;
import com.magao.backend.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 使用@RestControllerAdvice统一捕获和处理所有Controller层抛出的异常，
 * 将异常信息封装为统一的Result响应格式返回给前端。
 * </p>
 * <p>
 * 处理的异常类型：
 * 1. 自定义业务异常（BusinessException等）
 * 2. 参数校验异常（MethodArgumentNotValidException等）
 * 3. Spring Security异常（认证、授权异常）
 * 4. 系统异常（其他未捕获的异常）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证授权异常
     *
     * @param e 认证异常
     * @return 统一错误响应
     */
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthException(AuthException e) {
        log.warn("认证异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理考试异常
     *
     * @param e 考试异常
     * @return 统一错误响应
     */
    @ExceptionHandler(ExamException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleExamException(ExamException e) {
        log.warn("考试异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理支付异常
     *
     * @param e 支付异常
     * @return 统一错误响应
     */
    @ExceptionHandler(PaymentException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handlePaymentException(PaymentException e) {
        log.warn("支付异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid @RequestBody）
     *
     * @param e 方法参数校验异常
     * @return 统一错误响应，包含具体字段校验失败信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", message);
        return Result.fail(400, message);
    }

    /**
     * 处理绑定异常（@Valid 表单提交）
     *
     * @param e 绑定异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败：{}", message);
        return Result.fail(400, message);
    }

    /**
     * 处理约束违反异常（@Validated 方法参数校验）
     *
     * @param e 约束违反异常
     * @return 统一错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束校验失败：{}", message);
        return Result.fail(400, message);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e 缺少请求参数异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数：{}", e.getParameterName());
        return Result.fail(400, "缺少必要参数：" + e.getParameterName());
    }

    /**
     * 处理文件上传大小超限异常
     *
     * @param e 文件上传超限异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件上传超限：{}", e.getMessage());
        return Result.fail(400, "上传文件大小超过限制");
    }

    /**
     * 处理Spring Security认证失败异常
     *
     * @param e 认证失败异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败：{}", e.getMessage());
        return Result.fail(401, "用户名或密码错误");
    }

    /**
     * 处理Spring Security权限不足异常
     *
     * @param e 权限不足异常
     * @return 统一错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足：{}", e.getMessage());
        return Result.fail(403, "权限不足，无法访问该资源");
    }

    /**
     * 处理非法参数异常
     *
     * @param e 非法参数异常
     * @return 统一错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数：{}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e 请求方法不支持异常
     * @return 统一错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：{}", e.getMessage());
        return Result.fail(405, "请求方法不支持：" + e.getMethod());
    }

    /**
     * 处理静态资源未找到异常
     * <p>
     * 当请求的静态资源（如favicon、图片等）不存在时，返回404而非500，
     * 避免污染错误日志。
     * </p>
     *
     * @param e 资源未找到异常
     * @return 统一错误响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        return Result.fail(404, "资源不存在");
    }

    /**
     * 处理所有未捕获的异常（兜底处理）
     *
     * @param e 未知异常
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.fail(500, "系统内部错误，请联系管理员");
    }
}
