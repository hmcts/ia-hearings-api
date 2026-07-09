package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation failed"),
    REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING", HttpStatus.BAD_REQUEST, "Required field is missing"),
    HMC_SERVICE_ERROR("HMC_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "HMC service error"),
    IDAM_SERVICE_ERROR("IDAM_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "IDAM service error"),
    IDENTITY_MANAGER_ERROR("IDENTITY_MANAGER_ERROR", HttpStatus.UNAUTHORIZED, "Identity manager error"),
    CCD_DATA_ERROR("CCD_DATA_ERROR", HttpStatus.UNPROCESSABLE_ENTITY, "CCD data processing error"),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "External service error"),
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "Access denied"),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Unauthorized access");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
