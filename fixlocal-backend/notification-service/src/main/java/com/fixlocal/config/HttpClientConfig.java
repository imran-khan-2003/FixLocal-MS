package com.fixlocal.config;

import com.fixlocal.observability.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     @Value("${http.client.connect-timeout-ms:3000}") long connectTimeoutMs,
                                     @Value("${http.client.read-timeout-ms:5000}") long readTimeoutMs) {
        ClientHttpRequestInterceptor correlationIdInterceptor = (request, body, execution) -> {
            String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            request.getHeaders().set(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
            return execution.execute(request, body);
        };

        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .additionalInterceptors(correlationIdInterceptor)
                .build();
    }
}
