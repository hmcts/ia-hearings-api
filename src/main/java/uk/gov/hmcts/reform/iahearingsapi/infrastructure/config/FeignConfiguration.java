package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import tools.jackson.databind.cfg.EnumFeature;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
@Configuration
public class FeignConfiguration {

    @Bean
    @Primary
    public Encoder feignFormEncoder(
        ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters
    ) {
        return new SpringFormEncoder(new SpringEncoder(feignHttpMessageConverters));
    }

    @SuppressWarnings("deprecation")
    @Bean
    public Decoder decoder(ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters) {
        return new ResponseEntityDecoder(new SpringDecoder(feignHttpMessageConverters));

    }

    @Bean
    public HttpMessageConverter<?> feignJacksonHttpMessageConverter (JsonMapper jsonMapper){
        return new JacksonJsonHttpMessageConverter(jsonMapper);
    }

    public JsonMapper objectMapper(JsonMapper.Builder builder){
        return builder
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            .build();
    }
}
