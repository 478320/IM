package com.huayu.controller;

import com.huayu.dto.Result;
import com.huayu.exception.BusinessException;
import com.huayu.exception.SystemException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理器对象
 */
@RestControllerAdvice
public class ProjectExceptionAdvice {

    /**
     * 处理系统异常的方法
     *
     * @param ex 出现的异常
     */
    @ExceptionHandler(SystemException.class)
    public Result doSystemException(SystemException ex) {

        return Result.fail(ex.getMessage());
    }

    /**
     * 处理用户造成的业务异常的方法
     *
     * @param ex 出现的异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result doBusinessException(BusinessException ex) {

        return Result.fail(ex.getMessage());
    }

    /**
     * 处理未预料到的异常的方法
     *
     * @param ex 出现的异常
     */
    @ExceptionHandler(Exception.class)
    public Result doException(Exception ex) throws Exception {
        if (ex instanceof AuthenticationException) {
            throw ex;
        }
        return Result.fail( "系统繁忙请稍后再试");
    }
}
