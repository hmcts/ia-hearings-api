package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DATES_TO_AVOID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_ADDITIONAL_ADJUSTMENTS_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_MULTIMEDIA_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_VULNERABILITIES_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.GrantedRefusedType.GRANTED;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DatesToAvoid;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityRangeModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityType;

@Service
@RequiredArgsConstructor
public class CaseDataToServiceHearingValuesMapper {

    static final int HEARING_WINDOW_INTERVAL = 10;

    private final DateProvider hearingServiceDateProvider;

    public String getCaseManagementLocationCode(AsylumCase asylumCase) {
        Optional<CaseManagementLocation> caseManagementLocationOptional = asylumCase
            .read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class);
        if (caseManagementLocationOptional.isPresent()) {
            BaseLocation baseLocation = caseManagementLocationOptional.get().getBaseLocation();
            if (baseLocation != null) {
                return baseLocation.getId();
            }
        }

        return null;
    }

    public List<String> getHearingChannels(AsylumCase asylumCase) {
        List<String> hearingChannels = new ArrayList<>();
        Optional<DynamicList> hearingChannelOptional = asylumCase
            .read(HEARING_CHANNEL, DynamicList.class);
        if (hearingChannelOptional.isPresent()) {
            Value value = hearingChannelOptional.get().getValue();
            if (value != null) {
                hearingChannels.add(value.getCode());
            }
        }

        return hearingChannels;
    }

    public String getExternalCaseReference(AsylumCase asylumCase) {
        return asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseGet(() -> asylumCase.read(GWF_REFERENCE_NUMBER, String.class).orElse(null));
    }

    public HearingWindowModel getHearingWindowModel() {
        ZonedDateTime now = hearingServiceDateProvider.zonedNowWithTime();
        String dateRangeEnd = hearingServiceDateProvider
            .calculateDueDate(now, HEARING_WINDOW_INTERVAL)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return HearingWindowModel.builder()
            .dateRangeStart(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .dateRangeEnd(dateRangeEnd)
            .build();
    }

    public String getCaseSlaStartDate() {
        return hearingServiceDateProvider.now().toString();
    }

    public String getCaseDeepLink(String caseReference) {
        return String.format("/cases/case-details/%s#Overview", caseReference);
    }

    public String getHearingChannel(AsylumCase asylumCase) {
        return asylumCase.read(HEARING_CHANNEL, DynamicList.class)
            .map(dynamicList -> dynamicList.getValue().getCode()).orElse(null);
    }

    // TODO This is a temporary placeholder Replace with the real party ID when implemented.
    public String getPartyId() {
        return UUID.randomUUID().toString();
    }

    public List<UnavailabilityRangeModel> getUnavailabilityRanges(AsylumCase asylumCase) {

        Optional<List<IdValue<DatesToAvoid>>> datesToAvoidOptional = asylumCase.read(DATES_TO_AVOID);

        return datesToAvoidOptional.map(idValues -> idValues.stream()
            .map(IdValue::getValue)
            .filter(dateToAvoid -> dateToAvoid.getDateToAvoid() != null)
            .map(dateToAvoid ->
                     UnavailabilityRangeModel.builder()
                         .unavailableFromDate(dateToAvoid.getDateToAvoid().toString())
                         .unavailableToDate(dateToAvoid.getDateToAvoid().toString())
                         .unavailabilityType(UnavailabilityType.ALL_DAY)
                         .build()).toList()).orElse(Collections.emptyList());

    }

    public String getName(AsylumCase asylumCase, AsylumCaseFieldDefinition nameFieldDefinition) {
        return asylumCase.read(nameFieldDefinition, String.class)
            .orElse(null);
    }

    public List<String> getHearingChannelEmail(
        AsylumCase asylumCase,
        AsylumCaseFieldDefinition emailFieldDefinition) {
        return asylumCase.read(emailFieldDefinition, String.class).map(List::of).orElse(Collections.emptyList());
    }

    public List<String> getHearingChannelPhone(
        AsylumCase asylumCase,
        AsylumCaseFieldDefinition phoneFieldDefinition) {
        return asylumCase.read(phoneFieldDefinition, String.class).map(List::of).orElse(Collections.emptyList());
    }

    public String getRespondentName(AsylumCase asylumCase) {

        if (MapperUtils.isAppellantInUk(asylumCase) || MapperUtils.isS94B(asylumCase)) {
            return "Secretary of State";
        } else {
            return "Entry Clearance Officer";
        }
    }

    public String getListingCommentsFromHearingRequest(AsylumCase asylumCase) {
        StringBuilder responseFromHearingRequest = new StringBuilder();

        String isVulnerabilitiesAllowed = asylumCase.read(IS_VULNERABILITIES_ALLOWED, String.class)
            .orElse("");
        String isMultimediaAllowed = asylumCase.read(IS_MULTIMEDIA_ALLOWED, String.class)
            .orElse("");
        String isAdditionalAdjustmentsAllowed = asylumCase.read(IS_ADDITIONAL_ADJUSTMENTS_ALLOWED, String.class)
            .orElse("");

        responseFromHearingRequest.append(
            GRANTED.getValue().equals(isVulnerabilitiesAllowed) ? getGrantedHearingResponseFromField(
                asylumCase,
                "Adjustments to accommodate vulnerabilities: ",
                VULNERABILITIES_TRIBUNAL_RESPONSE
            ) : "");

        responseFromHearingRequest.append(
            GRANTED.getValue().equals(isMultimediaAllowed) ? getGrantedHearingResponseFromField(
                asylumCase,
                "Multimedia equipment: ",
                MULTIMEDIA_TRIBUNAL_RESPONSE
            ) : "");

        responseFromHearingRequest.append(
            GRANTED.getValue().equals(isAdditionalAdjustmentsAllowed) ? getGrantedHearingResponseFromField(
                asylumCase,
                "Other adjustments: ",
                ADDITIONAL_TRIBUNAL_RESPONSE
            ) : "");

        return responseFromHearingRequest.toString();

    }

    private StringBuilder getGrantedHearingResponseFromField(AsylumCase asylumCase,
                                                      String hearingRequestTitle,
                                                      AsylumCaseFieldDefinition responseField) {
        StringBuilder result = new StringBuilder();

        result.append(hearingRequestTitle);
        result.append(asylumCase.read(responseField, String.class).orElse(""));
        result.append(";");

        return result;
    }
}
