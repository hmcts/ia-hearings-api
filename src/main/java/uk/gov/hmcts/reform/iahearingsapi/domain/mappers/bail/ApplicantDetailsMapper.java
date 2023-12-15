package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper.appendBookingStatus;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CustodyStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;

@Component
@AllArgsConstructor
public class ApplicantDetailsMapper {

    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    public PartyDetailsModel map(BailCase bailCase,
                                 BailCaseFlagsToServiceHearingValuesMapper flagsToServiceHearingValuesMapper,
                                 BailCaseDataToServiceHearingValuesMapper bailCaseDataMapper) {

        IndividualDetailsModel individualDetails =
            IndividualDetailsModel.builder()
                .firstName(bailCaseDataMapper.getStringValueByDefinition(bailCase, APPLICANT_GIVEN_NAMES))
                .lastName(bailCaseDataMapper.getStringValueByDefinition(bailCase, APPLICANT_FAMILY_NAME))
                .preferredHearingChannel(bailCaseDataMapper.getHearingChannel(bailCase))
                .custodyStatus(CustodyStatus.IN_DETENTION.getValue())
                .vulnerableFlag(flagsToServiceHearingValuesMapper.getVulnerableFlag(bailCase))
                .vulnerabilityDetails(flagsToServiceHearingValuesMapper.getVulnerableDetails(bailCase))
                .build();

        PartyDetailsModel applicantPartyDetailsModel =
            PartyDetailsModel.builder()
                .partyID(bailCaseDataMapper.getApplicantPartyId(bailCase))
                .partyType(PartyType.IND.getPartyType())
                .partyRole("APPL")
                .individualDetails(individualDetails)
                .build();

        languageAndAdjustmentsMapper.processBailPartyCaseFlags(bailCase, applicantPartyDetailsModel);

        appendApplicantBookingStatus(bailCase, applicantPartyDetailsModel);

        return applicantPartyDetailsModel;
    }

    private void appendApplicantBookingStatus(BailCase bailCase,
                                              PartyDetailsModel applicantPartyDetailsModel) {

        Optional<InterpreterBookingStatus> spokenBookingStatus = bailCase
            .read(BailCaseFieldDefinition.APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS,
                InterpreterBookingStatus.class);

        Optional<InterpreterBookingStatus> signBookingStatus = bailCase
            .read(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class);

        appendBookingStatus(spokenBookingStatus, signBookingStatus, applicantPartyDetailsModel);
    }
}
