package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.DocumentTag.BAIL_SUBMISSION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;

@Service
@RequiredArgsConstructor
public class BailCaseDataToServiceHearingValuesMapper {
    static final int HEARING_START_WINDOW_INTERVAL_DEFAULT = 3;
    static final int HEARING_WINDOW_END_INTERVAL_DEFAULT = 8;
    static final int HEARING_START_WINDOW_INTERVAL_CONDITIONAL_BAIL = 29;
    static final String BAIL_STATE_DECISION_CONDITIONAL_BAIL = "decisionConditionalBail";

    private final DateProvider hearingServiceDateProvider;

    public String getExternalCaseReference(BailCase bailCase) {
        return bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(null);
    }

    public List<String> getHearingChannels(BailCase bailCase) {
        if (bailCase.read(VIDEO_HEARING1, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return List.of(VID.name());
        }
        return Collections.emptyList();
    }

    public String getListingComments(BailCase bailCase) {
        if (bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return bailCase.read(APPLICANT_DISABILITY_DETAILS, String.class).orElse("");
        }
        return "";
    }

    public HearingWindowModel getHearingWindowModel(String bailState) {
        ZonedDateTime now = hearingServiceDateProvider.zonedNowWithTime();

        if (bailState.equals(BAIL_STATE_DECISION_CONDITIONAL_BAIL)) {
            return HearingWindowModel.builder()
                .firstDateTimeMustBe(hearingServiceDateProvider
                                    .calculateDueDate(now, HEARING_START_WINDOW_INTERVAL_CONDITIONAL_BAIL)
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
        } else {
            return HearingWindowModel.builder()
                .dateRangeStart(hearingServiceDateProvider
                                    .calculateDueDate(now, HEARING_START_WINDOW_INTERVAL_DEFAULT)
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .dateRangeEnd(hearingServiceDateProvider
                                  .calculateDueDate(now, HEARING_WINDOW_END_INTERVAL_DEFAULT)
                                  .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
        }

    }

    public String getCaseSlaStartDate(BailCase bailCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalNotificationLetters =
            bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA);
        return optionalNotificationLetters
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(d -> d.getTag() == BAIL_SUBMISSION)
            .findFirst().orElseThrow(() -> new RequiredFieldMissingException(
                BAIL_SUBMISSION + " document not available"))
            .getDateUploaded();
    }

    public String getApplicantPartyId(BailCase bailCase) {
        return bailCase.read(APPLICANT_PARTY_ID, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("applicantPartyId is a required field"));
    }

    public String getStringValueByDefinition(BailCase bailCase, BailCaseFieldDefinition nameFieldDefinition) {
        return bailCase.read(nameFieldDefinition, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException(nameFieldDefinition.value() + " is a required field"));
    }

    public String getHearingChannel(BailCase bailCase) {
        return getHearingChannels(bailCase).stream().findFirst().orElse(null);
    }

    public String getLegalRepPartyId(BailCase bailCase) {
        return bailCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("legalRepIndividualPartyId is a required field"));
    }

    public String getLegalRepOrgPartyId(BailCase bailCase) {
        return bailCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("legalRepOrganisationPartyId is a required field"));
    }

    public String getLegalRepCompanyName(BailCase bailCase) {
        return bailCase.read(LEGAL_REP_COMPANY, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("legalRepCompany is a required field"));
    }

    public String getRespondentPartyId(BailCase bailCase) {
        return bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeReferenceNumber is a required field"));
    }

    public List<String> getHearingChannelEmailPhone(
        BailCase bailCase,
        BailCaseFieldDefinition emailFieldDefinition) {
        return bailCase.read(emailFieldDefinition, String.class).map(List::of).orElse(Collections.emptyList());
    }

    public String getLegalRepOrganisationIdentifier(BailCase bailCase) {
        Organisation organisation = bailCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)
            .map(OrganisationPolicy::getOrganisation)
            .orElse(null);

        return organisation == null ? "" : defaultIfNull(organisation.getOrganisationID(), "");
    }

    public String getCaseManagementLocationCode(BailCase bailCase) {
        //CaseManagemnetLocationCode is set based on Hearing Centre Location
        Optional<HearingCentre> hearingCentreOptional = bailCase
            .read(HEARING_CENTRE, HearingCentre.class);
        if (hearingCentreOptional.isPresent()) {
            HearingCentre hearingCentre = hearingCentreOptional.get();
            if (hearingCentre != null) {
                // if hearingCentre epims id is empty, throw exception
                if (hearingCentre.getEpimsId().isEmpty()) {
                    throw new RequiredFieldMissingException("Hearing Centre EPIMS ID is not available for : "
                                                                + hearingCentre.getValue());
                }
                return hearingCentre.getEpimsId();
            }
        }
        return null;
    }
}
