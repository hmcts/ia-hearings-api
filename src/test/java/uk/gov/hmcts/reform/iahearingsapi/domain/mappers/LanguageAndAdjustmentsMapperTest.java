package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.EVIDENCE_GIVEN_IN_PRIVATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LanguageAndAdjustmentsMapperTest {

    private static final String APPELLANT_PARTY_ID = "APEL";
    private static final String WITNESS_PARTY_ID = "WITN";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String PARTY_ID = "partyId";

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private PartyDetailsModel partyDetailsModel;

    @Mock
    private IndividualDetailsModel individualDetailsModel;

    @Mock
    private List<String> reasonableAdjustments;

    private LanguageAndAdjustmentsMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new LanguageAndAdjustmentsMapper();
    }

    @Test
    void should_add_flag_values_accordingly_to_appellant_party_details() {

        List<CaseFlagDetail> appellantCaseFlagDetails = new ArrayList<>();
        appellantCaseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .subTypeKey("bfi")
            .subTypeValue("British Sign Language")
            .status("Active")
            .build()));
        appellantCaseFlagDetails.add(new CaseFlagDetail("id2", CaseFlagValue.builder()
            .subTypeKey("deu")
            .subTypeValue("German")
            .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        appellantCaseFlagDetails.add(new CaseFlagDetail("id5", CaseFlagValue.builder()
            .name("Support filling in forms")
            .flagComment("comment of r.a. flag")
            .flagCode("RA0018")
            .status("Active")
            .build()));

        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(appellantCaseFlagDetails)));
        when(partyDetailsModel.getIndividualDetails()).thenReturn(individualDetailsModel);
        when(partyDetailsModel.getPartyRole()).thenReturn(APPELLANT_PARTY_ID);
        when(individualDetailsModel.getFirstName()).thenReturn(FIRST_NAME);
        when(individualDetailsModel.getLastName()).thenReturn(LAST_NAME);
        when(individualDetailsModel.getReasonableAdjustments()).thenReturn(reasonableAdjustments);

        mapper.processAsylumPartyCaseFlags(asylumCase, partyDetailsModel);

        verify(individualDetailsModel, times(1)).setInterpreterLanguage("bfi");
        verify(reasonableAdjustments, times(1)).addAll(List.of("RA0042", "RA0018"));
        verify(individualDetailsModel, times(1))
            .setOtherReasonableAdjustmentDetails("Interpreter: German; "
                                                 + "Support filling in forms: comment of r.a. flag;");
    }

    @Test
    void should_add_flag_values_accordingly_to_witness_party_details() {

        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(
                new PartyFlagIdValue(PARTY_ID, new StrategicCaseFlag(List.of(
                    new CaseFlagDetail("id1", CaseFlagValue.builder()
                        .flagCode(EVIDENCE_GIVEN_IN_PRIVATE.getFlagCode())
                        .name("Evidence given in private")
                        .flagComment("asking for privacy")
                        .status("Active")
                        .build())))),
                new PartyFlagIdValue(PARTY_ID, new StrategicCaseFlag(List.of(
                    new CaseFlagDetail("id2", CaseFlagValue.builder()
                        .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
                        .subTypeValue("Sardinian")
                        .status("Active")
                        .build())))),
                new PartyFlagIdValue(PARTY_ID, new StrategicCaseFlag(List.of(
                    new CaseFlagDetail("id3", CaseFlagValue.builder()
                        .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
                        .subTypeKey("ita")
                        .subTypeValue("Italian")
                        .status("Active")
                        .build())))),
                new PartyFlagIdValue(PARTY_ID, new StrategicCaseFlag(List.of(
                    new CaseFlagDetail("id4", CaseFlagValue.builder()
                        .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
                        .subTypeKey("por")
                        .subTypeValue("Portuguese")
                        .status("Active")
                        .build())))),
                new PartyFlagIdValue("Another name", new StrategicCaseFlag(List.of(
                    new CaseFlagDetail("id5", CaseFlagValue.builder()
                        .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
                        .subTypeKey("spa")
                        .subTypeValue("Spanish")
                        .status("Active")
                        .build())))),
                new PartyFlagIdValue(PARTY_ID, new StrategicCaseFlag(List.of(
                    new CaseFlagDetail("id6", CaseFlagValue.builder()
                        .name("Support filling in forms")
                        .flagComment("comment of r.a. flag")
                        .flagCode("RA0018")
                        .status("Active")
                        .build()))))
            )));

        when(partyDetailsModel.getIndividualDetails()).thenReturn(individualDetailsModel);
        when(partyDetailsModel.getPartyRole()).thenReturn(WITNESS_PARTY_ID);
        when(individualDetailsModel.getFirstName()).thenReturn(FIRST_NAME);
        when(individualDetailsModel.getLastName()).thenReturn(LAST_NAME);
        when(individualDetailsModel.getReasonableAdjustments()).thenReturn(reasonableAdjustments);
        when(partyDetailsModel.getPartyID()).thenReturn(PARTY_ID);

        mapper.processAsylumPartyCaseFlags(asylumCase, partyDetailsModel);

        verify(individualDetailsModel, times(1)).setInterpreterLanguage("ita");
        verify(reasonableAdjustments, times(1)).addAll(List.of("SM0004", "RA0018"));
        verify(individualDetailsModel, times(1))
            .setOtherReasonableAdjustmentDetails("Interpreter: Portuguese; "
                                                 + "Interpreter: Sardinian; "
                                                 + "Evidence given in private: asking for privacy; "
                                                 + "Support filling in forms: comment of r.a. flag;");
    }

    @Test
    void should_add_nothing_to_individual_details_when_no_qualifying_active_flags_exist() {

        List<CaseFlagDetail> appellantCaseFlagDetails = new ArrayList<>();
        appellantCaseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .subTypeKey("bfi")
            .subTypeValue("British Sign Language")
            .status("Inactive")
            .build()));

        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(appellantCaseFlagDetails)));
        when(partyDetailsModel.getIndividualDetails()).thenReturn(individualDetailsModel);
        when(partyDetailsModel.getPartyRole()).thenReturn(APPELLANT_PARTY_ID);
        when(individualDetailsModel.getFirstName()).thenReturn(FIRST_NAME);
        when(individualDetailsModel.getLastName()).thenReturn(LAST_NAME);
        when(individualDetailsModel.getReasonableAdjustments()).thenReturn(reasonableAdjustments);

        mapper.processAsylumPartyCaseFlags(asylumCase, partyDetailsModel);

        verify(individualDetailsModel, times(1)).setInterpreterLanguage(null);
        verify(reasonableAdjustments, times(1)).addAll(Collections.emptyList());
        verify(individualDetailsModel, never()).setOtherReasonableAdjustmentDetails(anyString());
    }

}
