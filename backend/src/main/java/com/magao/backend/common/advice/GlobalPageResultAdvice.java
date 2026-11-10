package com.magao.backend.common.advice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.magao.backend.common.result.PageResult;
import com.magao.backend.common.result.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局分页响应归一切面。
 * <p>
 * 将 Controller 直接返回的 MyBatis-Plus {@link IPage} 统一包装为 {@link PageResult}
 * （含 list 字段），对齐前端 {@code PageResult{list,total,pageNum,pageSize}} 契约。
 * 仅对 data 为 IPage 的 {@link Result} 生效，其余响应原样返回，不影响文件流/字符串等。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@ControllerAdvice
public class GlobalPageResultAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result) {
            Result<Object> result = (Result<Object>) body;
            Object data = result.getData();
            if (data instanceof IPage) {
                result.setData(PageResult.from((IPage<Object>) data));
            }
        }
        return body;
    }
}
