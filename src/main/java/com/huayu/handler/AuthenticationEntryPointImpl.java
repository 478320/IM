package com.huayu.handler;

import com.alibaba.fastjson.JSON;
import com.huayu.dto.Result;
import com.huayu.utils.WebUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证失败处理器
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    /**
     * 处理认证失败时的请求以json格式返回前端
     *
     * @param request       请求体
     * @param response      响应体
     * @param authException 认证失败异常
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Result result = Result.fail("用户认证失败请重新登录");
        String json = JSON.toJSONString(result);
        //处理异常
        WebUtils.renderString(response, json);
    }
}
