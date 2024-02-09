package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_DETAILS;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Component
public class InterpreterDetailsMapper {

    public static final String ROLE_CODE_INTERPRETER = "INTP";
    public static final String ADJUSTMENT_DETAILS_FORMAT = "Booking Ref: %s; " + " Notes: %s";

    public List<PartyDetailsModel> map(AsylumCase asylumCase,
                                       CaseDataToServiceHearingValuesMapper caseDataMapper,
                                       HearingDetails persistedHearingDetails,
                                       Event event) {
        Optional<List<IdValue<InterpreterDetails>>> interpreterDetailsOptional = asylumCase.read(INTERPRETER_DETAILS);

        return interpreterDetailsOptional
            .map(idValues -> idValues.stream()
                .map(IdValue::getValue)
                .map(interpreterDetails -> getPartyDetails(asylumCase,
                                                           caseDataMapper,
                                                           interpreterDetails,
                                                           persistedHearingDetails,
                                                           event))
                .toList())
            .orElse(Collections.emptyList());
    }

    private static PartyDetailsModel getPartyDetails(AsylumCase asylumCase,
                                                     CaseDataToServiceHearingValuesMapper caseDataMapper,
                                                     InterpreterDetails interpreterDetails,
                                                     HearingDetails persistedHearingDetails,
                                                     Event event) {
        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName(interpreterDetails.getInterpreterGivenNames())
            .lastName(interpreterDetails.getInterpreterFamilyName())
            .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase, persistedHearingDetails, event))
            .hearingChannelEmail(Arrays.asList(interpreterDetails.getInterpreterEmail()))
            .hearingChannelPhone(Arrays.asList(interpreterDetails.getInterpreterPhoneNumber()))
            .otherReasonableAdjustmentDetails(getOtherReasonableAdjustmentDetails(interpreterDetails))
            .build();

        return PartyDetailsModel.builder()
            .partyID(interpreterDetails.getInterpreterId())
            .partyType(PartyType.IND.getPartyType())
            .partyRole(ROLE_CODE_INTERPRETER)
            .individualDetails(individualDetails)
            .build();
    }

    private static String getOtherReasonableAdjustmentDetails(InterpreterDetails interpreterDetails) {
        return String.format(ADJUSTMENT_DETAILS_FORMAT, interpreterDetails.getInterpreterBookingRef(),
            interpreterDetails.getInterpreterNote());
    }
}
