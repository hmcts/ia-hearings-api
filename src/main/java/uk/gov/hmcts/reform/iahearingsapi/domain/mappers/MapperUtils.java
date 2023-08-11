package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;

public class MapperUtils {

    private MapperUtils() {
    }

    public static String getAppellantFullName(AsylumCase asylumCase) {

        return asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElseGet(() -> {
            final String appellantGivenNames =
                asylumCase
                    .read(APPELLANT_GIVEN_NAMES, String.class).orElse(null);
            final String appellantFamilyName =
                asylumCase
                    .read(APPELLANT_FAMILY_NAME, String.class).orElse(null);
            return !(appellantGivenNames == null || appellantFamilyName == null)
                ? appellantGivenNames + " " + appellantFamilyName
                : null;
        });
    }
}
