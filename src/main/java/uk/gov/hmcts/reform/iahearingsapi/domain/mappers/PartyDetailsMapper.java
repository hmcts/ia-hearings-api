package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static java.util.Objects.requireNonNullElse;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus.NOT_REQUESTED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@Component
@AllArgsConstructor
public class PartyDetailsMapper {

    private static final String STATUS = "Status: ";
    private static final String STATUS_SPOKEN = "Status (Spoken): ";
    private static final String STATUS_SIGN = "Status (Sign): ";

    private AppellantDetailsMapper appellantDetailsMapper;
    private LegalRepDetailsMapper legalRepDetailsMapper;
    private LegalRepOrgDetailsMapper legalRepOrgDetailsMapper;
    private RespondentDetailsMapper respondentDetailsMapper;
    private SponsorDetailsMapper sponsorDetailsMapper;
    private WitnessDetailsMapper witnessDetailsMapper;
    private InterpreterDetailsMapper interpreterDetailsMapper;

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
        partyDetails.addAll(interpreterDetailsMapper.map(asylumCase, caseDataMapper));

        return partyDetails;
    }

    public static PartyDetailsModel appendBookingStatus(Optional<InterpreterBookingStatus> spokenBookingStatus,
                                                 Optional<InterpreterBookingStatus> signBookingStatus,
                                                 PartyDetailsModel partyDetailsModel) {

        //String status;
        StringBuilder status = new StringBuilder();

        if (spokenBookingStatus.isPresent()
            && signBookingStatus.isPresent()
            && (!spokenBookingStatus.get().equals(NOT_REQUESTED)
                || !signBookingStatus.get().equals(NOT_REQUESTED))) {
            status
                .append(STATUS_SPOKEN)
                .append(spokenBookingStatus.get().getDesc())
                .append("; ")
                .append(STATUS_SIGN)
                .append(signBookingStatus.get().getDesc())
                .append(";");
        } else if (spokenBookingStatus.isPresent() && !spokenBookingStatus.get().equals(NOT_REQUESTED)) {
            status
                .append(STATUS)
                .append(spokenBookingStatus.get().getDesc())
                .append(";");
        } else if (signBookingStatus.isPresent() && !signBookingStatus.get().equals(NOT_REQUESTED)) {
            status
                .append(STATUS)
                .append(signBookingStatus.get().getDesc())
                .append(";");
        }

        String otherReasonableAdjustments = requireNonNullElse(partyDetailsModel.getIndividualDetails()
            .getOtherReasonableAdjustmentDetails(), "");

        partyDetailsModel.getIndividualDetails()
            .setOtherReasonableAdjustmentDetails((otherReasonableAdjustments + " " + status).trim());

        return partyDetailsModel;
    }
}
