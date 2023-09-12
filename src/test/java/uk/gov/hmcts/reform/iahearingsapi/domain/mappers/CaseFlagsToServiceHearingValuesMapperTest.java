package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.ANONYMITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.AUDIO_VIDEO_EVIDENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.DETAINED_INDIVIDUAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.EVIDENCE_GIVEN_IN_PRIVATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.FOREIGN_NATIONAL_OFFENDER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LACKING_CAPACITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.PRESIDENTIAL_PANEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.UNACCOMPANIED_MINOR;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.URGENT_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.VULNERABLE_USER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CustodyStatus.IN_CUSTODY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CustodyStatus.IN_DETENTION;

import java.util.ArrayList;
import java.util.Arrays;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyFlagsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseFlagsToServiceHearingValuesMapperTest {

    private final String caseReference = "caseReference";
    private final String flagAmendUrl = "/cases/case-details/" + caseReference + "#Case%20flags";
    private CaseFlagsToServiceHearingValuesMapper mapper;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;

    @BeforeEach
    void setup() {
        mapper = new CaseFlagsToServiceHearingValuesMapper(caseDataMapper);
    }

    @Test
    void getPublicCaseName_should_return_case_reference() {

        StrategicCaseFlag caseLevelFlag = new StrategicCaseFlag(
            List.of(new CaseFlagDetail("id1", CaseFlagValue.builder()
                .flagCode(ANONYMITY.getFlagCode())
                .status("Active")
                .build())));
        when(asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class)).thenReturn(Optional.of(caseLevelFlag));

        assertEquals(caseReference, mapper.getPublicCaseName(asylumCase, caseReference));
    }

    @Test
    void getPublicCaseName_should_return_case_appellant_full_name() {

        String appellantFullName = "John Doe";
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantFullName));

        assertEquals(mapper.getPublicCaseName(asylumCase, caseReference), appellantFullName);
    }

    @Test
    void getCaseAdditionalSecurityFlag_should_return_true() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getCaseAdditionalSecurityFlag(asylumCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(FOREIGN_NATIONAL_OFFENDER.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getCaseAdditionalSecurityFlag(asylumCase));
    }

    @Test
    void getCaseAdditionalSecurityFlag_should_return_false() {

        assertFalse(mapper.getCaseAdditionalSecurityFlag(asylumCase));

        StrategicCaseFlag appellantFlags = new StrategicCaseFlag(
            List.of(new CaseFlagDetail("id1", CaseFlagValue.builder()
                        .flagCode(UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR.getFlagCode())
                        .status("Inactive")
                        .build())));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(appellantFlags));

        assertFalse(mapper.getCaseAdditionalSecurityFlag(asylumCase));
    }

    @Test
    void getAutoListFlag_should_return_false() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertFalse(mapper.getAutoListFlag(asylumCase));

        caseFlagDetails.addAll(Arrays.asList(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()), new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(FOREIGN_NATIONAL_OFFENDER.getFlagCode())
            .status("Active")
            .build()), new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(AUDIO_VIDEO_EVIDENCE.getFlagCode())
            .status("Active")
            .build()), new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(LITIGATION_FRIEND.getFlagCode())
            .status("Active")
            .build()), new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(LACKING_CAPACITY.getFlagCode())
            .status("Active")
            .build())));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));
        when(asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                List.of(new CaseFlagDetail(
                    "id1",
                    CaseFlagValue.builder()
                        .flagCode(PRESIDENTIAL_PANEL.getFlagCode())
                        .status("Active")
                        .build())))));

        assertFalse(mapper.getAutoListFlag(asylumCase));
    }

    @Test
    void getAutoListFlag_should_return_true() {

        assertTrue(mapper.getAutoListFlag(asylumCase));

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .status("Inactive")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getAutoListFlag(asylumCase));
    }

    @Test
    void getHearingPriorityType_should_return_urgent() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(URGENT_CASE.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertEquals(PriorityType.URGENT, mapper.getHearingPriorityType(asylumCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(UNACCOMPANIED_MINOR.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertEquals(PriorityType.URGENT, mapper.getHearingPriorityType(asylumCase));
    }

    @Test
    void getHearingPriorityType_should_return_standard() {

        assertEquals(PriorityType.STANDARD, mapper.getHearingPriorityType(asylumCase));

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(UNACCOMPANIED_MINOR.getFlagCode())
            .status("Inactive")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(ANONYMITY.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertEquals(PriorityType.STANDARD, mapper.getHearingPriorityType(asylumCase));
    }

    @Test
    void getListingComments_should_return_empty_string() {

        assertEquals("", mapper.getListingComments(asylumCase));
    }

    @Test
    void getListingComments_should_return_flag_comment() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        String flagComment = "Disruptive behaviour";
        String listingComments = "Customer behaviour: " + flagComment + ";";
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR.getFlagCode())
            .status("Active")
            .flagComment(flagComment)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertEquals(listingComments, mapper.getListingComments(asylumCase));
    }

    @Test
    void getListingComments_should_return_additional_instructions() {

        String additionalInstructionsTribunalResponse = "Additional instructions";
        when(asylumCase.read(ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(additionalInstructionsTribunalResponse));

        assertEquals(
            additionalInstructionsTribunalResponse + ";",
            mapper.getListingComments(asylumCase));
    }

    @Test
    void getListingComments_should_return_additional_instructions_and_flag_comment() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        String flagComment = "Disruptive behaviour";
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR.getFlagCode())
            .status("Active")
            .flagComment(flagComment)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        String additionalInstructionsTribunalResponse = "Additional instructions";
        when(asylumCase.read(ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(additionalInstructionsTribunalResponse));

        String listingComments =
            additionalInstructionsTribunalResponse + ";Customer behaviour: " + flagComment + ";";

        assertEquals(listingComments, mapper.getListingComments(asylumCase));
    }

    @Test
    void getPrivateHearingRequiredFlag_should_return_true() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(EVIDENCE_GIVEN_IN_PRIVATE.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getPrivateHearingRequiredFlag(asylumCase));
    }

    @Test
    void getPrivateHearingRequiredFlag_should_return_false() {

        assertFalse(mapper.getPrivateHearingRequiredFlag(asylumCase));

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(EVIDENCE_GIVEN_IN_PRIVATE.getFlagCode())
            .status("Inactive")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertFalse(mapper.getPrivateHearingRequiredFlag(asylumCase));
    }

    @Test
    void getCaseInterpreterRequiredFlag_should_return_true() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getCaseInterpreterRequiredFlag(asylumCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getCaseInterpreterRequiredFlag(asylumCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        List<PartyFlagIdValue> witnessCaseFlag = List.of(
            new PartyFlagIdValue("partyId1",
                          new StrategicCaseFlag(
                              "witness1",
                              "",
                              caseFlagDetails)));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS)).thenReturn(Optional.of(witnessCaseFlag));

        assertTrue(mapper.getCaseInterpreterRequiredFlag(asylumCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        witnessCaseFlag = List.of(
            new PartyFlagIdValue("partyId2",
                          new StrategicCaseFlag(
                              "witness2",
                              "",
                              caseFlagDetails)));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS)).thenReturn(Optional.of(witnessCaseFlag));

        assertTrue(mapper.getCaseInterpreterRequiredFlag(asylumCase));
    }

    @Test
    void getCaseInterpreterRequiredFlag_should_return_false() {

        assertFalse(mapper.getCaseInterpreterRequiredFlag(asylumCase));

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .status("Inactive")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertFalse(mapper.getCaseInterpreterRequiredFlag(asylumCase));
    }

    @Test
    void getCaseFlags_should_return_valid_case_flag_object_with_single_flag() {

        when(asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                List.of(new CaseFlagDetail("id1", CaseFlagValue.builder()
                    .flagCode(ANONYMITY.getFlagCode())
                    .name(ANONYMITY.getName())
                    .status("Active")
                    .hearingRelevant(YesOrNo.YES)
                    .build())))));


        Caseflags expected = Caseflags.builder()
            .flagAmendUrl(flagAmendUrl)
            .flags(List.of(
                PartyFlagsModel.builder()
                    .partyId(null)
                    .partyName(null)
                    .flagId("id1")
                    .flagStatus("Active")
                    .flagDescription(ANONYMITY.getName())
                    .build()
            )).build();

        Caseflags actual = mapper.getCaseFlags(asylumCase, caseReference);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void getCaseFlags_should_return_valid_case_flag_object_with_a_list_of_flags() {

        when(caseDataMapper.getAppellantPartyId(asylumCase)).thenReturn("appellantPartyId");

        List<CaseFlagDetail> appellantCaseFlagDetails = Arrays.asList(
            new CaseFlagDetail("id4", CaseFlagValue.builder()
                .flagCode(LITIGATION_FRIEND.getFlagCode())
                .name(LITIGATION_FRIEND.getName())
                .status("Active")
                .hearingRelevant(YesOrNo.YES)
                .build()),
            new CaseFlagDetail("id5", CaseFlagValue.builder()
                .flagCode(HEARING_LOOP.getFlagCode())
                .name(HEARING_LOOP.getName())
                .status("Inactive")
                .hearingRelevant(YesOrNo.YES)
                .build()),
            new CaseFlagDetail("id6", CaseFlagValue.builder()
                .flagCode(LACKING_CAPACITY.getFlagCode())
                .name(LACKING_CAPACITY.getName())
                .status("Active")
                .hearingRelevant(YesOrNo.NO)
                .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(
                new StrategicCaseFlag("appellant1", "", appellantCaseFlagDetails)));
        List<PartyFlagIdValue> witnessCaseFlag = List.of(
            new PartyFlagIdValue("witnessPartyId",
                          new StrategicCaseFlag(
                              "witness3",
                              "",
                              List.of(new CaseFlagDetail(
                                  "id7",
                                  CaseFlagValue.builder()
                                      .flagCode(HEARING_LOOP.getFlagCode())
                                      .name(HEARING_LOOP.getName())
                                      .status("Active")
                                      .hearingRelevant(YesOrNo.YES).build())))));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(witnessCaseFlag));

        Caseflags expected = Caseflags.builder()
            .flagAmendUrl(flagAmendUrl)
            .flags(List.of(
                PartyFlagsModel.builder()
                    .partyId("appellantPartyId")
                    .partyName("appellant1")
                    .flagId("id4")
                    .flagStatus("Active")
                    .flagDescription(LITIGATION_FRIEND.getName())
                    .build(),
                PartyFlagsModel.builder()
                    .partyId("witnessPartyId")
                    .partyName("witness3")
                    .flagId("id7")
                    .flagStatus("Active")
                    .flagDescription(HEARING_LOOP.getName())
                    .build()
            )).build();

        Caseflags actual = mapper.getCaseFlags(asylumCase, caseReference);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void getCaseFlags_should_return_empty_case_flag_object() {

        when(asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                Arrays.asList(
                    new CaseFlagDetail("id2", CaseFlagValue.builder()
                        .flagCode(URGENT_CASE.getFlagCode())
                        .name(URGENT_CASE.getName())
                        .status("Active")
                        .hearingRelevant(YesOrNo.NO)
                        .build()),
                    new CaseFlagDetail("id3", CaseFlagValue.builder()
                        .flagCode(PRESIDENTIAL_PANEL.getFlagCode())
                        .status("Inactive")
                        .hearingRelevant(YesOrNo.YES)
                        .build())))));

        Caseflags result = mapper.getCaseFlags(asylumCase, caseReference);
        assertNotNull(result);
        assertNull(result.getFlags());
    }

    @Test
    void getCustodyStatus_should_return_in_detention() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(DETAINED_INDIVIDUAL.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertEquals(IN_DETENTION.getValue(), mapper.getCustodyStatus(asylumCase));
    }

    @Test
    void getCustodyStatus_should_return_in_custody() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(FOREIGN_NATIONAL_OFFENDER.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertEquals(IN_CUSTODY.getValue(), mapper.getCustodyStatus(asylumCase));
    }

    @Test
    void getVulnerabilityFlag_should_return_true() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(VULNERABLE_USER.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getVulnerableFlag(asylumCase));

        caseFlagDetails.add(new CaseFlagDetail("id2", CaseFlagValue.builder()
            .flagCode(UNACCOMPANIED_MINOR.getFlagCode())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getVulnerableFlag(asylumCase));
    }

    @Test
    void getVulnerabilityFlag_should_return_false() {
        assertFalse(mapper.getVulnerableFlag(asylumCase));

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(VULNERABLE_USER.getFlagCode())
            .status("Inactive")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertFalse(mapper.getVulnerableFlag(asylumCase));
    }

    @Test
    void getVulnerabilityDetails_should_return_details() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        final String vulnerableUser = "Vulnerable user";
        final String unaccompaniedMinor = "Unaccompanied minor";
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(VULNERABLE_USER.getFlagCode()).status("Active")
            .flagComment(vulnerableUser).build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertEquals(vulnerableUser, mapper.getVulnerableDetails(asylumCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(UNACCOMPANIED_MINOR.getFlagCode())
            .status("Active")
            .flagComment(unaccompaniedMinor)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertEquals(vulnerableUser + ";" + unaccompaniedMinor, mapper.getVulnerableDetails(asylumCase));
    }

    @Test
    void getVulnerabilityDetails_should_return_null() {

        assertNull(mapper.getVulnerableDetails(asylumCase));

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(VULNERABLE_USER.getFlagCode())
            .status("Inactive")
            .flagComment("Vulnerable user")
            .build()));
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(UNACCOMPANIED_MINOR.getFlagCode())
            .status("Inactive")
            .flagComment("Unaccompanied minor")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(caseFlagDetails)));

        assertNull(mapper.getVulnerableDetails(asylumCase));
    }

}
