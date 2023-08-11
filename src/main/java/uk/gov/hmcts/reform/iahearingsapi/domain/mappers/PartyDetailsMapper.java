package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@Component
@AllArgsConstructor
public class PartyDetailsMapper {

    private AppellantDetailsMapper appellantDetailsMapper;
    private LegalRepDetailsMapper legalRepDetailsMapper;
    private LegalRepOrgDetailsMapper legalRepOrgDetailsMapper;
    private RespondentDetailsMapper respondentDetailsMapper;
    private SponsorDetailsMapper sponsorDetailsMapper;
    private WitnessDetailsMapper witnessDetailsMapper;

    public List<PartyDetailsModel> map(
        AsylumCase asylumCase,
        CaseFlagsToServiceHearingValuesMapper caseFlagsMapper,
        CaseDataToServiceHearingValuesMapper caseDataMapper) {

        List<PartyDetailsModel> partyDetails = new ArrayList<>(Arrays.asList(
            appellantDetailsMapper.map(asylumCase, caseFlagsMapper, caseDataMapper),
            respondentDetailsMapper.map(asylumCase, caseDataMapper)
        ));
        if (MapperUtils.hasSponsor(asylumCase)) {
            partyDetails.add(sponsorDetailsMapper.map(asylumCase, caseDataMapper));
        }
        if (MapperUtils.isRepJourney(asylumCase)) {
            partyDetails.add(legalRepDetailsMapper.map(asylumCase, caseDataMapper));
            partyDetails.add(legalRepOrgDetailsMapper.map(asylumCase, caseDataMapper));
        }
        partyDetails.addAll(witnessDetailsMapper.map(asylumCase, caseDataMapper));

        return partyDetails;
    }
}
