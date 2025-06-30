package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.CASE_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.ANONYMITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.EVIDENCE_GIVEN_IN_PRIVATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LACKING_CAPACITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.PRESIDENTIAL_PANEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.UNACCOMPANIED_MINOR;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.URGENT_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.VULNERABLE_USER;

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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailPartyFlagIdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyFlagsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BailCaseFlagsToServiceHearingValuesMapperTest {

    private static final String caseLevelFlags = "Case level flags";
    private static final String caseLevelFlagsPartyID = "Caselevelflags";
    public static final String DATE_TIME_CREATED = "2024-04-11T13:43:15.044Z";
    public static final String DATE_TIME_MODIFIED = "2024-04-15T16:23:10.044Z";
    public static final String DATE_TIME_CREATED_NO_NANOS = "2024-04-11T13:43:15";
    public static final String DATE_TIME_MODIFIED_NO_NANOS = "2024-04-15T16:23:10";
    private final String caseReference = "caseReference";
    private final String flagAmendUrl = "/cases/case-details/" + caseReference + "#Case%20flags";
    private BailCaseFlagsToServiceHearingValuesMapper mapper;

    @Mock
    private BailCase bailCase;

    @Mock
    private BailCaseDataToServiceHearingValuesMapper caseDataMapper;

    @BeforeEach
    void setup() {
        mapper = new BailCaseFlagsToServiceHearingValuesMapper(caseDataMapper);
    }

    @Test
    void getPublicCaseName_should_return_case_reference() {

        BailStrategicCaseFlag caseLevelFlag = new BailStrategicCaseFlag(
            List.of(new CaseFlagDetail("id1", CaseFlagValue.builder()
                .flagCode(ANONYMITY.getFlagCode())
                .status("Active")
                .build())));
        when(bailCase.read(CASE_FLAGS, BailStrategicCaseFlag.class)).thenReturn(Optional.of(caseLevelFlag));

        assertEquals(caseReference, mapper.getPublicCaseName(bailCase, caseReference));
    }

    @Test
    void getPublicCaseName_should_return_case_applicant_full_name() {

        String applicantFullName = "John Doe";
        when(bailCase.read(APPLICANT_FULL_NAME, String.class)).thenReturn(Optional.of(applicantFullName));

        assertEquals(mapper.getPublicCaseName(bailCase, caseReference), applicantFullName);
    }

    @Test
    void getHearingPriorityType_should_return_standard() {

        assertEquals(PriorityType.STANDARD, mapper.getHearingPriorityType(bailCase));
    }

    @Test
    void getHearingPriorityType_should_return_urgent() {

        BailStrategicCaseFlag caseLevelFlag = new BailStrategicCaseFlag(
            List.of(new CaseFlagDetail("id1", CaseFlagValue.builder()
                .flagCode(URGENT_CASE.getFlagCode())
                .status("Active")
                .build())));
        when(bailCase.read(CASE_FLAGS, BailStrategicCaseFlag.class)).thenReturn(Optional.of(caseLevelFlag));

        assertEquals(PriorityType.URGENT, mapper.getHearingPriorityType(bailCase));
    }

    @Test
    void getPrivateHearingRequiredFlag_should_return_true() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(EVIDENCE_GIVEN_IN_PRIVATE.getFlagCode())
            .status("Active")
            .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getPrivateHearingRequiredFlag(bailCase));
    }

    @Test
    void getPrivateHearingRequiredFlag_should_return_false() {

        assertFalse(mapper.getPrivateHearingRequiredFlag(bailCase));

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(EVIDENCE_GIVEN_IN_PRIVATE.getFlagCode())
            .status("Inactive")
            .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertFalse(mapper.getPrivateHearingRequiredFlag(bailCase));
    }

    @Test
    void getCaseInterpreterRequiredFlag_should_return_true() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getCaseInterpreterRequiredFlag(bailCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getCaseInterpreterRequiredFlag(bailCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        List<BailPartyFlagIdValue> fcsCaseFlag = List.of(
            new BailPartyFlagIdValue("partyId1",
                                 new BailStrategicCaseFlag(
                                     "witness1",
                                     "",
                                     caseFlagDetails)));
        when(bailCase.read(FCS_LEVEL_FLAGS)).thenReturn(Optional.of(fcsCaseFlag));

        assertTrue(mapper.getCaseInterpreterRequiredFlag(bailCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
            .status("Active")
            .build()));
        fcsCaseFlag = List.of(
            new BailPartyFlagIdValue("partyId2",
                                 new BailStrategicCaseFlag(
                                     "witness2",
                                     "",
                                     caseFlagDetails)));
        when(bailCase.read(FCS_LEVEL_FLAGS)).thenReturn(Optional.of(fcsCaseFlag));

        assertTrue(mapper.getCaseInterpreterRequiredFlag(bailCase));
    }

    @Test
    void getCaseInterpreterRequiredFlag_should_return_false() {

        assertFalse(mapper.getCaseInterpreterRequiredFlag(bailCase));

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .status("Inactive")
            .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertFalse(mapper.getCaseInterpreterRequiredFlag(bailCase));
    }

    @Test
    void getCaseFlags_should_return_valid_case_flag_object_with_single_flag() {

        when(bailCase.read(CASE_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(
                List.of(new CaseFlagDetail("id1", CaseFlagValue.builder()
                    .flagCode(ANONYMITY.getFlagCode())
                    .name(ANONYMITY.getName())
                    .status("Active")
                    .hearingRelevant(YesOrNo.YES)
                    .flagComment("test comment")
                    .dateTimeCreated(DATE_TIME_CREATED)
                    .dateTimeModified(DATE_TIME_MODIFIED)
                    .build())))));


        Caseflags expected = Caseflags.builder()
            .flagAmendUrl(flagAmendUrl)
            .flags(List.of(
                PartyFlagsModel.builder()
                    .partyId(caseLevelFlagsPartyID)
                    .partyName(caseLevelFlags)
                    .flagId(ANONYMITY.getFlagCode())
                    .flagStatus("Active")
                    .flagDescription("test comment")
                    .dateTimeCreated(DATE_TIME_CREATED_NO_NANOS)
                    .dateTimeModified(DATE_TIME_MODIFIED_NO_NANOS)
                    .build()
            )).build();

        Caseflags actual = mapper.getCaseFlags(bailCase, caseReference);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void getCaseFlags_should_return_valid_case_flag_object_with_a_list_of_flags() {

        when(caseDataMapper.getApplicantPartyId(bailCase)).thenReturn("applicantPartyId");

        List<CaseFlagDetail> applicantCaseFlagDetails = Arrays.asList(
            new CaseFlagDetail("id4", CaseFlagValue.builder()
                .flagCode(LITIGATION_FRIEND.getFlagCode())
                .name(LITIGATION_FRIEND.getName())
                .status("Active")
                .hearingRelevant(YesOrNo.YES)
                .flagComment("test comment")
                .dateTimeCreated(DATE_TIME_CREATED)
                .dateTimeModified(DATE_TIME_MODIFIED)
                .build()),
            new CaseFlagDetail("id5", CaseFlagValue.builder()
                .flagCode(HEARING_LOOP.getFlagCode())
                .name(HEARING_LOOP.getName())
                .status("Inactive")
                .hearingRelevant(YesOrNo.YES)
                .dateTimeCreated(DATE_TIME_CREATED)
                .dateTimeModified(DATE_TIME_MODIFIED)
                .build()),
            new CaseFlagDetail("id6", CaseFlagValue.builder()
                .flagCode(LACKING_CAPACITY.getFlagCode())
                .name(LACKING_CAPACITY.getName())
                .status("Active")
                .hearingRelevant(YesOrNo.NO)
                .dateTimeCreated(DATE_TIME_CREATED)
                .dateTimeModified(DATE_TIME_MODIFIED)
                .build()),
            new CaseFlagDetail("id7", CaseFlagValue.builder()
                .flagCode(LANGUAGE_INTERPRETER.getFlagCode())
                .name(LANGUAGE_INTERPRETER.getName())
                .status("Active")
                .hearingRelevant(YesOrNo.YES)
                .subTypeValue("French")
                .dateTimeCreated(DATE_TIME_CREATED)
                .dateTimeModified(DATE_TIME_MODIFIED)
                .build()),
            new CaseFlagDetail("id8", CaseFlagValue.builder()
                .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
                .name(SIGN_LANGUAGE_INTERPRETER.getName())
                .status("Active")
                .hearingRelevant(YesOrNo.YES)
                .subTypeValue("International Sign (IS)")
                .dateTimeCreated(DATE_TIME_CREATED)
                .dateTimeModified(DATE_TIME_MODIFIED)
                .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(
                new BailStrategicCaseFlag("applicant1", "", applicantCaseFlagDetails)));
        List<BailPartyFlagIdValue> fcsCaseFlag = List.of(
            new BailPartyFlagIdValue("fcsPartyId",
                          new BailStrategicCaseFlag(
                              "fcs3",
                              "",
                              List.of(new CaseFlagDetail(
                                  "id7",
                                  CaseFlagValue.builder()
                                      .flagCode(HEARING_LOOP.getFlagCode())
                                      .name(HEARING_LOOP.getName())
                                      .status("Active")
                                      .dateTimeCreated(DATE_TIME_CREATED)
                                      .dateTimeModified(DATE_TIME_MODIFIED)
                                      .hearingRelevant(YesOrNo.YES).build())))));
        when(bailCase.read(FCS_LEVEL_FLAGS))
            .thenReturn(Optional.of(fcsCaseFlag));

        Caseflags expected = Caseflags.builder()
            .flagAmendUrl(flagAmendUrl)
            .flags(List.of(
                PartyFlagsModel.builder()
                    .partyId("applicantPartyId")
                    .partyName("applicant1")
                    .flagId(LITIGATION_FRIEND.getFlagCode())
                    .flagStatus("Active")
                    .flagDescription("test comment")
                    .dateTimeCreated(DATE_TIME_CREATED_NO_NANOS)
                    .dateTimeModified(DATE_TIME_MODIFIED_NO_NANOS)
                    .build(),
                PartyFlagsModel.builder()
                    .partyId("applicantPartyId")
                    .partyName("applicant1")
                    .flagId(LACKING_CAPACITY.getFlagCode())
                    .flagStatus("Active")
                    .dateTimeCreated(DATE_TIME_CREATED_NO_NANOS)
                    .dateTimeModified(DATE_TIME_MODIFIED_NO_NANOS)
                    .build(),
                PartyFlagsModel.builder()
                    .partyId("applicantPartyId")
                    .partyName("applicant1")
                    .flagId(LANGUAGE_INTERPRETER.getFlagCode())
                    .flagStatus("Active")
                    .flagDescription("French")
                    .dateTimeCreated(DATE_TIME_CREATED_NO_NANOS)
                    .dateTimeModified(DATE_TIME_MODIFIED_NO_NANOS)
                    .build(),
                PartyFlagsModel.builder()
                    .partyId("applicantPartyId")
                    .partyName("applicant1")
                    .flagId(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
                    .flagStatus("Active")
                    .flagDescription("International Sign (IS)")
                    .dateTimeCreated(DATE_TIME_CREATED_NO_NANOS)
                    .dateTimeModified(DATE_TIME_MODIFIED_NO_NANOS)
                    .build(),
                PartyFlagsModel.builder()
                    .partyId("fcsPartyId")
                    .partyName("fcs3")
                    .flagId(HEARING_LOOP.getFlagCode())
                    .flagStatus("Active")
                    .dateTimeCreated(DATE_TIME_CREATED_NO_NANOS)
                    .dateTimeModified(DATE_TIME_MODIFIED_NO_NANOS)
                    .build()
            )).build();

        Caseflags actual = mapper.getCaseFlags(bailCase, caseReference);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void getCaseFlags_should_return_empty_case_flag_object() {

        when(bailCase.read(CASE_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(
                Arrays.asList(
                    new CaseFlagDetail("id3", CaseFlagValue.builder()
                        .flagCode(PRESIDENTIAL_PANEL.getFlagCode())
                        .status("Inactive")
                        .hearingRelevant(YesOrNo.YES)
                        .build())))));

        Caseflags result = mapper.getCaseFlags(bailCase, caseReference);
        assertNotNull(result);
        assertNull(result.getFlags());
    }

    @Test
    void getVulnerabilityFlag_should_return_true() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(VULNERABLE_USER.getFlagCode())
            .status("Active")
            .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getVulnerableFlag(bailCase));

        caseFlagDetails.add(new CaseFlagDetail("id2", CaseFlagValue.builder()
            .flagCode(UNACCOMPANIED_MINOR.getFlagCode())
            .status("Active")
            .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertTrue(mapper.getVulnerableFlag(bailCase));
    }

    @Test
    void getVulnerabilityFlag_should_return_false() {
        assertFalse(mapper.getVulnerableFlag(bailCase));

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(VULNERABLE_USER.getFlagCode())
            .status("Inactive")
            .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertFalse(mapper.getVulnerableFlag(bailCase));
    }

    @Test
    void getVulnerabilityDetails_should_return_details() {

        List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
        final String vulnerableUser = "Vulnerable user";
        final String unaccompaniedMinor = "Unaccompanied minor";
        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(VULNERABLE_USER.getFlagCode()).status("Active")
            .flagComment(vulnerableUser).build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertEquals(vulnerableUser, mapper.getVulnerableDetails(bailCase));

        caseFlagDetails.add(new CaseFlagDetail("id1", CaseFlagValue.builder()
            .flagCode(UNACCOMPANIED_MINOR.getFlagCode())
            .status("Active")
            .flagComment(unaccompaniedMinor)
            .build()));
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertEquals(vulnerableUser, mapper.getVulnerableDetails(bailCase));
    }

    @Test
    void getVulnerabilityDetails_should_return_null() {

        assertNull(mapper.getVulnerableDetails(bailCase));

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
        when(bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class))
            .thenReturn(Optional.of(new BailStrategicCaseFlag(caseFlagDetails)));

        assertNull(mapper.getVulnerableDetails(bailCase));
    }

}
