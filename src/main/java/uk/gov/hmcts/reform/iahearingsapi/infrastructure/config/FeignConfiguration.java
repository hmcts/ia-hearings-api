package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.exceptions.FeignClientErrorDecoder;

@Configuration
public class FeignConfiguration {

    private final AppInsightsService appInsightsService;

    public FeignConfiguration(AppInsightsService appInsightsService) {
        this.appInsightsService = appInsightsService;
    }

    @Bean
    @Primary
    Decoder feignDecoder(ObjectMapper objectMapper) {
        return new JacksonDecoder(objectMapper);
    }

    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        return new FeignClientErrorDecoder(appInsightsService, objectMapper);
    }

    @Bean
    public Retryer retryer(@Value("${feign.client.retryer.period}") long period,
                           @Value("${feign.client.retryer.maxPeriod}") long maxPeriod,
                           @Value("${feign.client.retryer.maxAttempts}") int maxAttempts) {
        return new Retryer.Default(period, maxPeriod, maxAttempts);
    }

}
