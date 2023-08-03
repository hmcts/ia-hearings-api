package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StrategicCaseFlagType {

    URGENT_CASE("CF0007", "Urgent case"),
    PRESIDENTIAL_PANEL("CF0011", "Presidential panel"),
    ANONYMITY("CF0012", "RRO (Restricted Reporting Order / Anonymisation)"),
    UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR("PF0007", "Unacceptable/disruptive customer behaviour"),
    UNACCOMPANIED_MINOR("PF0013", "Unaccompanied minor"),
    AUDIO_VIDEO_EVIDENCE("PF0014", "Audio/Video Evidence"),
    LANGUAGE_INTERPRETER("PF0015", "Language Interpreter"),
    LITIGATION_FRIEND("PF0017", "Litigation friend"),
    LACKING_CAPACITY("PF0018", "Lacking capacity"),
    STEP_FREE_WHEELCHAIR_ACCESS("RA0019", "Step free / wheelchair access"),
    SIGN_LANGUAGE_INTERPRETER("RA0042", "Sign Language Interpreter"),
    HEARING_LOOP("RA0043", "Hearing loop (hearing enhancement system)"),
    EVIDENCE_GIVEN_IN_PRIVATE("SM0004", "Evidence given in private");

    @JsonValue
    private final String flagCode;
    private final String name;

    StrategicCaseFlagType(String flagCode, String name) {
        this.flagCode = flagCode;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFlagCode() {
        return flagCode;
    }

    @Override
    public String toString() {
        return getFlagCode();
    }
}
