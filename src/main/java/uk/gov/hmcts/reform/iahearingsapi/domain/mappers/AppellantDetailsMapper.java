package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.INTERNAL_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_SINGLE_SEX_COURT_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SINGLE_SEX_COURT_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.INTERNAL_APPELLANT_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PHONE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.GrantedRefusedType.GRANTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.SingleSexType.MALE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper.appendBookingStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ContactPreference;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Component
@AllArgsConstructor
public class AppellantDetailsMapper {

    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    public PartyDetailsModel map(
        AsylumCase asylumCase,
        CaseFlagsToServiceHearingValuesMapper caseFlagsMapper,
        CaseDataToServiceHearingValuesMapper caseDataMapper,
        HearingDetails persistedHearingDetails,
        Event event) {

        StringBuilder singleSexCourtResponse = new StringBuilder();
        if (GRANTED.getValue().equals(asylumCase.read(IS_SINGLE_SEX_COURT_ALLOWED, String.class).orElse(""))) {
            singleSexCourtResponse.append("Single sex court: ");
            if (MALE.getValue().equals(asylumCase.read(SINGLE_SEX_COURT_TYPE, String.class).orElse(""))) {
                singleSexCourtResponse.append("Male");
            } else {
                singleSexCourtResponse.append("Female");
            }
            singleSexCourtResponse.append(";");
        }

        boolean isInternalCase = MapperUtils.isInternalCase(asylumCase);

        AsylumCaseFieldDefinition emailFieldDef = isInternalCase
            ? INTERNAL_APPELLANT_EMAIL
            : (MapperUtils.isAipJourney(asylumCase) ? APPELLANT_EMAIL_ADDRESS : EMAIL);

        AsylumCaseFieldDefinition phoneFieldDef = isInternalCase
            ? INTERNAL_APPELLANT_MOBILE_NUMBER
            : (MapperUtils.isAipJourney(asylumCase) ? APPELLANT_PHONE_NUMBER : MOBILE_NUMBER);

        List<String> hearingChannelEmailValue;
        List<String> hearingChannelPhoneValue;
        if (isInternalCase) {
            hearingChannelEmailValue = caseDataMapper.getHearingChannelEmail(asylumCase, emailFieldDef);
            hearingChannelPhoneValue = caseDataMapper.getHearingChannelPhone(asylumCase, phoneFieldDef);
        } else {
            hearingChannelEmailValue = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                .map(c -> c.equals(ContactPreference.WANTS_EMAIL)
                    ? caseDataMapper.getHearingChannelEmail(asylumCase, emailFieldDef)
                    : Collections.<String>emptyList())
                .orElse(Collections.emptyList());

            hearingChannelPhoneValue = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                .map(c -> c.equals(ContactPreference.WANTS_SMS)
                    ? caseDataMapper.getHearingChannelPhone(asylumCase, phoneFieldDef)
                    : Collections.<String>emptyList())
                .orElse(Collections.emptyList());
        }

        IndividualDetailsModel individualDetails =
            IndividualDetailsModel.builder()
                .custodyStatus(caseFlagsMapper.getCustodyStatus(asylumCase))
                .vulnerabilityDetails(caseFlagsMapper.getVulnerableDetails(asylumCase))
                .vulnerableFlag(caseFlagsMapper.getVulnerableFlag(asylumCase))
                .firstName(caseDataMapper.getName(asylumCase, APPELLANT_GIVEN_NAMES))
                .lastName(caseDataMapper.getName(asylumCase, APPELLANT_FAMILY_NAME))
                .hearingChannelEmail(hearingChannelEmailValue)
                .hearingChannelPhone(hearingChannelPhoneValue)
                .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase, persistedHearingDetails, event))
                .build();

        if (!singleSexCourtResponse.isEmpty()) {
            individualDetails.setOtherReasonableAdjustmentDetails(singleSexCourtResponse.toString());
        }

        PartyDetailsModel appellantPartyDetailsModel = PartyDetailsModel.builder()
            .partyID(caseDataMapper.getAppellantPartyId(asylumCase))
            .partyType(PartyType.IND.getPartyType())
            .individualDetails(individualDetails)
            .partyRole("APEL")
            .unavailabilityDOW(Collections.emptyList())
            .unavailabilityRanges(caseDataMapper.getUnavailabilityRanges(asylumCase))
            .build();

        languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(asylumCase, appellantPartyDetailsModel);

        appendAppellantBookingStatus(asylumCase, appellantPartyDetailsModel);

        return appellantPartyDetailsModel;
    }

    private void appendAppellantBookingStatus(AsylumCase asylumCase,
                                              PartyDetailsModel appellantPartyDetailsModel) {

        Optional<InterpreterBookingStatus> spokenBookingStatus = asylumCase
            .read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class);

        Optional<InterpreterBookingStatus> signBookingStatus = asylumCase
            .read(APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS, InterpreterBookingStatus.class);

        appendBookingStatus(spokenBookingStatus, signBookingStatus, appellantPartyDetailsModel);
    }
}
