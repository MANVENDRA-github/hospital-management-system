package com.hospital.appointment.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthInterceptor {

    @Bean
    public RequestInterceptor authForwarder() {
        return (RequestTemplate template) -> {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                String header = sra.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (header != null && !header.isBlank()) {
                    template.header(HttpHeaders.AUTHORIZATION, header);
                }
            }
        };
    }
}
