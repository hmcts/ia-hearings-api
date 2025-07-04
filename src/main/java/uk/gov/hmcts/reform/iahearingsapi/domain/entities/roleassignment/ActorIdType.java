package uk.gov.hmcts.reform.iahearingsapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ActorIdType {
    IDAM, CASEPARTY, @JsonEnumDefaultValue UNKNOWN
}
