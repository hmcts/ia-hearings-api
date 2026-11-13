package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.cfg.EnumFeature;

@Configuration
public class FeignConfiguration {

    @Bean
    public FeignHttpMessageConverters feignHttpMessageConverters(
        ObjectProvider<ClientHttpMessageConvertersCustomizer> messageConverters,
        ObjectProvider<HttpMessageConverterCustomizer> customizers
    ) {
        return new FeignHttpMessageConverters(messageConverters, customizers);
    }

    @Bean
    @Primary
    public Encoder feignFormEncoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
            .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            .build();
    }
}
