package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.CASE_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.FCS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.ANONYMITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.EVIDENCE_GIVEN_IN_PRIVATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.UNACCOMPANIED_MINOR;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.URGENT_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.VULNERABLE_USER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.MapperUtils.parseDateTimeStringWithoutNanos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailPartyFlagIdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyFlagsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;

@Service
@RequiredArgsConstructor
public class BailCaseFlagsToServiceHearingValuesMapper {

    private static final String caseLevelFlags = "Case level flags";
    private static final String caseLevelFlagsPartyID = "Caselevelflags";
    private final BailCaseDataToServiceHearingValuesMapper caseDataMapper;

    public String getPublicCaseName(BailCase bailCase, String caseReference) {

        List<BailStrategicCaseFlag> caseFlags = bailCase.read(CASE_FLAGS, BailStrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        boolean hasActiveAnonymityFlag = hasOneOrMoreActiveFlagsOfType(caseFlags, List.of(ANONYMITY));

        if (hasActiveAnonymityFlag) {
            return caseReference;
        }

        return BailMapperUtils.getApplicantFullName(bailCase);
    }

    public PriorityType getHearingPriorityType(BailCase bailCase) {

        List<BailStrategicCaseFlag> caseFlags = bailCase.read(CASE_FLAGS, BailStrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());

        return hasOneOrMoreActiveFlagsOfType(caseFlags, List.of(URGENT_CASE))
            ? PriorityType.URGENT
            : PriorityType.STANDARD;
    }

    public boolean getPrivateHearingRequiredFlag(BailCase bailCase) {
        List<BailStrategicCaseFlag> appellantCaseFlags = bailCase
            .read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        return hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, List.of(EVIDENCE_GIVEN_IN_PRIVATE));
    }

    public boolean getCaseInterpreterRequiredFlag(BailCase bailCase) {
        List<BailStrategicCaseFlag> caseFlags = new ArrayList<>();
        List<BailStrategicCaseFlag> appellantFlags = bailCase
            .read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        if (!appellantFlags.isEmpty()) {
            caseFlags.addAll(appellantFlags);
        }
        Optional<List<BailPartyFlagIdValue>> flagsOptional = bailCase.read(FCS_LEVEL_FLAGS);
        flagsOptional.ifPresent(fcsFlagIdValues -> {
            List<BailStrategicCaseFlag> fcsFlags = fcsFlagIdValues
                .stream().map(BailPartyFlagIdValue::getValue).toList();
            if (!fcsFlags.isEmpty()) {
                caseFlags.addAll(fcsFlags);
            }
        });

        return !caseFlags.isEmpty() && hasOneOrMoreActiveFlagsOfType(
            caseFlags, List.of(SIGN_LANGUAGE_INTERPRETER, LANGUAGE_INTERPRETER)
        );
    }

    public Caseflags getCaseFlags(BailCase bailCase, String caseReference) {

        Caseflags caseflags = Caseflags.builder().build();
        List<PartyFlagsModel> flags = new ArrayList<>();

        flags.addAll(getCaseLevelFlags(bailCase));
        flags.addAll(getApplicantCaseFlags(bailCase, caseDataMapper));
        flags.addAll(getFcsCaseFlags(bailCase));

        if (!flags.isEmpty()) {
            caseflags.setFlags(flags);
            caseflags.setFlagAmendUrl("/cases/case-details/" + caseReference + "#Case%20flags");
        }

        return caseflags;
    }

    public boolean getVulnerableFlag(BailCase bailCase) {
        List<BailStrategicCaseFlag> appellantCaseFlags =
            bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class)
                .map(List::of).orElse(Collections.emptyList());

        return hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, List.of(VULNERABLE_USER, UNACCOMPANIED_MINOR));
    }

    public String getVulnerableDetails(BailCase bailCase) {
        List<String> targetCaseFlagCodes = Stream.of(VULNERABLE_USER, UNACCOMPANIED_MINOR)
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

    private List<PartyFlagsModel> getCaseLevelFlags(BailCase bailCase) {
        return bailCase.read(BailCaseFieldDefinition.CASE_FLAGS, BailStrategicCaseFlag.class)
            .map(flag -> buildCaseFlags(flag.getDetails(), caseLevelFlagsPartyID, caseLevelFlags))
            .orElse(Collections.emptyList());
    }

    private List<PartyFlagsModel> getApplicantCaseFlags(
        BailCase bailCase, BailCaseDataToServiceHearingValuesMapper caseDataMapper) {
        return bailCase.read(APPELLANT_LEVEL_FLAGS, BailStrategicCaseFlag.class)
            .map(flag -> buildCaseFlags(
                flag.getDetails(),
                caseDataMapper.getApplicantPartyId(bailCase),
                flag.getPartyName()))
            .orElse(Collections.emptyList());
    }

    private List<PartyFlagsModel> getFcsCaseFlags(BailCase bailCase) {
        Optional<List<BailPartyFlagIdValue>> flagsOptional = bailCase.read(FCS_LEVEL_FLAGS);
        return flagsOptional.map(fcsFlagIdValues -> fcsFlagIdValues.stream()
                .map(fcsFlagIdValue -> buildCaseFlags(
                    fcsFlagIdValue.getValue().getDetails(),
                    fcsFlagIdValue.getPartyId(),
                    fcsFlagIdValue.getValue().getPartyName()))
                .flatMap(Collection::stream).toList())
            .orElse(Collections.emptyList());
    }

    private List<PartyFlagsModel> buildCaseFlags(
        List<CaseFlagDetail> caseFlagDetails, String partyId, String partyName) {

        return caseFlagDetails.stream()
            .filter(detail -> Objects.equals(detail.getCaseFlagValue().getStatus(), "Active"))
            .map(detail -> PartyFlagsModel.builder()
                .partyId(partyId)
                .partyName(partyName)
                .flagId(detail.getCaseFlagValue().getFlagCode())
                .flagStatus(detail.getCaseFlagValue().getStatus())
                .flagDescription(getFlagDescription(detail))
                .dateTimeCreated(parseDateTimeStringWithoutNanos(detail.getCaseFlagValue().getDateTimeCreated()))
                .dateTimeModified(parseDateTimeStringWithoutNanos(detail.getCaseFlagValue().getDateTimeModified()))
                .build()).collect(Collectors.toList());
    }

    private static String getFlagDescription(CaseFlagDetail detail) {
        String flagCode = detail.getCaseFlagValue().getFlagCode();
        if (LANGUAGE_INTERPRETER.getFlagCode().equals(flagCode)
            || SIGN_LANGUAGE_INTERPRETER.getFlagCode().equals(flagCode)) {
            return detail.getCaseFlagValue().getSubTypeValue();
        }

        return detail.getCaseFlagValue().getFlagComment();
    }
}
