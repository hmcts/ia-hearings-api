package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class HearingsControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<String> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException e
    ) {
        ExceptionUtils.printRootCauseStackTrace(e);
        String errorMessage = String.format("{\"error\": \"%s\"}", e.getMessage());
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<String> handleExceptions(
        HttpServletRequest request,
        Exception ex
    ) {
        ExceptionUtils.printRootCauseStackTrace(ex);
        String errorMessage = String.format("{\"error\": \"%s\"}", ex.getMessage());
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

}
