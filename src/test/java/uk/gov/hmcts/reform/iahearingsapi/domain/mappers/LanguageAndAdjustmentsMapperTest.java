package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LACKING_CAPACITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper.INTERPRETER_LANGUAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper.OTHER_REASONABLE_ADJUSTMENTS_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper.REASONABLE_ADJUSTMENTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LanguageAndAdjustmentsMapperTest {

    @Mock
    private AsylumCase asylumCase;

    private LanguageAndAdjustmentsMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new LanguageAndAdjustmentsMapper();
    }

    @Test
    void should_sort_flags_into_fields_when_all_sorts_of_flags_present() {

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
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(
                new PartyFlagIdValue("id3", new StrategicCaseFlag(List.of(
                    new CaseFlagDetail("id3", CaseFlagValue.builder()
                        .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
                        .subTypeKey("ita")
                        .subTypeValue("Italian")
                        .status("Active")
                        .build())))),
                new PartyFlagIdValue("id4", new StrategicCaseFlag(List.of(
                    new CaseFlagDetail("id4", CaseFlagValue.builder()
                        .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
                        .subTypeKey("por")
                        .subTypeValue("Portuguese")
                        .status("Active")
                        .build()))))
            )));

        Map<String, List<String>> result = mapper.getLanguageAndAdjustmentsFields(asylumCase);

        assertEquals(List.of("bfi"), result.get(INTERPRETER_LANGUAGE));
        assertTrue(result.get(REASONABLE_ADJUSTMENTS).contains("RA0018"));
        assertTrue(result.get(OTHER_REASONABLE_ADJUSTMENTS_DETAILS).contains("Interpreter: German"));
        assertTrue(result.get(OTHER_REASONABLE_ADJUSTMENTS_DETAILS).contains("Interpreter: Italian"));
        assertTrue(result.get(OTHER_REASONABLE_ADJUSTMENTS_DETAILS).contains("Interpreter: Portuguese"));
        assertTrue(result.get(OTHER_REASONABLE_ADJUSTMENTS_DETAILS).contains("Support filling in forms: "
                                                                             + "comment of r.a. flag"));
    }

    @Test
    void should_sort_flags_into_fields_when_no_eligible_flag_present() {

        List<CaseFlagDetail> appellantCaseFlagDetails = new ArrayList<>();

        appellantCaseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .name("Lacking capacity")
            .flagComment("small capacity")
            .flagCode(LACKING_CAPACITY.getFlagCode())
            .status("Active")
            .build()));
        appellantCaseFlagDetails.add(new CaseFlagDetail("id2", CaseFlagValue.builder()
            .name("Lacking capacity")
            .flagComment("small capacity")
            .flagCode(LACKING_CAPACITY.getFlagCode())
            .status("Active")
            .build()));

        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(appellantCaseFlagDetails)));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.empty());

        Map<String, List<String>> result = mapper.getLanguageAndAdjustmentsFields(asylumCase);

        assertEquals(List.of(""), result.get(INTERPRETER_LANGUAGE));
        assertTrue(result.get(REASONABLE_ADJUSTMENTS).isEmpty());
        assertTrue(result.get(OTHER_REASONABLE_ADJUSTMENTS_DETAILS).isEmpty());
    }

    @Test
    void should_not_consider_inactive_flags() {

        List<CaseFlagDetail> appellantCaseFlagDetails = new ArrayList<>();
        appellantCaseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .subTypeKey("bfi")
            .subTypeValue("British Sign Language")
            .status("Inactive")
            .build()));

        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(appellantCaseFlagDetails)));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.empty());

        Map<String, List<String>> result = mapper.getLanguageAndAdjustmentsFields(asylumCase);

        assertEquals(List.of(""), result.get(INTERPRETER_LANGUAGE));
        assertTrue(result.get(REASONABLE_ADJUSTMENTS).isEmpty());
        assertTrue(result.get(OTHER_REASONABLE_ADJUSTMENTS_DETAILS).isEmpty());
    }

    @Test
    void should_write_manually_typed_languages_in_other_reasonable_adjustments() {

        List<CaseFlagDetail> appellantCaseFlagDetails = new ArrayList<>();
        appellantCaseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
            .subTypeKey(null)
            .subTypeValue("Bavarian")
            .status("Active")
            .build()));

        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(appellantCaseFlagDetails)));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.empty());

        Map<String, List<String>> result = mapper.getLanguageAndAdjustmentsFields(asylumCase);

        assertEquals(List.of(""), result.get(INTERPRETER_LANGUAGE));
        assertTrue(result.get(REASONABLE_ADJUSTMENTS).isEmpty());
        assertTrue(result.get(OTHER_REASONABLE_ADJUSTMENTS_DETAILS).contains("Interpreter: Bavarian"));
    }

}
