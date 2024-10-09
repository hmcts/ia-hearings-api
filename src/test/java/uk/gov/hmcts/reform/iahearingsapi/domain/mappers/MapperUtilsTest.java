package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.S94B_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.JourneyType.REP;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapperUtilsTest {

    @Mock
    private AsylumCase asylumCase;

    @Test
    void getAppellantFullName_should_return_appellant_display_name_when_it_is_set() {

        String appellantDisplayName = "appellantDisplayName";
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class))
            .thenReturn(Optional.of(appellantDisplayName));

        assertEquals(appellantDisplayName, MapperUtils.getAppellantFullName(asylumCase));
    }

    @Test
    void getAppellantFullName_should_return_appellant_given_names_and_family_name() {

        String givenNames = "firstName secondName";
        String familyName = "familyName";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of(givenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of(familyName));

        assertEquals(givenNames + " " + familyName, MapperUtils.getAppellantFullName(asylumCase));
    }

    @Test
    void getAppellantFullName_should_return_null() {

        assertNull(MapperUtils.getAppellantFullName(asylumCase));
    }

    @Test
    void isAipJourney_should_return_true() {

        when(asylumCase.read(JOURNEY_TYPE, String.class)).thenReturn(Optional.of(AIP.getValue()));

        assertTrue(MapperUtils.isAipJourney(asylumCase));
    }

    @Test
    void isAipJourney_should_return_false() {

        assertFalse(MapperUtils.isAipJourney(asylumCase));
    }

    @Test
    void isRepJourney_should_return_true() {

        assertTrue(MapperUtils.isRepJourney(asylumCase));

        when(asylumCase.read(JOURNEY_TYPE, String.class)).thenReturn(Optional.of(REP.getValue()));

        assertTrue(MapperUtils.isRepJourney(asylumCase));
    }

    @Test
    void isRepJourney_should_return_false() {

        when(asylumCase.read(JOURNEY_TYPE, String.class)).thenReturn(Optional.of(AIP.getValue()));

        assertFalse(MapperUtils.isRepJourney(asylumCase));
    }

    @Test
    void isAppellantInUk_should_return_true() {

        assertTrue(MapperUtils.isAppellantInUk(asylumCase));

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(MapperUtils.isAppellantInUk(asylumCase));
    }

    @Test
    void isAppellantInUk_should_return_false() {

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(MapperUtils.isAppellantInUk(asylumCase));
    }

    @Test
    void isS94B_should_return_true() {

        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(MapperUtils.isS94B(asylumCase));
    }

    @Test
    void isS94B_should_return_false() {

        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(MapperUtils.isS94B(asylumCase));
    }

    @Test
    void hasSponsor_should_return_true() {

        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(MapperUtils.hasSponsor(asylumCase));
    }

    @Test
    void hasSponsor_should_return_false() {

        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(MapperUtils.hasSponsor(asylumCase));
    }

    @Test
    void isChangeOrganisationRequestPresent_should_return_true() {

        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
            .thenReturn(Optional.of(mock(ChangeOrganisationRequest.class)));

        assertTrue(MapperUtils.isChangeOrganisationRequestPresent(asylumCase));
    }

    @Test
    void isChangeOrganisationRequestPresent_should_return_false() {

        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
            .thenReturn(Optional.empty());

        assertFalse(MapperUtils.isChangeOrganisationRequestPresent(asylumCase));
    }

    @Test
    public void parseDateTimeStringWithoutNanos() {
        String inputDateTimeString = "2024-04-11T10:15:30.123Z";
        String expected = "2024-04-11T10:15:30";

        assertEquals(expected, MapperUtils.parseDateTimeStringWithoutNanos(inputDateTimeString));
    }

    @Test
    public void parseDateTimeStringWithoutNanos_NullInput() {
        assertNull(MapperUtils.parseDateTimeStringWithoutNanos(null));
    }

    @Test
    void isInternalCase_should_return_true() {

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(MapperUtils.isInternalCase(asylumCase));
    }

    @Test
    void isInternalCase_should_return_false() {

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(MapperUtils.isInternalCase(asylumCase));
    }

    @Test
    void isInternalCaseHasLegalRep_should_return_true() {

        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(MapperUtils.isInternalCaseHasLegalRep(asylumCase));
    }

    @Test
    void isInternalCaseHasLegalRep_should_return_false() {

        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertTrue(MapperUtils.isInternalCaseHasLegalRep(asylumCase));
    }
}
