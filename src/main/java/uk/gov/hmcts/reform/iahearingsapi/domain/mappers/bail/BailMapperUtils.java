package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.*;

public class BailMapperUtils {

    private BailMapperUtils() {
    }

    public static String getApplicantFullName(BailCase bailCase) {

        return bailCase.read(APPLICANT_FULL_NAME, String.class).orElseGet(() -> {
            final String applicantGivenNames =
                bailCase
                    .read(APPLICANT_GIVEN_NAMES, String.class).orElse(null);
            final String applicantFamilyName =
                bailCase
                    .read(APPLICANT_FAMILY_NAME, String.class).orElse(null);
            return !(applicantGivenNames == null || applicantFamilyName == null)
                ? applicantGivenNames + " " + applicantFamilyName
                : null;
        });
    }
}
