package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

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
    private RespondentDetailsMapper respondentDetailsMapper;
    private SponsorDetailsMapper sponsorDetailsMapper;
    private WitnessDetailsMapper witnessDetailsMapper;

    public List<PartyDetailsModel> map(
        AsylumCase asylumCase, CaseFlagsToServiceHearingValuesMapper caseFlagsMapper) {

        return Arrays.asList(
            appellantDetailsMapper.map(asylumCase, caseFlagsMapper),
            legalRepDetailsMapper.map(),
            respondentDetailsMapper.map(),
            sponsorDetailsMapper.map(),
            witnessDetailsMapper.map()
        );
    }
}
