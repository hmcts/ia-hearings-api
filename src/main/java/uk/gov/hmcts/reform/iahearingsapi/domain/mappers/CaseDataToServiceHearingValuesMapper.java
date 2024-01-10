package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DATES_TO_AVOID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_ADDITIONAL_ADJUSTMENTS_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_MULTIMEDIA_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_VULNERABILITIES_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.GrantedRefusedType.GRANTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DatesToAvoid;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityRangeModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityType;

@Service
@RequiredArgsConstructor
public class CaseDataToServiceHearingValuesMapper {

    static final int HEARING_WINDOW_INTERVAL_DEFAULT = 11;
    public static final String REQUIRED_FIELD_FOR_RES_ID_NOT_PRESENT_ERROR =
        "Require either homeOfficeReferenceNumber or gwfReferenceNumber field to be present.";

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

        if (isDecisionWithoutHearingAppeal(asylumCase)) {
            return List.of(HearingChannel.ONPPRS.name());
        }

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

    public HearingWindowModel getHearingWindowModel(State appealStatus) {
        if (appealStatus == State.LISTING) {
            return calculateAsylumHearingWindow();
        } else {
            return null;
        }
    }

    public HearingWindowModel getHearingWindowModel(boolean isAutoRequest) {
        if (isAutoRequest) {
            return calculateAsylumHearingWindow();
        } else {
            return null;
        }
    }

    private HearingWindowModel calculateAsylumHearingWindow() {
        ZonedDateTime now = hearingServiceDateProvider.zonedNowWithTime();
        String dateRangeStart = hearingServiceDateProvider
            .calculateDueDate(now, HEARING_WINDOW_INTERVAL_DEFAULT)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return HearingWindowModel.builder()
            .dateRangeStart(dateRangeStart)
            .build();
    }

    public LocalDate getCaseSlaStartDate() {
        return hearingServiceDateProvider.now();
    }

    public String getCaseDeepLink(String caseReference) {
        return String.format("/cases/case-details/%s#Overview", caseReference);
    }

    public String getHearingChannel(AsylumCase asylumCase) {
        return getHearingChannels(asylumCase).stream().findFirst().orElse(null);
    }

    public Integer getHearingDuration(AsylumCase asylumCase, Boolean isAdjournmentDetails) {
        if (isDecisionWithoutHearingAppeal(asylumCase)) {
            return null;
        }

        int hearingDuration =
            asylumCase.read(isAdjournmentDetails ? NEXT_HEARING_DURATION : LIST_CASE_HEARING_LENGTH, String.class)
            .map(duration -> duration.isBlank() ? 0 : Integer.parseInt(duration))
            .orElse(0);
        return hearingDuration <= 0 ? null : hearingDuration;
    }

    public String getAppellantPartyId(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_PARTY_ID, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("appellantPartyId is a required field"));
    }

    public String getRespondentPartyId(AsylumCase asylumCase) {
        Optional<String> refNumber = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class);
        if (refNumber.isPresent()) {
            return refNumber.get();
        } else {
            return asylumCase.read(GWF_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException(REQUIRED_FIELD_FOR_RES_ID_NOT_PRESENT_ERROR));
        }
    }

    public String getLegalRepPartyId(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("legalRepIndividualPartyId is a required field"));
    }

    public String getLegalRepOrgPartyId(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("legalRepOrganisationPartyId is a required field"));
    }

    public String getSponsorPartyId(AsylumCase asylumCase) {
        return asylumCase.read(SPONSOR_PARTY_ID, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("sponsorPartyId is a required field"));
    }

    public String getLegalRepCompanyName(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_COMPANY_NAME, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("legalRepCompanyName is a required field"));
    }

    public String getLegalRepOrganisationIdentifier(AsylumCase asylumCase) {
        Organisation organisation = asylumCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)
            .map(OrganisationPolicy::getOrganisation)
            .orElse(null);

        return organisation == null ? "" : defaultIfNull(organisation.getOrganisationID(), "");
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

    public boolean isDecisionWithoutHearingAppeal(AsylumCase asylumCase) {
        return asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(hearingCentre -> DECISION_WITHOUT_HEARING == hearingCentre).orElse(false);
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
