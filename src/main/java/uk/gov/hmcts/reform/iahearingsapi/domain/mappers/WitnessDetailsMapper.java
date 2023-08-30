package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;

@Component
public class WitnessDetailsMapper {

    public List<PartyDetailsModel> map(AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {

        Optional<List<IdValue<WitnessDetails>>> witnessDetailsOptional = asylumCase.read(WITNESS_DETAILS);

        return witnessDetailsOptional.map(idValues -> idValues.stream()
            .map(IdValue::getValue).map(witnessDetails -> PartyDetailsModel.builder()
                .partyID(caseDataMapper.getPartyId())
                .partyType(PartyType.IND.getPartyType())
                .partyRole("WITN")
                .individualDetails(
                    IndividualDetailsModel.builder()
                        .firstName(witnessDetails.getWitnessName())
                        .lastName(witnessDetails.getWitnessFamilyName())
                        .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase))
                        .build())
                .build()).toList()).orElse(Collections.emptyList());

    }
}
