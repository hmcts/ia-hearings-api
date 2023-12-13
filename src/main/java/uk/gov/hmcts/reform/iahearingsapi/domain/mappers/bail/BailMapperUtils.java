package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;


public class BailMapperUtils {

    private BailMapperUtils() {
    }

    public static String getApplicantFullName(BailCase bailCase) {

        return bailCase.read(APPLICANT_FULL_NAME, String.class).orElseGet(() -> {
            final String applicantGivenNames =
                bailCase.read(APPLICANT_GIVEN_NAMES, String.class).orElse(null);
            final String applicantFamilyName =
                bailCase.read(APPLICANT_FAMILY_NAME, String.class).orElse(null);
            return !(applicantGivenNames == null || applicantFamilyName == null)
                ? applicantGivenNames + " " + applicantFamilyName
                : null;
        });
    }

    public static boolean isLegallyRepresented(BailCase bailCase) {
        return bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)
            .map(isLegallyRepresented -> YES == isLegallyRepresented).orElse(false);
    }


    public static boolean isFcsSupporting(BailCase bailCase, BailCaseFieldDefinition fieldDefinition) {
        return bailCase.read(fieldDefinition, YesOrNo.class)
            .map(isSupporting -> YES == isSupporting).orElse(false);
    }

}
