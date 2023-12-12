package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BailMapperUtilsTest {

    @Mock
    private BailCase bailCase;

    @Test
    void getApplicantFullName_should_return_appellant_display_name_when_it_is_set() {

        String appellantDisplayName = "appellantDisplayName";
        when(bailCase.read(APPLICANT_FULL_NAME, String.class))
            .thenReturn(Optional.of(appellantDisplayName));

        assertEquals(appellantDisplayName, BailMapperUtils.getApplicantFullName(bailCase));
    }

    @Test
    void getApplicantFullName_should_return_appellant_given_names_and_family_name() {

        String givenNames = "firstName secondName";
        String familyName = "familyName";
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of(givenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of(familyName));

        assertEquals(givenNames + " " + familyName, BailMapperUtils.getApplicantFullName(bailCase));
    }

    @Test
    void getApplicantFullName_should_return_null() {

        assertNull(BailMapperUtils.getApplicantFullName(bailCase));
    }

    @Test
    void isLegallyRepresented_should_return_true() {

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(BailMapperUtils.isLegallyRepresented(bailCase));
    }

    @Test
    void isLegallyRepresented_should_return_false() {

        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(BailMapperUtils.isLegallyRepresented(bailCase));
    }

    @Test
    void isFcsSupporting_should_return_true() {

        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(BailMapperUtils.isFcsSupporting(bailCase, HAS_FINANCIAL_COND_SUPPORTER));
    }

    @Test
    void isFcsSupporting_should_return_false() {

        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(BailMapperUtils.isFcsSupporting(bailCase, HAS_FINANCIAL_COND_SUPPORTER));
    }
}
