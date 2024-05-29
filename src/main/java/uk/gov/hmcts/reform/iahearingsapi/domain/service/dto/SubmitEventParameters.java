package uk.gov.hmcts.reform.iahearingsapi.domain.service.dto;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;

import java.time.LocalDateTime;
import java.util.Map;

public record SubmitEventParameters(String userToken,
                                    String s2sToken,
                                    String userId,
                                    String caseId,
                                    Map<String, Object> data,
                                    Event event,
                                    boolean ignoreWarning,
                                    String eventToken,
                                    LocalDateTime lastModified,
                                    String caseType) {
}
