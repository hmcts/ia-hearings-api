package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlagValue {

    String name;
    String status;
    String flagCode;
    String subTypeKey;
    String flagComment;
    String subTypeValue;
    YesOrNo hearingRelevant;
    String dateTimeCreated;
    String dateTimeModified;

}
