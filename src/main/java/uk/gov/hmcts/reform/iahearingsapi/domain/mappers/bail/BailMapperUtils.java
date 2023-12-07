package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;


public class BailMapperUtils {

    private BailMapperUtils() {
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
