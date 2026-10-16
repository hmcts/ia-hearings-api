package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.exceptions.CcdDataDeserializationException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.IdamApiException;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.idam.IdentityManagerResponseException;

@ExtendWith(MockitoExtension.class)
class HearingsControllerAdviceTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    private HearingsControllerAdvice hearingsControllerAdvice;

    private ListAppender<ILoggingEvent> listAppender;

    private String testExceptionMessage;
    private static final String TEST_PATH = "/test/path";
    private static final String TEST_CORRELATION_ID = "test-correlation-id";

    @BeforeEach
    void setup() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest));

        hearingsControllerAdvice = new HearingsControllerAdvice(errorResponseBuilder);

        Logger controllerAdviceLogger = (Logger) LoggerFactory.getLogger(HearingsControllerAdvice.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        controllerAdviceLogger.addAppender(listAppender);

        testExceptionMessage = "Test exception message!";
    }

    @Test
    void should_handle_required_field_missing_exception() {
        RequiredFieldMissingException ex = new RequiredFieldMissingException(testExceptionMessage);
        String expectedMessage = "Required field is missing: " + testExceptionMessage;
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.REQUIRED_FIELD_MISSING, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.REQUIRED_FIELD_MISSING, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleRequiredFieldMissingException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.REQUIRED_FIELD_MISSING.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.REQUIRED_FIELD_MISSING, httpServletRequest);
    }

    @Test
    void should_handle_illegal_state_exception() {
        IllegalStateException ex = new IllegalStateException(testExceptionMessage);
        String expectedMessage = "Invalid application state: " + testExceptionMessage;
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleIllegalStateException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_illegal_argument_exception() {
        IllegalArgumentException ex = new IllegalArgumentException(testExceptionMessage);
        String expectedMessage = "Invalid argument: " + testExceptionMessage;
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleIllegalArgumentException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_http_message_not_readable_exception() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        String expectedMessage = "Malformed request body";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleHttpMessageNotReadableException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_missing_servlet_request_parameter_exception() {
        MissingServletRequestParameterException ex =
            new MissingServletRequestParameterException("paramName", "String");
        String expectedMessage = "Missing request parameter: paramName";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleMissingServletRequestParameterException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_http_media_type_not_supported_exception() {
        HttpMediaTypeNotSupportedException ex = mock(HttpMediaTypeNotSupportedException.class);
        when(ex.getContentType()).thenReturn(null);
        String expectedMessage = "Unsupported media type: null";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleHttpMediaTypeNotSupportedException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_http_request_method_not_supported_exception() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");
        String expectedMessage = "HTTP method not supported: DELETE";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.BAD_REQUEST, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.BAD_REQUEST, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleHttpRequestMethodNotSupportedException(httpServletRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.BAD_REQUEST, httpServletRequest);
    }

    @Test
    void should_handle_hmc_exception() {
        HmcException ex = new HmcException("Connection timeout");
        String expectedMessage = "HMC service error: " + ex.getMessage();
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.HMC_SERVICE_ERROR, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.HMC_SERVICE_ERROR, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleHmcException(httpServletRequest, ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.HMC_SERVICE_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.HMC_SERVICE_ERROR, httpServletRequest);
    }

    @Test
    void should_handle_idam_api_exception() {
        IdamApiException ex = new IdamApiException("Service unavailable");
        String expectedMessage = "Authentication service unavailable";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.IDAM_SERVICE_ERROR, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.IDAM_SERVICE_ERROR, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleIdamApiException(httpServletRequest, ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.IDAM_SERVICE_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.IDAM_SERVICE_ERROR, httpServletRequest);
    }

    @Test
    void should_handle_identity_manager_exception() {
        IdentityManagerResponseException ex =
            new IdentityManagerResponseException("Invalid token", new RuntimeException());
        String expectedMessage = "Authentication service unavailable";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.IDENTITY_MANAGER_ERROR, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.IDENTITY_MANAGER_ERROR, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleIdentityManagerException(httpServletRequest, ex);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.IDENTITY_MANAGER_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.IDENTITY_MANAGER_ERROR, httpServletRequest);
    }

    @Test
    void should_handle_ccd_deserialization_exception() {
        CcdDataDeserializationException ex =
            new CcdDataDeserializationException("Invalid JSON", new RuntimeException());
        String expectedMessage = "Unable to process case data";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.CCD_DATA_ERROR, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.CCD_DATA_ERROR, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleCcdDeserializationException(httpServletRequest, ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.CCD_DATA_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.CCD_DATA_ERROR, httpServletRequest);
    }

    @Test
    void should_handle_feign_exception() {
        Request feignRequest = Request.create(
            Request.HttpMethod.GET, "/test", Collections.emptyMap(), null, new RequestTemplate());
        FeignException feignException = FeignException.errorStatus("test",
            feign.Response.builder()
                .status(503)
                .reason("Service Unavailable")
                .request(feignRequest)
                .headers(Collections.emptyMap())
                .build());

        String expectedMessage = "External service error occurred";
        ErrorResponse expectedResponse = buildErrorResponse(ErrorCode.EXTERNAL_SERVICE_ERROR, expectedMessage);

        when(errorResponseBuilder.build(ErrorCode.EXTERNAL_SERVICE_ERROR, httpServletRequest, expectedMessage))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleFeignException(httpServletRequest, feignException);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode());

        verify(errorResponseBuilder).logError(feignException, ErrorCode.EXTERNAL_SERVICE_ERROR, httpServletRequest);
    }

    @Test
    void should_handle_access_denied_exception() {
        AccessDeniedException ex = new AccessDeniedException(testExceptionMessage);
        ErrorResponse expectedResponse =
            buildErrorResponse(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage());

        when(errorResponseBuilder.build(ErrorCode.ACCESS_DENIED, httpServletRequest, null))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleAccessDeniedException(httpServletRequest, ex);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.ACCESS_DENIED, httpServletRequest);
    }

    @Test
    void should_handle_all_uncaught_exceptions() {
        RuntimeException ex = new RuntimeException(testExceptionMessage);
        ErrorResponse expectedResponse =
            buildErrorResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());

        when(errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, httpServletRequest, null))
            .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> responseEntity =
            hearingsControllerAdvice.handleAllUncaughtExceptions(httpServletRequest, ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());

        verify(errorResponseBuilder).logError(ex, ErrorCode.INTERNAL_ERROR, httpServletRequest);
    }

    private ErrorResponse buildErrorResponse(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(message)
            .timestamp(Instant.now())
            .requestId(TEST_CORRELATION_ID)
            .path(TEST_PATH)
            .build();
    }
}
