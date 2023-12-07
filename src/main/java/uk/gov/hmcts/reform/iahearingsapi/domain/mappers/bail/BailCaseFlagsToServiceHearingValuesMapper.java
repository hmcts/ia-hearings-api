package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.VULNERABLE_USER;

@Service
@RequiredArgsConstructor
public class BailCaseFlagsToServiceHearingValuesMapper {

    private final BailCaseDataToServiceHearingValuesMapper caseDataMapper;


    public boolean getVulnerableFlag(BailCase bailCase) {
        List<BailStrategicCaseFlag> appellantCaseFlags =
            bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class)
                .map(List::of).orElse(Collections.emptyList());

        return hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, List.of(VULNERABLE_USER));
    }

    public String getVulnerableDetails(BailCase bailCase) {
        List<String> targetCaseFlagCodes = Stream.of(VULNERABLE_USER)
            .map(StrategicCaseFlagType::getFlagCode).toList();
        List<String> vulnerabilityDetails =  bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class)
            .map(BailStrategicCaseFlag::getDetails)
            .orElse(Collections.emptyList()).stream().filter(detail ->
                targetCaseFlagCodes.contains(detail.getCaseFlagValue().getFlagCode())
                    && Objects.equals("Active", detail.getCaseFlagValue().getStatus()))
            .map(details -> details.getCaseFlagValue().getFlagComment())
            .filter(flagComment -> flagComment != null && !flagComment.isBlank()).toList();

        return vulnerabilityDetails.isEmpty() ? null : String.join(";", vulnerabilityDetails);
    }

    private boolean hasOneOrMoreActiveFlagsOfType(
        List<BailStrategicCaseFlag> caseFlags, List<StrategicCaseFlagType> caseFlagTypes) {

        if (!caseFlags.isEmpty()) {
            List<String> flagCodes = caseFlagTypes.stream()
                .map(StrategicCaseFlagType::getFlagCode).toList();
            List<CaseFlagDetail> details = caseFlags.stream()
                .map(BailStrategicCaseFlag::getDetails)
                .flatMap(Collection::stream).toList();
            if (!details.isEmpty()) {
                return details.stream()
                    .map(detail ->
                         flagCodes.contains(detail.getCaseFlagValue().getFlagCode())
                             && Objects.equals(detail.getCaseFlagValue().getStatus(), "Active"))
                    .reduce(false, (accumulator, hasActiveFlag) -> accumulator || hasActiveFlag);
            }
        }

        return false;
    }
}
