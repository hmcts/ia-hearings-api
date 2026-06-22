package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NLR_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NLR_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper.appendBookingStatus;

@Component
@AllArgsConstructor
public class NlrDetailsMapper {

    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    // TODO change this value to RPTT one once it's been added to BFA1 code in rd-commondata
    //  For now, we are using the value for Intermediary as a placeholder.
    // public static final String NLR_PARTY_ROLE = "RPTT";
    public static final String NLR_PARTY_ROLE = "INTE";

    public PartyDetailsModel map(AsylumCase asylumCase,
                                 NonLegalRepDetails nonLegalRepDetails,
                                 CaseDataToServiceHearingValuesMapper caseDataMapper,
                                 HearingDetails persistedHearingDetails,
                                 Event event) {
        List<String> phones = nonLegalRepDetails.getPhoneNumber() != null
            ? List.of(nonLegalRepDetails.getPhoneNumber()) : Collections.emptyList();

        PartyDetailsModel nlrPartyDetailsModel = PartyDetailsModel.builder()
            .partyID(nonLegalRepDetails.getIdamId())
            .partyType(PartyType.IND.getPartyType())
            .partyRole(NLR_PARTY_ROLE)
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

        languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(asylumCase, nlrPartyDetailsModel);

        appendNlrBookingStatus(asylumCase, nlrPartyDetailsModel);

        return nlrPartyDetailsModel;
    }

    private void appendNlrBookingStatus(AsylumCase asylumCase,
                                        PartyDetailsModel nlrPartyDetailsModel) {

        Optional<InterpreterBookingStatus> spokenBookingStatus = asylumCase
            .read(NLR_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class);

        Optional<InterpreterBookingStatus> signBookingStatus = asylumCase
            .read(NLR_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class);

        appendBookingStatus(spokenBookingStatus, signBookingStatus, nlrPartyDetailsModel);
    }
}
