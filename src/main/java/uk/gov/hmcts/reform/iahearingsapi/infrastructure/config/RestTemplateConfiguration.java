package uk.gov.hmcts.reform.iahearingsapi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("removal")
@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestOperations restOperations(
        ObjectMapper objectMapper
    ) {
        return restTemplate(objectMapper);
    }

    @Bean
    public RestTemplate restTemplate(
        ObjectMapper objectMapper
    ) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
            .removeIf(converter -> converter.getSupportedMediaTypes().contains(MediaType.APPLICATION_JSON));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter(objectMapper));

        return restTemplate;
    }

    private org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
        mappingJackson2HttpMessageConverter(
            ObjectMapper objectMapper
    ) {
        return new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);
    }

}
