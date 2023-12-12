package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;

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
                .custodyStatus("D")
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

        return applicantPartyDetailsModel;
    }
}
