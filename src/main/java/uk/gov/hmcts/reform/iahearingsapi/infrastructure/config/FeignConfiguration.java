package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@SuppressWarnings("removal")
public class FeignConfiguration {

    //    needed to allow context encoder to fetch the beans of feign clients
    @Bean
    public FeignHttpMessageConverters feignHttpMessageConverters(
        ObjectProvider<ClientHttpMessageConvertersCustomizer> customizers,
        ObjectProvider<HttpMessageConverterCustomizer> cloudCustomizers
    ) {
        return new FeignHttpMessageConverters(customizers, cloudCustomizers);

    }

    @Bean
    @Primary
    public Encoder feignFormEncoder(
        ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters
    ) {
        return new SpringFormEncoder(new SpringEncoder(feignHttpMessageConverters));
    }

    @SuppressWarnings({"deprecation", "removal"})
    @Bean
    public Decoder decoder() {
        HttpMessageConverter<?> jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper());

        return new ResponseEntityDecoder(new SpringDecoder(singleConverter(jacksonConverter)));

    }

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private static ObjectProvider<FeignHttpMessageConverters> singleConverter(HttpMessageConverter<?> converter) {
        FeignHttpMessageConverters converters =
            new FeignHttpMessageConverters(
                new ObjectProvider<>() {
                }, new ObjectProvider<>() {
                }
            ) {
                @Override
                public List<HttpMessageConverter<?>> getConverters() {
                    return List.of(converter);

                }
            };
        return new ObjectProvider<>() {
            @Override
            public FeignHttpMessageConverters getObject() {
                return converters;
            }
        };
    }
}
