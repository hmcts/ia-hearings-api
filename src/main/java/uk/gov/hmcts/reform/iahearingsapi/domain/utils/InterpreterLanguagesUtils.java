package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_10;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_5;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_6;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_7;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_8;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_9;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_10;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_5;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_6;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_7;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_8;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_9;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4;

import java.util.List;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;

public class InterpreterLanguagesUtils {

    public static final List<AsylumCaseFieldDefinition> WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES = List.of(
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_5,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_6,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_7,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_8,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_9,
        WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_10
    );

    public static final List<AsylumCaseFieldDefinition> WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES = List.of(
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_5,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_6,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_7,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_8,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_9,
        WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_10
    );

    public static final List<BailCaseFieldDefinition> FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES = List.of(
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1,
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2,
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3,
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4
    );

    public static final List<BailCaseFieldDefinition> FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES = List.of(
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1,
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2,
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3,
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4
    );
}
