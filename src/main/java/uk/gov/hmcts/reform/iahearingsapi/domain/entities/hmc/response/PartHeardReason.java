package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

@Getter
public enum PartHeardReason {

    NOT_BROUGHT("not-brought", "BFA1-appellant-not-brought"),
    COURT_CLOSURE("court-closure", "BFA1-court-closure"),
    EVIDENCE_CONTENTION("evidence-contention", "BFA1-evidence-contention"),
    EVIDENCE_REQUIRED("evidence-required", "BFA1-evidence-required"),
    AV_ISSUE("av-issue", "BFA1-av-issue"),
    NEW_ISSUE("new-issue", "BFA1-new-issue"),
    NO_JUDGE("no-judge", "BFA1-no-judge"),
    NOT_READY("not-ready", "BFA1-not-ready"),
    NO_ATT("no-att", "BFA1-noatt"),
    PUBLIC_HEALTH("public-health", "BFA1-public-health"),
    RA_NOT_MET("ra-not-met", "BFA1-ra-not-met");

    @JsonValue
    private final String label;

    @JsonValue
    private final String hmcValue;

    PartHeardReason(String label, String hmcValue) {
        this.label = label;
        this.hmcValue = hmcValue;
    }

    public static Optional<PartHeardReason> from(String value) {
        return stream(values())
                .filter(v -> Objects.equals(v.label, value))
                .findFirst();
    }
}
