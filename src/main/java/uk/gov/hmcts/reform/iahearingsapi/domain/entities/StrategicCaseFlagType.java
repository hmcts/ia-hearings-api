package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StrategicCaseFlagType {

    URGENT_CASE("CF0007", "Urgent case", false),
    PRESIDENTIAL_PANEL("CF0011", "Presidential panel", true),
    ANONYMITY("CF0012", "RRO (Restricted Reporting Order / Anonymisation)", true),
    UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR(
        "PF0007", "Unacceptable/disruptive customer behaviour", true),
    FOREIGN_NATIONAL_OFFENDER("PF0012", "Foreign national offender", true),
    UNACCOMPANIED_MINOR("PF0013", "Unaccompanied minor", false),
    AUDIO_VIDEO_EVIDENCE("PF0014", "Audio/Video Evidence", true),
    LANGUAGE_INTERPRETER("PF0015", "Language Interpreter", true),
    LITIGATION_FRIEND("PF0017", "Litigation friend", true),
    LACKING_CAPACITY("PF0018", "Lacking capacity", false),
    STEP_FREE_WHEELCHAIR_ACCESS("RA0019", "Step free / wheelchair access", true),
    SIGN_LANGUAGE_INTERPRETER("RA0042", "Sign Language Interpreter", true),
    HEARING_LOOP("RA0043", "Hearing loop (hearing enhancement system)", true),
    EVIDENCE_GIVEN_IN_PRIVATE("SM0004", "Evidence given in private", true);

    @JsonValue
    private final String flagCode;
    private final String name;
    private final boolean hearingRelevant;

    StrategicCaseFlagType(String flagCode, String name, boolean hearingRelevant) {
        this.flagCode = flagCode;
        this.name = name;
        this.hearingRelevant = hearingRelevant;
    }

    public String getName() {
        return name;
    }

    public String getFlagCode() {
        return flagCode;
    }

    public boolean isHearingRelevant() {
        return hearingRelevant;
    }

    @Override
    public String toString() {
        return getFlagCode();
    }
}
