package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CMR_HEARING_ID_LIST;

@Slf4j
@Component
public class CmrHearingIdListProcessor {
    public void processHearingIdList(AsylumCase asylumCase, String hearingId) {
        Optional<List<IdValue<String>>> hearingIdListOpt = asylumCase.read(CMR_HEARING_ID_LIST);
        final List<IdValue<String>> hearingIdList = hearingIdListOpt.orElse(emptyList());

        if (doesNotContainHearingId(hearingIdList, hearingId)) {
            List<IdValue<String>> newHearingIdList = appendToHearingIdList(hearingIdList, hearingId);
            asylumCase.write(CMR_HEARING_ID_LIST, newHearingIdList);
        }
    }

    private boolean doesNotContainHearingId(
        List<IdValue<String>> existingHearingIdList,
        String newHearingId
    ) {
        for (IdValue<String> existingHearingId : existingHearingIdList) {
            if (newHearingId.equals(existingHearingId.getValue())) {
                return false;
            }
        }

        return true;
    }

    private List<IdValue<String>> appendToHearingIdList(
        List<IdValue<String>> existingHearingIdList,
        String newHearingId
    ) {
        final List<IdValue<String>> allHearingIds = new ArrayList<>();

        int index = 1;
        for (IdValue<String> existingHearingId : existingHearingIdList) {
            allHearingIds.add(new IdValue<>(String.valueOf(index++), existingHearingId.getValue()));
        }

        allHearingIds.add(new IdValue<>(String.valueOf(index), newHearingId));

        return allHearingIds;
    }
}
