package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import java.util.Map;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CaseDataContent {

    private String caseReference;
    private Map<String, Object> data;
    private Map<String, Object> event;
    private String eventToken;
    private boolean ignoreWarning;
}
