package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HearingsControllerAdviceTest {

    @Mock
    HttpServletRequest request;
    private HearingsControllerAdvice hearingsControllerAdvice;

    @BeforeEach
    public void setUp() {
        hearingsControllerAdvice = new HearingsControllerAdvice();
    }

    @Test
    void should_handle_required_missing_field_exception() {

        ResponseEntity<String> responseEntity = hearingsControllerAdvice
            .handleRequiredFieldMissingException(request, new RequiredFieldMissingException(
                "HMCTS internal case name is a required field"));

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertEquals(responseEntity.getBody(), "{\"error\": \"HMCTS internal case name is a required field\"}");
    }

    @Test
    void should_handle_illegal_argument_exception() {

        ResponseEntity<String> responseEntity = hearingsControllerAdvice
            .handleExceptions(request, new IllegalArgumentException("HMCTS internal case name is a required field"));

        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertEquals(responseEntity.getBody(), "{\"error\": \"HMCTS internal case name is a required field\"}");
    }
}
