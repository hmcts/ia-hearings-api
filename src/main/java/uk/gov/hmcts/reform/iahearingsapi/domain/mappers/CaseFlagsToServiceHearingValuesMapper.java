package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.ANONYMITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.AUDIO_VIDEO_EVIDENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.DETAINED_INDIVIDUAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.EVIDENCE_GIVEN_IN_PRIVATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.FOREIGN_NATIONAL_OFFENDER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LACKING_CAPACITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.PRESIDENTIAL_PANEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.UNACCOMPANIED_MINOR;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.URGENT_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.VULNERABLE_USER;

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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CustodyStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyFlagsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;

@Service
@RequiredArgsConstructor
public class CaseFlagsToServiceHearingValuesMapper {

    private final CaseDataToServiceHearingValuesMapper caseDataMapper;
    private static final String caseLevelFlags = "Case level flags";
    private static final String caseLevelFlagsPartyID = "Caselevelflags";

    public String getPublicCaseName(AsylumCase asylumCase, String caseReference) {

        List<StrategicCaseFlag> caseFlags = asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        boolean hasActiveAnonymityFlag = hasOneOrMoreActiveFlagsOfType(caseFlags, List.of(ANONYMITY));

        if (hasActiveAnonymityFlag) {
            return caseReference;
        }

        return MapperUtils.getAppellantFullName(asylumCase);
    }

    public boolean getCaseAdditionalSecurityFlag(AsylumCase asylumCase) {
        List<StrategicCaseFlag> appellantCaseFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());

        return hasOneOrMoreActiveFlagsOfType(appellantCaseFlags,
            List.of(UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR, FOREIGN_NATIONAL_OFFENDER));
    }

    public boolean getAutoListFlag(AsylumCase asylumCase) {
        List<StrategicCaseFlag> appellantCaseFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        boolean isDecisionWithoutHearingAppeal = caseDataMapper.isDecisionWithoutHearingAppeal(asylumCase);
        List<StrategicCaseFlagType> appellantCaseFlagTypes = List.of(
            SIGN_LANGUAGE_INTERPRETER,
            FOREIGN_NATIONAL_OFFENDER,
            AUDIO_VIDEO_EVIDENCE,
            LITIGATION_FRIEND,
            LACKING_CAPACITY);
        boolean hasOneOrMoreActiveAppellantCaseFlags =
            hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, appellantCaseFlagTypes);
        List<StrategicCaseFlag> caseFlags = asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        boolean hasActiveCaseFlag = hasOneOrMoreActiveFlagsOfType(caseFlags, List.of(PRESIDENTIAL_PANEL));

        return !(hasOneOrMoreActiveAppellantCaseFlags || hasActiveCaseFlag || isDecisionWithoutHearingAppeal);
    }

    public PriorityType getHearingPriorityType(AsylumCase asylumCase) {
        List<StrategicCaseFlag> appellantCaseFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());

        Optional<List<PartyFlagIdValue>> witnessCaseFlagsOptional = asylumCase.read(WITNESS_LEVEL_FLAGS);
        List<StrategicCaseFlag> witnessCaseFlags = witnessCaseFlagsOptional
            .map(list -> list.stream().map(PartyFlagIdValue::getValue).collect(Collectors.toList()))
            .orElse(Collections.emptyList());

        List<StrategicCaseFlag> caseFlags = asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());

        boolean hasActiveAppellantFlag =
            hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, List.of(UNACCOMPANIED_MINOR));
        boolean hasActiveWitnessFlag =
            hasOneOrMoreActiveFlagsOfType(witnessCaseFlags, List.of(UNACCOMPANIED_MINOR));
        boolean hasActiveCaseFlag = hasOneOrMoreActiveFlagsOfType(caseFlags, List.of(URGENT_CASE));

        if (hasActiveAppellantFlag || hasActiveWitnessFlag || hasActiveCaseFlag) {
            return PriorityType.URGENT;
        }

        return PriorityType.STANDARD;
    }

    public String getListingComments(AsylumCase asylumCase) {
        String additionalInstructions = asylumCase.read(ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE, String.class)
            .orElse("");
        String listingComments = additionalInstructions.isBlank() ? "" : additionalInstructions + ";";

        String flagComment = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class).map(caseFlag -> {
            List<CaseFlagDetail> details = caseFlag.getDetails();
            if (details != null) {
                return details
                    .stream()
                    .filter(detail -> {
                        CaseFlagValue value = detail.getCaseFlagValue();
                        return Objects.equals(
                            UNACCEPTABLE_DISRUPTIVE_CUSTOMER_BEHAVIOUR.getFlagCode(),
                            value.getFlagCode())
                            && Objects.equals("Active", value.getStatus())
                            && value.getFlagComment() != null
                            && !value.getFlagComment().isBlank();
                    })
                    .map(detail -> "Customer behaviour: " + detail.getCaseFlagValue().getFlagComment() + ";")
                    .findAny().orElse("");
            }
            return "";
        }).orElse("");

        return flagComment.isBlank()
            ? listingComments
            : (listingComments.isBlank() ? flagComment : listingComments + flagComment);
    }

    public boolean getPrivateHearingRequiredFlag(AsylumCase asylumCase) {
        List<StrategicCaseFlag> appellantCaseFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        return hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, List.of(EVIDENCE_GIVEN_IN_PRIVATE));
    }

    public boolean getCaseInterpreterRequiredFlag(AsylumCase asylumCase) {
        List<StrategicCaseFlag> caseFlags = new ArrayList<>();
        List<StrategicCaseFlag> appellantFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        if (!appellantFlags.isEmpty()) {
            caseFlags.addAll(appellantFlags);
        }
        Optional<List<PartyFlagIdValue>> flagsOptional = asylumCase.read(WITNESS_LEVEL_FLAGS);
        flagsOptional.ifPresent(witnessFlagIdValues -> {
            List<StrategicCaseFlag> witnessFlags = witnessFlagIdValues
                .stream().map(PartyFlagIdValue::getValue).toList();
            if (!witnessFlags.isEmpty()) {
                caseFlags.addAll(witnessFlags);
            }
        });

        return !caseFlags.isEmpty() && hasOneOrMoreActiveFlagsOfType(
            caseFlags, List.of(SIGN_LANGUAGE_INTERPRETER, LANGUAGE_INTERPRETER)
        );
    }

    public Caseflags getCaseFlags(
        AsylumCase asylumCase, String caseReference) {

        Caseflags caseflags = Caseflags.builder().build();
        List<PartyFlagsModel> flags = new ArrayList<>();

        flags.addAll(getCaseLevelFlags(asylumCase));
        flags.addAll(getAppellantCaseFlags(asylumCase, caseDataMapper));
        flags.addAll(getWitnessCaseFlags(asylumCase));

        if (!flags.isEmpty()) {
            caseflags.setFlags(flags);
            caseflags.setFlagAmendUrl("/cases/case-details/" + caseReference + "#Case%20flags");
        }

        return caseflags;
    }

    public String getCustodyStatus(AsylumCase asylumCase) {
        List<StrategicCaseFlag> appellantCaseFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());

        if (hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, List.of(FOREIGN_NATIONAL_OFFENDER))) {
            return CustodyStatus.IN_CUSTODY.getValue();
        } else if (hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, List.of(DETAINED_INDIVIDUAL))) {
            return CustodyStatus.IN_DETENTION.getValue();
        }

        return null;
    }

    public boolean getVulnerableFlag(AsylumCase asylumCase) {
        List<StrategicCaseFlag> appellantCaseFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());

        return hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, List.of(VULNERABLE_USER, UNACCOMPANIED_MINOR));
    }

    public String getVulnerableDetails(AsylumCase asylumCase) {
        List<String> targetCaseFlagCodes = Stream.of(VULNERABLE_USER, UNACCOMPANIED_MINOR)
            .map(StrategicCaseFlagType::getFlagCode).toList();
        List<String> vulnerabilityDetails =  asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(StrategicCaseFlag::getDetails)
            .orElse(Collections.emptyList()).stream().filter(detail ->
                targetCaseFlagCodes.contains(detail.getCaseFlagValue().getFlagCode())
                    && Objects.equals("Active", detail.getCaseFlagValue().getStatus()))
            .map(details -> details.getCaseFlagValue().getFlagComment())
            .filter(flagComment -> flagComment != null && !flagComment.isBlank()).toList();

        return vulnerabilityDetails.isEmpty() ? null : String.join(";", vulnerabilityDetails);
    }

    public List<PartyFlagsModel> getCaseLevelFlags(AsylumCase asylumCase) {
        return asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class)
            .map(flag -> buildCaseFlags(flag.getDetails(), caseLevelFlagsPartyID, caseLevelFlags))
            .orElse(Collections.emptyList());
    }

    public List<PartyFlagsModel> getAppellantCaseFlags(
        AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {
        return asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(flag -> buildCaseFlags(
                flag.getDetails(),
                caseDataMapper.getAppellantPartyId(asylumCase),
                flag.getPartyName()))
            .orElse(Collections.emptyList());
    }

    public List<PartyFlagsModel> getWitnessCaseFlags(AsylumCase asylumCase) {
        Optional<List<PartyFlagIdValue>> flagsOptional = asylumCase.read(WITNESS_LEVEL_FLAGS);
        return flagsOptional.map(witnessFlagIdValues -> witnessFlagIdValues.stream()
            .map(witnessFlagIdValue -> buildCaseFlags(
                witnessFlagIdValue.getValue().getDetails(),
                witnessFlagIdValue.getPartyId(),
                witnessFlagIdValue.getValue().getPartyName()))
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

    private boolean hasOneOrMoreActiveFlagsOfType(
        List<StrategicCaseFlag> caseFlags, List<StrategicCaseFlagType> caseFlagTypes) {

        if (!caseFlags.isEmpty()) {
            List<String> flagCodes = caseFlagTypes.stream()
                .map(StrategicCaseFlagType::getFlagCode).toList();
            List<CaseFlagDetail> details = caseFlags.stream()
                .map(StrategicCaseFlag::getDetails)
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
