package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@Component
public class LanguageAndAdjustmentsMapper {

    public static final Set<String> LANGUAGE_CASE_FLAG_CODES = Stream.of(
        LANGUAGE_INTERPRETER, SIGN_LANGUAGE_INTERPRETER)
        .map(StrategicCaseFlagType::getFlagCode).collect(Collectors.toSet());
    public static final Set<String> REASONABLE_ADJUSTMENT_PREFIXES = Set.of("RA", "SM");
    public static final String INTERPRETER = "Interpreter: ";
    private static final String ACTIVE = "Active";
    private static final String PARTY_ROLE_APPELLANT = "APEL";
    private static final String PARTY_ROLE_WITNESS = "WITN";

    public PartyDetailsModel processPartyCaseFlags(AsylumCase asylumCase, PartyDetailsModel partyDetails) {

        IndividualDetailsModel individualDetails = partyDetails.getIndividualDetails();

        if (individualDetails != null) {
            String partyRole = partyDetails.getPartyRole();

            List<StrategicCaseFlag> caseFlags = StringUtils.equals(partyRole, PARTY_ROLE_APPELLANT)
                ? getAppellantCaseFlags(asylumCase)
                : StringUtils.equals(partyRole, PARTY_ROLE_WITNESS)
                ? getWitnessCaseFlags(asylumCase, partyDetails.getPartyID())
                : Collections.emptyList();

            List<CaseFlagDetail> activeCaseFlagDetails = filterForActiveCaseFlagDetails(caseFlags);

            List<CaseFlagDetail> languageFlags = new ArrayList<>();
            List<CaseFlagDetail> reasonableAdjustmentsFlags = new ArrayList<>();

            separateLanguageAndReasonableAdjustmentFlags(activeCaseFlagDetails,
                                                         languageFlags,
                                                         reasonableAdjustmentsFlags);

            List<CaseFlagDetail> sortedLanguageFlags = sortLanguageFlagsByCode(languageFlags);

            List<CaseFlagDetail> secondLanguageFlags = new ArrayList<>(Collections.emptyList());
            String interpreterLanguage = extractInterpreterLanguageField(sortedLanguageFlags, secondLanguageFlags);

            individualDetails.setInterpreterLanguage(interpreterLanguage);

            List<String> otherLanguages = buildOtherLanguagesField(secondLanguageFlags);
            List<String> reasonableAdjustmentsComments = buildReasonableAdjustmentsFlagComments(
                reasonableAdjustmentsFlags);

            String otherReasonableAdjustments = Stream.concat(otherLanguages.stream(), reasonableAdjustmentsComments
                .stream())
                .collect(Collectors.joining("; "))
                .trim();

            List<String> reasonableAdjustments = reasonableAdjustmentsFlags.stream()
                .map(flag -> flag.getCaseFlagValue().getFlagCode()).toList();

            if (individualDetails.getReasonableAdjustments() != null) {
                individualDetails.getReasonableAdjustments().addAll(reasonableAdjustments);
            } else {
                individualDetails.setReasonableAdjustments(reasonableAdjustments);
            }

            if (!otherReasonableAdjustments.isEmpty()) {
                individualDetails.setOtherReasonableAdjustmentDetails(otherReasonableAdjustments + ";");
            }
        }

        return partyDetails;
    }

    private void separateLanguageAndReasonableAdjustmentFlags(List<CaseFlagDetail> activeCaseFlagDetails,
                                                              List<CaseFlagDetail> languageFlags,
                                                              List<CaseFlagDetail> reasonableAdjustmentsFlags) {
        activeCaseFlagDetails
            .forEach(flagDetail -> {
                if (isLanguageCaseFlag(flagDetail)) {
                    languageFlags.add(flagDetail);
                } else if (isReasonableAdjustmentFlag(flagDetail)) {
                    reasonableAdjustmentsFlags.add(flagDetail);
                }
            });
    }

    private List<CaseFlagDetail> sortLanguageFlagsByCode(List<CaseFlagDetail> languageFlags) {
        return languageFlags.stream()
            .sorted(Comparator.comparing(
                detail -> detail.getCaseFlagValue().getSubTypeKey(),
                Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    /**
     * Translate list of language flags into list of values for "otherReasonableAdjustmentsDetails" field.
     * @param otherLanguagesFlags All language flags without code or extra ones with code
     * @return List of equivalent strings in the format "Interpreter: TheLanguage"
     */
    private List<String> buildOtherLanguagesField(List<CaseFlagDetail> otherLanguagesFlags) {
        return otherLanguagesFlags.stream()
            .map(flag -> INTERPRETER + flag.getCaseFlagValue().getSubTypeValue())
            .collect(Collectors.toList());
    }

    /**
     * Translate list of reasonable adjustments flags comments into list of values for "reasonableAdjustments" field.
     * @param reasonableAdjustmentFlags Flags whose code start with "RA****" or "SM****"
     * @return List of equivalent strings that'll populate "reasonableAdjustments" in the format
     *     "Case flag name: the comment of the case flag"
     */
    private List<String> buildReasonableAdjustmentsFlagComments(List<CaseFlagDetail> reasonableAdjustmentFlags) {
        return reasonableAdjustmentFlags.stream()
            .filter(flag -> flag.getCaseFlagValue().getFlagComment() != null)
            .map(flag -> flag.getCaseFlagValue().getName() + ": " + flag.getCaseFlagValue().getFlagComment())
            .collect(Collectors.toList());
    }

    private boolean isLanguageCaseFlag(CaseFlagDetail detail) {
        return LANGUAGE_CASE_FLAG_CODES.contains(detail.getCaseFlagValue().getFlagCode());
    }

    private boolean isReasonableAdjustmentFlag(CaseFlagDetail detail) {
        return REASONABLE_ADJUSTMENT_PREFIXES.contains(detail.getCaseFlagValue().getFlagCode().substring(0,2));
    }

    private List<StrategicCaseFlag> getAppellantCaseFlags(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(Lists::newArrayList).orElse(new ArrayList<>());
    }

    private List<StrategicCaseFlag> getWitnessCaseFlags(AsylumCase asylumCase, String partyId) {
        List<StrategicCaseFlag> witnessCaseFlags = new ArrayList<>();

        Optional<List<PartyFlagIdValue>> caseFlagsOptional = asylumCase.read(WITNESS_LEVEL_FLAGS);

        caseFlagsOptional.ifPresent(witnessFlagIdValues -> {
            List<StrategicCaseFlag> caseFlags = witnessFlagIdValues
                .stream()
                .filter(partyFlagIdValue -> partyFlagIdValue.getPartyId().equals(partyId))
                .map(PartyFlagIdValue::getValue)
                .toList();
            if (!caseFlags.isEmpty()) {
                witnessCaseFlags.addAll(caseFlags);
            }
        });

        return witnessCaseFlags;
    }

    private List<CaseFlagDetail> filterForActiveCaseFlagDetails(List<StrategicCaseFlag> strategicCaseFlags) {
        return strategicCaseFlags
            .stream()
            .map(StrategicCaseFlag::getDetails)
            .flatMap(Collection::stream)
            .filter(detail -> Objects.equals(ACTIVE, detail.getCaseFlagValue().getStatus()))
            .toList();
    }

    /**
     * Find one eligible case flag to populate interpreterLanguage and add the remainder to a list
     * that will be used to populate otherReasonableAdjustmentDetails.
     * @param sortedLanguageFlags language flags sorted to have the ones with language-code at the top
     * @param otherLanguageFlags collection initially empty and to be filled with the remaining flags
     * @return the value to be used to populate interpreterLanguage
     */
    private String extractInterpreterLanguageField(List<CaseFlagDetail> sortedLanguageFlags,
                                                   List<CaseFlagDetail> otherLanguageFlags) {
        CaseFlagDetail interpreterLanguageFlag = null;

        int i = 0;
        while (i < sortedLanguageFlags.size()) {
            CaseFlagDetail flag = sortedLanguageFlags.get(i);

            boolean isSelectedLanguage = flag.getCaseFlagValue().getSubTypeKey() != null;
            if (interpreterLanguageFlag == null && isSelectedLanguage) {
                interpreterLanguageFlag = flag;
            } else {
                otherLanguageFlags.add(flag);
            }
            i++;
        }

        if (interpreterLanguageFlag == null) {
            return null;
        }

        return interpreterLanguageFlag.getCaseFlagValue().getSubTypeKey();
    }
}
