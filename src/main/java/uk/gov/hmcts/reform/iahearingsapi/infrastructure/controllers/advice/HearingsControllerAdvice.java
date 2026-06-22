package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.exceptions.CcdDataDeserializationException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.IdamApiException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.idam.IdentityManagerResponseException;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class HearingsControllerAdvice {

    private final ErrorResponseBuilder errorResponseBuilder;

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<ErrorResponse> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException ex
    ) {
        ExceptionUtils.printRootCauseStackTrace(ex);
        errorResponseBuilder.logError(ex, ErrorCode.REQUIRED_FIELD_MISSING, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.REQUIRED_FIELD_MISSING, request, "Required field is missing: " + ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.REQUIRED_FIELD_MISSING.getHttpStatus());
    }

    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalStateException(
        HttpServletRequest request,
        IllegalStateException ex
    ) {
        ExceptionUtils.printRootCauseStackTrace(ex);
        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.BAD_REQUEST, request, "Invalid application state: " + ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        HttpServletRequest request,
        IllegalArgumentException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.BAD_REQUEST, request, "Invalid argument: " + ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        HttpServletRequest request,
        MethodArgumentNotValidException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.VALIDATION_ERROR, request);

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .build())
            .collect(Collectors.toList());

        ErrorResponse response = errorResponseBuilder.buildWithFieldErrors(
            ErrorCode.VALIDATION_ERROR, request, fieldErrors);
        return new ResponseEntity<>(response, ErrorCode.VALIDATION_ERROR.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpServletRequest request,
        HttpMessageNotReadableException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.BAD_REQUEST, request, "Malformed request body");
        return new ResponseEntity<>(response, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
        HttpServletRequest request,
        MissingServletRequestParameterException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.BAD_REQUEST, request, "Missing request parameter: " + ex.getParameterName());
        return new ResponseEntity<>(response, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
        HttpServletRequest request,
        HttpMediaTypeNotSupportedException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.BAD_REQUEST, request, "Unsupported media type: " + ex.getContentType());
        return new ResponseEntity<>(response, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
        HttpServletRequest request,
        HttpRequestMethodNotSupportedException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.BAD_REQUEST, request, "HTTP method not supported: " + ex.getMethod());
        return new ResponseEntity<>(response, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(HmcException.class)
    protected ResponseEntity<ErrorResponse> handleHmcException(
        HttpServletRequest request,
        HmcException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.HMC_SERVICE_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.HMC_SERVICE_ERROR, request, "HMC service error: " + ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.HMC_SERVICE_ERROR.getHttpStatus());
    }

    @ExceptionHandler(IdamApiException.class)
    protected ResponseEntity<ErrorResponse> handleIdamApiException(
        HttpServletRequest request,
        IdamApiException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.IDAM_SERVICE_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.IDAM_SERVICE_ERROR, request, "Authentication service unavailable");
        return new ResponseEntity<>(response, ErrorCode.IDAM_SERVICE_ERROR.getHttpStatus());
    }

    @ExceptionHandler(IdentityManagerResponseException.class)
    protected ResponseEntity<ErrorResponse> handleIdentityManagerException(
        HttpServletRequest request,
        IdentityManagerResponseException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.IDENTITY_MANAGER_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.IDENTITY_MANAGER_ERROR, request, "Authentication service unavailable");
        return new ResponseEntity<>(response, ErrorCode.IDENTITY_MANAGER_ERROR.getHttpStatus());
    }

    @ExceptionHandler(CcdDataDeserializationException.class)
    protected ResponseEntity<ErrorResponse> handleCcdDeserializationException(
        HttpServletRequest request,
        CcdDataDeserializationException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.CCD_DATA_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.CCD_DATA_ERROR, request, "Unable to process case data");
        return new ResponseEntity<>(response, ErrorCode.CCD_DATA_ERROR.getHttpStatus());
    }

    @ExceptionHandler(FeignException.class)
    protected ResponseEntity<ErrorResponse> handleFeignException(
        HttpServletRequest request,
        FeignException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.EXTERNAL_SERVICE_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.EXTERNAL_SERVICE_ERROR, request, "External service error occurred");
        return new ResponseEntity<>(response, ErrorCode.EXTERNAL_SERVICE_ERROR.getHttpStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(
        HttpServletRequest request,
        AccessDeniedException ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.ACCESS_DENIED, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.ACCESS_DENIED, request, null);
        return new ResponseEntity<>(response, ErrorCode.ACCESS_DENIED.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(
        HttpServletRequest request,
        Exception ex
    ) {
        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_ERROR.getHttpStatus());
    }
}
