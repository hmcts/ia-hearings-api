package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper.appendBookingStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.InterpreterLanguagesUtils.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.InterpreterLanguagesUtils.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;

@Component
@AllArgsConstructor
public class WitnessDetailsMapper {

    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    public List<PartyDetailsModel> map(AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {

        Optional<List<IdValue<WitnessDetails>>> witnessDetailsOptional = asylumCase.read(WITNESS_DETAILS);
        List<IdValue<WitnessDetails>> witnessDetailsList = witnessDetailsOptional.orElse(Collections.emptyList());
        Map<IdValue<WitnessDetails>, Integer> witnessDetailsMap = idValuesToIndex(witnessDetailsList);

        return witnessDetailsOptional.map(idValues -> idValues.stream()
            .map(witnessDetailsIdValue -> {

                WitnessDetails witnessDetails = witnessDetailsIdValue.getValue();

                PartyDetailsModel witnessPartyDetailsModel = PartyDetailsModel.builder()
                    .partyID(witnessDetails.getWitnessPartyId())
                    .partyType(PartyType.IND.getPartyType())
                    .partyRole("WITN")
                    .individualDetails(
                        IndividualDetailsModel.builder()
                            .firstName(witnessDetails.getWitnessName())
                            .lastName(witnessDetails.getWitnessFamilyName())
                            .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase))
                            .build())
                    .build();

                languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(asylumCase, witnessPartyDetailsModel);

                int index = witnessDetailsMap.get(witnessDetailsIdValue);
                appendWitnessBookingStatus(asylumCase, index, witnessPartyDetailsModel);

                return witnessPartyDetailsModel;
            }).toList())
            .orElse(Collections.emptyList());
    }

    private void appendWitnessBookingStatus(AsylumCase asylumCase,
                                            int index,
                                            PartyDetailsModel witnessPartyDetailsModel) {

        Optional<InterpreterBookingStatus> spokenBookingStatus = asylumCase
            .read(WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.get(index), InterpreterBookingStatus.class);

        Optional<InterpreterBookingStatus> signBookingStatus = asylumCase
            .read(WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.get(index), InterpreterBookingStatus.class);

        appendBookingStatus(spokenBookingStatus, signBookingStatus, witnessPartyDetailsModel);
    }

    private Map<IdValue<WitnessDetails>,Integer> idValuesToIndex(List<IdValue<WitnessDetails>> witnessDetailsIdValues) {
        Map<IdValue<WitnessDetails>,Integer> idValuesToIndex = new HashMap<>();

        int i = 0;
        while (i < witnessDetailsIdValues.size()) {
            idValuesToIndex.put(witnessDetailsIdValues.get(i), i);
            i++;
        }

        return idValuesToIndex;
    }
}
