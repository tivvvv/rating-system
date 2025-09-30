package com.tiv.rating.system.handler;

import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public BusinessResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BusinessResponse<String> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining());
        return ResultUtils.error(BusinessCodeEnum.PARAMS_ERROR, errorMsg);
    }

    /**
     * 运行时异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public BusinessResponse<?> businessExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(BusinessCodeEnum.SYSTEM_ERROR);
    }

}
