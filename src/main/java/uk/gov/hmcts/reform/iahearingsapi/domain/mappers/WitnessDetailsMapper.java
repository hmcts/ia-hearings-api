package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus.NOT_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper.appendBookingStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.InterpreterLanguagesUtils.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.InterpreterLanguagesUtils.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES;

import java.util.Collections;
import java.util.List;
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

                languageAndAdjustmentsMapper.processPartyCaseFlags(asylumCase, witnessPartyDetailsModel);

                appendWitnessBookingStatus(asylumCase, witnessDetailsIdValue, witnessPartyDetailsModel);

                return witnessPartyDetailsModel;
            }).toList())
            .orElse(Collections.emptyList());
    }

    private void appendWitnessBookingStatus(AsylumCase asylumCase,
                                            IdValue<WitnessDetails> witnessDetailsIdValue,
                                            PartyDetailsModel witnessPartyDetailsModel) {

        int id = Integer.parseInt(witnessDetailsIdValue.getId());

        Optional<InterpreterBookingStatus> spokenBookingStatus = asylumCase
            .read(WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.get(id - 1), InterpreterBookingStatus.class)
            .filter(interpreterBookingStatus -> !interpreterBookingStatus.equals(NOT_REQUESTED));

        Optional<InterpreterBookingStatus> signBookingStatus = asylumCase
            .read(WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.get(id - 1), InterpreterBookingStatus.class)
            .filter(interpreterBookingStatus -> !interpreterBookingStatus.equals(NOT_REQUESTED));

        appendBookingStatus(spokenBookingStatus, signBookingStatus, witnessPartyDetailsModel);
    }
}
