package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PHONE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_TITLE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MOBILE_NUMBER;

import java.util.Collections;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;

@Component
public class AppellantDetailsMapper {

    public PartyDetailsModel map(
        AsylumCase asylumCase,
        CaseFlagsToServiceHearingValuesMapper caseFlagsMapper,
        CaseDataToServiceHearingValuesMapper caseDataMapper) {

        AsylumCaseFieldDefinition emailFieldDef = MapperUtils.isAipJourney(asylumCase)
            ? APPELLANT_EMAIL_ADDRESS
            : EMAIL;

        AsylumCaseFieldDefinition phoneFieldDef = MapperUtils.isAipJourney(asylumCase)
            ? APPELLANT_PHONE_NUMBER
            : MOBILE_NUMBER;

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getPartyId())
            .partyType(PartyType.IND.getPartyType())
            .individualDetails(
                IndividualDetailsModel.builder()
                    .custodyStatus(caseFlagsMapper.getCustodyStatus(asylumCase))
                    .vulnerabilityDetails(caseFlagsMapper.getVulnerableDetails(asylumCase))
                    .vulnerableFlag(caseFlagsMapper.getVulnerableFlag(asylumCase))
                    .firstName(caseDataMapper.getName(asylumCase, APPELLANT_GIVEN_NAMES))
                    .lastName(caseDataMapper.getName(asylumCase, APPELLANT_FAMILY_NAME))
                    .title(asylumCase.read(APPELLANT_TITLE, String.class).orElse(null))
                    .hearingChannelEmail(
                        caseDataMapper.getHearingChannelEmail(asylumCase, emailFieldDef))
                    .hearingChannelPhone(
                        caseDataMapper.getHearingChannelPhone(asylumCase, phoneFieldDef))
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase))
                    .build())
            .partyRole("APEL")
            .unavailabilityDOW(Collections.emptyList())
            .unavailabilityRanges(caseDataMapper.getUnavailabilityRanges(asylumCase))
            .build();
    }
}