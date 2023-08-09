package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;

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

}
