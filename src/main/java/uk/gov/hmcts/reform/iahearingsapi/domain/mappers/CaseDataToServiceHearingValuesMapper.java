package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS_DESCRIPTION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DATES_TO_AVOID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_ADDITIONAL_ADJUSTMENTS_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_LINKED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_MULTIMEDIA_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_VULNERABILITIES_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_PAPER_J;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.GrantedRefusedType.GRANTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType.EA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType.EU;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType.HU;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DatesToAvoid;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityRangeModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityType;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Service
@RequiredArgsConstructor
public class CaseDataToServiceHearingValuesMapper {

    static final int HEARING_WINDOW_INTERVAL_DEFAULT = 11;
    public static final String REQUIRED_FIELD_FOR_RES_ID_NOT_PRESENT_ERROR =
        "Require either homeOfficeReferenceNumber or gwfReferenceNumber field to be present.";

    private final DateProvider hearingServiceDateProvider;

    public String getCaseManagementLocationCode(AsylumCase asylumCase) {
        if (HearingsUtils.isAppealsLocationRefDataEnabled(asylumCase)) {
            return getCaseManagementLocationEpimmsIdFromRefData(asylumCase);
        } else {
            return getCaseManagementLocationEpimmsId(asylumCase);
        }
    }

    private String getCaseManagementLocationEpimmsIdFromRefData(AsylumCase asylumCase) {
        Optional<CaseManagementLocationRefData> caseManagementLocationOptional = asylumCase
            .read(CASE_MANAGEMENT_LOCATION_REF_DATA, CaseManagementLocationRefData.class);
        if (caseManagementLocationOptional.isPresent()) {
            DynamicList baseLocation = caseManagementLocationOptional.get().getBaseLocation();
            if (baseLocation != null && baseLocation.getValue() != null) {
                return baseLocation.getValue().getCode();
            }
        }

        return null;
    }

    private String getCaseManagementLocationEpimmsId(AsylumCase asylumCase) {
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

        return getHearingChannels(asylumCase, null, null);
    }

    public List<String> getHearingChannels(AsylumCase asylumCase, HearingDetails persistedHearingDetails, Event event) {

        if (HearingsUtils.isDecisionWithoutHearingAppeal(asylumCase)) {
            return List.of(HearingChannel.ONPPRS.name());
        }

        Optional<DynamicList> hearingChannelOptional = asylumCase.read(HEARING_CHANNEL, DynamicList.class);
        if (persistedHearingDetails != null && event != null) {
            switch (event) {
                case RECORD_ADJOURNMENT_DETAILS:
                    hearingChannelOptional = asylumCase.read(NEXT_HEARING_FORMAT, DynamicList.class);
                    break;
                case UPDATE_HEARING_REQUEST:
                    hearingChannelOptional = asylumCase.read(REQUEST_HEARING_CHANNEL, DynamicList.class);
                    if (hearingChannelOptional.isEmpty()) {
                        return persistedHearingDetails.getHearingChannels();
                    }
                    break;
                default:
                    return persistedHearingDetails.getHearingChannels();
            }
        }

        List<String> hearingChannels = new ArrayList<>();
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

    public String getHearingChannel(AsylumCase asylumCase, HearingDetails persistedHearingDetails, Event event) {
        return getHearingChannels(asylumCase, persistedHearingDetails, event).stream().findFirst().orElse(null);
    }

    public Integer getHearingDuration(AsylumCase asylumCase) {
        if (HearingsUtils.isDecisionWithoutHearingAppeal(asylumCase)) {
            AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new RequiredFieldMissingException("AppealType cannot be missing"));
            return Set.of(EA, EU, HU).contains(appealType) ? 60 : 90;
        }

        int hearingDuration = asylumCase.read(LISTING_LENGTH, HoursMinutes.class)
            .map(HoursMinutes::convertToIntegerMinutes)
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

    public String getLegalRepCompany(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_COMPANY, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("legalRepCompany is a required field"));
    }

    public String getInternalCaseLegalRepCompany(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_COMPANY_PAPER_J, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("legalRepCompanyPaperJ is a required field"));
    }

    public String getLegalRepOrganisationIdentifier(AsylumCase asylumCase) {
        Organisation organisation = asylumCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)
            .map(OrganisationPolicy::getOrganisation)
            .orElse(null);

        return (organisation == null || organisation.getOrganisationID() == null)
            ? ""
            : organisation.getOrganisationID();
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

    public String getListingComments(AsylumCase asylumCase) {
        StringBuilder commentsBuilder = new StringBuilder();

        String isVulnerabilitiesAllowed = asylumCase.read(IS_VULNERABILITIES_ALLOWED, String.class)
            .orElse("");
        String isMultimediaAllowed = asylumCase.read(IS_MULTIMEDIA_ALLOWED, String.class)
            .orElse("");
        String isAdditionalAdjustmentsAllowed = asylumCase.read(IS_ADDITIONAL_ADJUSTMENTS_ALLOWED, String.class)
            .orElse("");

        commentsBuilder.append(
            GRANTED.getValue().equals(isVulnerabilitiesAllowed) ? getGrantedHearingResponseFromField(
                asylumCase,
                "Adjustments to accommodate vulnerabilities: ",
                VULNERABILITIES_TRIBUNAL_RESPONSE
            ) : "");

        commentsBuilder.append(
            GRANTED.getValue().equals(isMultimediaAllowed) ? getGrantedHearingResponseFromField(
                asylumCase,
                "Multimedia equipment: ",
                MULTIMEDIA_TRIBUNAL_RESPONSE
            ) : "");

        commentsBuilder.append(
            GRANTED.getValue().equals(isAdditionalAdjustmentsAllowed) ? getGrantedHearingResponseFromField(
                asylumCase,
                "Other adjustments: ",
                ADDITIONAL_TRIBUNAL_RESPONSE
            ) : "");

        asylumCase.read(ADDITIONAL_INSTRUCTIONS_DESCRIPTION, String.class).ifPresent(description -> {
            commentsBuilder.append("Additional instructions: ").append(description).append(";");
        });

        return commentsBuilder.toString();

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

    public Boolean getHearingLinkedFlag(AsylumCase asylumCase) {
        return asylumCase.read(IS_HEARING_LINKED, YesOrNo.class)
            .map(autoList -> YES == autoList)
            .orElse(false);
    }

    public int getIntHearingDurationFromString(AsylumCase asylumCase, AsylumCaseFieldDefinition caseField) {
        return asylumCase.read(caseField, String.class)
            .map(duration -> duration.isBlank() ? 0 : Integer.parseInt(duration))
            .orElse(0);
    }
}
