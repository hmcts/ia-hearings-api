package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

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

import java.util.Objects;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

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

    public static boolean isAipJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, String.class)
            .map(journeyType -> Objects.equals(AIP.getValue(), journeyType)).orElse(false);
    }

    public static boolean isRepJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, String.class)
            .map(journeyType -> Objects.equals(REP.getValue(), journeyType)).orElse(true);
    }

    // ChangeOrganisationRequest is present when the case is in between representations
    public static boolean isChangeOrganisationRequestPresent(AsylumCase asylumCase) {
        return asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class).isPresent();
    }

    public static boolean isAppellantInUk(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
            .map(inUk -> YesOrNo.YES == inUk).orElse(true);
    }

    public static boolean isS94B(AsylumCase asylumCase) {
        return asylumCase.read(S94B_STATUS, YesOrNo.class)
            .map(s94bStatus -> YesOrNo.YES == s94bStatus).orElse(false);
    }

    public static boolean hasSponsor(AsylumCase asylumCase) {
        return asylumCase.read(HAS_SPONSOR, YesOrNo.class).map(sponsor -> YesOrNo.YES == sponsor).orElse(false);
    }

    public static String parseDateTimeStringWithoutNanos(String dateTimeString) {
        if (dateTimeString == null) {
            return null;
        }

        return dateTimeString.substring(0, Math.min(dateTimeString.length(), 19));
    }

    public static boolean isInternalCase(AsylumCase asylumCase) {
        return (asylumCase.read(IS_ADMIN, YesOrNo.class)).map(isAdmin -> YesOrNo.YES == isAdmin).orElse(false);
    }

    public static boolean isInternalCaseHasLegalRep(AsylumCase asylumCase) {
        return (asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class))
            .map(isAip -> YesOrNo.NO == isAip).orElse(false);
    }
}
