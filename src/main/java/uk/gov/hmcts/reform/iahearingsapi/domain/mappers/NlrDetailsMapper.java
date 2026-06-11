package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Component
public class NlrDetailsMapper {

    public PartyDetailsModel map(AsylumCase asylumCase,
                                 NonLegalRepDetails nonLegalRepDetails,
                                 CaseDataToServiceHearingValuesMapper caseDataMapper,
                                 HearingDetails persistedHearingDetails,
                                 Event event) {
        List<String> phones = nonLegalRepDetails.getPhoneNumber() != null
            ? List.of(nonLegalRepDetails.getPhoneNumber()) : Collections.emptyList();

        return PartyDetailsModel.builder()
            .partyID(nonLegalRepDetails.getIdamId())
            .partyType(PartyType.IND.getPartyType())
            .partyRole("NLRP")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName(nonLegalRepDetails.getGivenNames())
                    .lastName(nonLegalRepDetails.getFamilyName())
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(
                        asylumCase,
                        persistedHearingDetails,
                        event
                    ))
                    .hearingChannelEmail(List.of(nonLegalRepDetails.getEmailAddress()))
                    .hearingChannelPhone(phones)
                    .build())
            .build();
    }
}
