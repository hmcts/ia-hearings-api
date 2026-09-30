package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MemberType {

    JUDGE("JUDGE"),
    PANEL_MEMBER("PANEL_MEMBER");

    @JsonValue
    private final String memberType;

    MemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getMemberType() {
        return memberType;
    }
}
