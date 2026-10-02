package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.yaml.MappingJackson2YamlHttpMessageConverter;

@Configuration
@SuppressWarnings("removal")
public class FeignConfiguration {
    @Bean
    public HttpMessageConverterCustomizer feignJacksonConverterCustomizer(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper feignObjectMapper = feignObjectMapper(builder);
        return converters -> {
            converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter
                || converter instanceof MappingJackson2YamlHttpMessageConverter);

            int index = 0;
            for (int i = 0; i < converters.size(); i++) {
                if (converters.get(i) instanceof ByteArrayHttpMessageConverter
                    || converters.get(i) instanceof StringHttpMessageConverter) {
                    index = i + 1;
                }
            }
            converters.add(index, new MappingJackson2HttpMessageConverter(feignObjectMapper));
        };
    }

    @Bean
    public ClientHttpMessageConvertersCustomizer jacksonClientCustomizer(Jackson2ObjectMapperBuilder builder) {
        return converters -> converters.withJsonConverter(
            new MappingJackson2HttpMessageConverter(feignObjectMapper(builder)));
    }

    private static ObjectMapper feignObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToEnable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .modulesToInstall(new Jdk8Module(), new JavaTimeModule())
            .build();
    }
}
