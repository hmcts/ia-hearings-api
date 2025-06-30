package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_MOBILE_NUMBER;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Component
public class SponsorDetailsMapper {

    public PartyDetailsModel map(AsylumCase asylumCase,
                                 CaseDataToServiceHearingValuesMapper caseDataMapper,
                                 HearingDetails persistedHearingDetails,
                                 Event event) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getSponsorPartyId(asylumCase))
            .partyType(PartyType.IND.getPartyType())
            .partyRole("SPON")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName(caseDataMapper.getName(asylumCase, SPONSOR_GIVEN_NAMES))
                    .lastName(caseDataMapper.getName(asylumCase, SPONSOR_FAMILY_NAME))
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase,
                                                                              persistedHearingDetails,
                                                                              event))
                    .hearingChannelEmail(
                        caseDataMapper.getHearingChannelEmail(asylumCase, SPONSOR_EMAIL))
                    .hearingChannelPhone(
                        caseDataMapper.getHearingChannelPhone(asylumCase, SPONSOR_MOBILE_NUMBER))
                    .build())
            .build();
    }
}
