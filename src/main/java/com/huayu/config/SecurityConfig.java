package com.huayu.config;

import com.huayu.filter.JwtAuthenticationTokenFilter;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

/**
 * springSecurity配置类
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Resource
    private AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    /**
     * 安全配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        //拦截设置
        http
                .authorizeRequests()
                //公开以下urls
                .requestMatchers("/user/login").permitAll()
                .requestMatchers("/user/register").permitAll()
                .requestMatchers("/user/{id}").permitAll()
                .requestMatchers("/blog/new/**").permitAll()
                .requestMatchers("/blog/hot").permitAll()
                .requestMatchers("/blog-comments/{id}").permitAll()
                .requestMatchers("/blog/{id}").permitAll()
                .requestMatchers("/groups/save").permitAll()
                //其他路径必须验证
                .anyRequest().authenticated();

        //异常处理
        http.exceptionHandling(exception  -> {
            exception.authenticationEntryPoint(authenticationEntryPoint);//请求未认证的接口
        });

        //关闭session
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // 关闭cors
        http.cors((cors) ->{
            cors.configurationSource(corsConfigSource());
        });
        // 关闭csrf
        http.csrf((csrf) -> {
            csrf.disable();
        });
        return http.build();
    }

    /**
     * 跨域问题解决过滤器
     */
    private CorsConfigurationSource corsConfigSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 取消ROLE_前缀
     */
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // Remove the ROLE_ prefix
        return new GrantedAuthorityDefaults("");
    }

    /**
     * 设置密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 设置中文配置
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:org/springframework/security/messages_zh_CN");
        return messageSource;
    }

    /**
     * 认证管理器，登录的时候参数会传给 authenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }



}
