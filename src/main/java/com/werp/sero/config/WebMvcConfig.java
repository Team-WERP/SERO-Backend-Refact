package com.werp.sero.config;

import com.werp.sero.common.logging.QueryCountInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final QueryCountInterceptor queryCountInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(queryCountInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**");
    }
}
