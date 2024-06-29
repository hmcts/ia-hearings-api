package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;

public enum ServiceDataFieldDefinition {

    CASE_REF("caseRef", new TypeReference<String>() {}),
    HMCTS_SERVICE_CODE("hmctsServiceCode", new TypeReference<String>() {}),
    HEARING_ID("hearingID", new TypeReference<String>() {}),
    NEXT_HEARING_DATE("nextHearingDate", new TypeReference<LocalDateTime>() {}),
    HEARING_VENUE_ID("hearingVenueId", new TypeReference<String>() {}),
    HEARING_CHANNELS("hearingChannels", new TypeReference<List<HearingChannel>>() {}),
    HEARING_TYPE("hearingType", new TypeReference<String>() {}),
    HEARING_LISTING_STATUS("hearingListingStatus", new TypeReference<ListingStatus>() {}),
    LIST_ASSIST_CASE_STATUS("ListAssistCaseStatus", new TypeReference<ListAssistCaseStatus>() {}),
    HMC_STATUS("HMCStatus", new TypeReference<HmcStatus>() {}),
    DURATION("duration", new TypeReference<Integer>() {}),
    HEARING_REQUEST_VERSION_NUMBER("hearingRequestVesionNumber", new TypeReference<Long>() {}),
    HEARING_GET_RESPONSE("hearingGetResponse", new TypeReference<HearingGetResponse>() {}),
    HEARING_RESPONSE_RECEIVED_DATE_TIME("hearingResponseReceivedDateTime", new TypeReference<LocalDateTime>() {}),
    CASE_CATEGORY("caseCategory", new TypeReference<List<CaseCategoryModel>>() {});

    private final String value;
    private final TypeReference<?> typeReference;

    ServiceDataFieldDefinition(String value, TypeReference<?> typeReference) {
        this.value = value;
        this.typeReference = typeReference;
    }

    public String value() {
        return value;
    }

    public TypeReference<?> getTypeReference() {
        return typeReference;
    }
}
