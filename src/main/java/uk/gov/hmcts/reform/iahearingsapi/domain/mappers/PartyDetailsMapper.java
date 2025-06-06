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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.ApplicantDetailsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailInterpreterDetailsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailMapperUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.FinancialConditionSupporterDetailsMapper;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.PartyDetails;


@Component
@AllArgsConstructor
public class PartyDetailsMapper {

    private static final String STATUS = "Status: ";
    private static final String STATUS_SPOKEN = "Status (Spoken): ";
    private static final String STATUS_SIGN = "Status (Sign): ";

    private AppellantDetailsMapper appellantDetailsMapper;
    private ApplicantDetailsMapper applicantDetailsMapper;
    private LegalRepDetailsMapper legalRepDetailsMapper;
    private LegalRepOrgDetailsMapper legalRepOrgDetailsMapper;
    private RespondentDetailsMapper respondentDetailsMapper;
    private SponsorDetailsMapper sponsorDetailsMapper;
    private WitnessDetailsMapper witnessDetailsMapper;
    private FinancialConditionSupporterDetailsMapper financialConditionSupporterDetailsMapper;
    private InterpreterDetailsMapper interpreterDetailsMapper;

    private BailInterpreterDetailsMapper bailInterpreterDetailsMapper;

    public List<PartyDetailsModel> mapAsylumPartyDetails(
        AsylumCase asylumCase,
        CaseFlagsToServiceHearingValuesMapper caseFlagsMapper,
        CaseDataToServiceHearingValuesMapper caseDataMapper) {

        return mapAsylumPartyDetails(asylumCase, caseFlagsMapper, caseDataMapper, null, null);
    }

    public List<PartyDetailsModel> mapAsylumPartyDetails(
        AsylumCase asylumCase,
        CaseFlagsToServiceHearingValuesMapper caseFlagsMapper,
        CaseDataToServiceHearingValuesMapper caseDataMapper,
        HearingDetails persistedHearingDetails,
        Event event) {

        List<PartyDetailsModel> partyDetails = new ArrayList<>(Arrays.asList(
            appellantDetailsMapper.map(asylumCase, caseFlagsMapper, caseDataMapper, persistedHearingDetails, event),
            respondentDetailsMapper.map(asylumCase, caseDataMapper)
        ));
        if (MapperUtils.hasSponsor(asylumCase)) {
            partyDetails.add(sponsorDetailsMapper.map(asylumCase, caseDataMapper, persistedHearingDetails, event));
        }
        if (!MapperUtils.isInternalCase(asylumCase)
            && MapperUtils.isRepJourney(asylumCase)
            && !MapperUtils.isChangeOrganisationRequestPresent(asylumCase)) {

            partyDetails.add(legalRepDetailsMapper.map(asylumCase, caseDataMapper, persistedHearingDetails, event));
            partyDetails.add(legalRepOrgDetailsMapper.map(asylumCase, caseDataMapper));
        }
        if (MapperUtils.isInternalCase(asylumCase) && MapperUtils.isInternalCaseHasLegalRep(asylumCase)) {
            partyDetails.add(legalRepDetailsMapper
                                 .mapInternalCaseLegalRep(asylumCase, caseDataMapper, persistedHearingDetails, event));
            partyDetails.add(legalRepOrgDetailsMapper.mapInternalCaseLegalRepOrg(asylumCase, caseDataMapper));
        }

        partyDetails.addAll(witnessDetailsMapper.map(asylumCase, caseDataMapper, persistedHearingDetails, event));
        partyDetails.addAll(interpreterDetailsMapper.map(asylumCase, caseDataMapper, persistedHearingDetails, event));

        return partyDetails;

    }

    public List<PartyDetailsModel> mapBailPartyDetails(
        BailCase bailCase,
        BailCaseFlagsToServiceHearingValuesMapper bailCaseFlagsMapper,
        BailCaseDataToServiceHearingValuesMapper bailCaseDataMapper) {

        List<PartyDetailsModel> partyDetails = new ArrayList<>(Arrays.asList(
            applicantDetailsMapper.map(bailCase, bailCaseFlagsMapper, bailCaseDataMapper),
            respondentDetailsMapper.map(bailCase, bailCaseDataMapper)
        ));
        if (BailMapperUtils.isLegallyRepresented(bailCase)) {
            partyDetails.add(legalRepDetailsMapper.map(bailCase, bailCaseDataMapper));
            partyDetails.add(legalRepOrgDetailsMapper.map(bailCase, bailCaseDataMapper));
        }

        partyDetails.addAll(financialConditionSupporterDetailsMapper.map(bailCase, bailCaseDataMapper));
        partyDetails.addAll(bailInterpreterDetailsMapper.map(bailCase, bailCaseDataMapper));

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

    public static PartyDetails mapPartyDetailsModelToPartyDetails(PartyDetailsModel partyDetailsModel) {
        return PartyDetails.builder()
            .individualDetails(partyDetailsModel.getIndividualDetails())
            .partyID(partyDetailsModel.getPartyID())
            .organisationDetails(partyDetailsModel.getOrganisationDetails())
            .partyRole(partyDetailsModel.getPartyRole())
            .partyType(partyDetailsModel.getPartyType())
            .unavailabilityDayOfWeek(partyDetailsModel.getUnavailabilityDOW())
            .unavailabilityRanges(partyDetailsModel.getUnavailabilityRanges())
            .build();
    }
}
