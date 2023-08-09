package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.ANONYMITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.AUDIO_VIDEO_EVIDENCE;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyFlagsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;

@Service
@RequiredArgsConstructor
public class CaseFlagsToServiceHearingValuesMapper {

    public String getPublicCaseName(AsylumCase asylumCase, String caseReference) {

        List<StrategicCaseFlag> caseFlags = asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        boolean hasActiveAnonymityFlag = hasOneOrMoreActiveFlagsOfType(caseFlags, List.of(ANONYMITY));

        if (hasActiveAnonymityFlag) {
            return caseReference;
        }

        return getAppellantDisplayName(asylumCase);
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

        return !(hasOneOrMoreActiveAppellantCaseFlags || hasActiveCaseFlag);
    }

    public PriorityType getHearingPriorityType(AsylumCase asylumCase) {
        List<StrategicCaseFlag> appellantCaseFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        boolean hasActiveAppellantFlag =
            hasOneOrMoreActiveFlagsOfType(appellantCaseFlags, List.of(UNACCOMPANIED_MINOR));
        List<StrategicCaseFlag> caseFlags = asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        boolean hasActiveCaseFlag = hasOneOrMoreActiveFlagsOfType(caseFlags, List.of(URGENT_CASE));

        if (hasActiveAppellantFlag || hasActiveCaseFlag) {
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
        List<StrategicCaseFlag> appellantCaseFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());
        if (!appellantCaseFlags.isEmpty()) {
            caseFlags.addAll(appellantCaseFlags);
        }
        Optional<List<IdValue<StrategicCaseFlag>>> caseFlagsOptional = asylumCase.read(WITNESS_LEVEL_FLAGS);
        caseFlagsOptional.ifPresent(idValues -> {
            List<StrategicCaseFlag> witnessCaseFlags = idValues
                .stream().map(IdValue::getValue).toList();
            if (!witnessCaseFlags.isEmpty()) {
                caseFlags.addAll(witnessCaseFlags);
            }
        });

        return !caseFlags.isEmpty() && hasOneOrMoreActiveFlagsOfType(
            caseFlags, List.of(SIGN_LANGUAGE_INTERPRETER, LANGUAGE_INTERPRETER)
        );
    }

    public Caseflags getCaseFlags(AsylumCase asylumCase) {
        List<PartyFlagsModel> flags = new ArrayList<>();
        Caseflags caseflags = Caseflags.builder().build();

        asylumCase.read(CASE_FLAGS, StrategicCaseFlag.class).ifPresent(flag -> {
            flags.addAll(buildCaseFlags(flag.getDetails(), flag.getPartyName()));
        });
        asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class).ifPresent(flag -> {
            flags.addAll(buildCaseFlags(flag.getDetails(), flag.getPartyName()));
        });
        Optional<List<IdValue<StrategicCaseFlag>>> caseFlagsOptional = asylumCase.read(WITNESS_LEVEL_FLAGS);
        caseFlagsOptional.ifPresent(idValues -> {
            List<StrategicCaseFlag> witnessCaseFlags = idValues
                .stream().map(IdValue::getValue).toList();
            if (!witnessCaseFlags.isEmpty()) {
                flags.addAll(
                    witnessCaseFlags.stream()
                        .map(witnessCaseFlag ->
                             buildCaseFlags(witnessCaseFlag.getDetails(), witnessCaseFlag.getPartyName()))
                        .flatMap(Collection::stream)
                        .toList());
            }
        });

        if (!flags.isEmpty()) {
            caseflags.setFlags(flags);
            String flagAmendUrl = "flagAmendUrl";
            caseflags.setFlagAmendUrl(flagAmendUrl);
        }

        return caseflags;
    }

    private List<PartyFlagsModel> buildCaseFlags(List<CaseFlagDetail> caseFlagDetails, String partyName) {
        if (caseFlagDetails != null) {
            return caseFlagDetails.stream().filter(detail -> {
                CaseFlagValue value = detail.getCaseFlagValue();

                return Objects.equals(value.getStatus(), "Active")
                    && YesOrNo.YES.equals(value.getHearingRelevant());
            }).map(detail -> PartyFlagsModel.builder()
                .partyId(partyName)
                .partyName(partyName)
                .flagId(detail.getId())
                .flagStatus(detail.getCaseFlagValue().getStatus())
                .flagDescription(detail.getCaseFlagValue().getName())
                .build()
            ).collect(Collectors.toList());
        }

        return Collections.emptyList();
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

    private String getAppellantDisplayName(AsylumCase asylumCase) {

        return asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElseGet(() -> {
            final String appellantGivenNames =
                asylumCase
                    .read(APPELLANT_GIVEN_NAMES, String.class).orElse(null);
            final String appellantFamilyName =
                asylumCase
                    .read(APPELLANT_FAMILY_NAME, String.class).orElse(null);
            return !(appellantGivenNames == null || appellantFamilyName == null)
                ? appellantGivenNames + " " + appellantFamilyName
                : null;
        });
    }
}
