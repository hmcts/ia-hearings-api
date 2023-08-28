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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;

@Component
public class LanguageAndAdjustmentsMapper {

    public static final Set<String> LANGUAGE_CASE_FLAG_CODES = Stream.of(
        LANGUAGE_INTERPRETER, SIGN_LANGUAGE_INTERPRETER)
        .map(StrategicCaseFlagType::getFlagCode).collect(Collectors.toSet());
    public static final Set<String> REASONABLE_ADJUSTMENT_PREFIXES = Set.of("RA", "SM");

    public static final String INTERPRETER_LANGUAGE = "interpreterLanguage";
    public static final String OTHER_REASONABLE_ADJUSTMENTS_DETAILS = "otherReasonableAdjustmentsDetails";
    public static final String REASONABLE_ADJUSTMENTS = "reasonableAdjustments";

    public static final String INTERPRETER = "Interpreter: ";
    private static final String ACTIVE = "Active";

    /**
     * Method to extract the interpreter language, other reasonable adjustments details and reasonable adjustments.
     * These three fields are extrapolated by this same method, as they don't relate to the three hearing values in
     * a 1:1 way.
     * Interpreter Language or Sign Language Interpreter flags: the first one with code (e.g. "deu") populates
     * the "interpreterLanguage" value, any additional one with code will populate the
     * "otherReasonableAdjustmentsDetails" value, any manually typed one (without the "code") will add to
     * "otherReasonableAdjustmentsDetails" too.
     * Reasonable Adjustment flags (with flagCode "RA****" or "SM****") except Sign Language Interpreter: the codes
     * of each will populate the "reasonableAdjustments" value, the comment of each, if present, will add to the
     * "otherReasonableAdjustmentsDetails" value.
     * @param asylumCase The asylum case queried for a hearing.
     * @return           A map with the three values to be written in the response to HMC.
     */
    public Map<String, List<String>> getLanguageAndAdjustmentsFields(AsylumCase asylumCase) {
        Map<String, List<String>> fields = new HashMap<>();

        List<CaseFlagDetail> caseFlags = filterForActiveCaseFlagDetails(getAppellantAndWitnessCaseFlags(asylumCase));

        List<CaseFlagDetail> languageFlags = new ArrayList<>();
        List<CaseFlagDetail> reasonableAdjustmentsFlags = new ArrayList<>();

        caseFlags
            .forEach(flagDetail -> {
                if (isLanguageCaseFlag(flagDetail)) {
                    languageFlags.add(flagDetail);
                } else if (isReasonableAdjustmentFlag(flagDetail)) {
                    reasonableAdjustmentsFlags.add(flagDetail);
                }
            });

        // get the languageCaseFlags sorted to have the ones with code above the ones without
        List<CaseFlagDetail> sortedLanguageFlags = languageFlags.stream()
            .sorted(Comparator.comparing(
                detail -> detail.getCaseFlagValue().getSubTypeKey(),
                Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        // extract one flag with code to populate "interpreterLanguage" and group all the others with code
        // and the ones without code to populate "otherReasonableAdjustmentsDetails"
        List<CaseFlagDetail> otherLanguageFlags = new ArrayList<>(Collections.emptyList());

        String interpreterLanguage = extractInterpreterLanguageField(sortedLanguageFlags, otherLanguageFlags);

        List<String> otherLanguages = buildOtherLanguagesField(otherLanguageFlags);
        List<String> reasonableAdjustmentsComments = buildReasonableAdjustmentsFlagComments(reasonableAdjustmentsFlags);

        List<String> otherReasonableAdjustments = Stream.concat(otherLanguages.stream(), reasonableAdjustmentsComments
                .stream()).collect(Collectors.toList());

        List<String> reasonableAdjustments = reasonableAdjustmentsFlags.stream()
            .map(flag -> flag.getCaseFlagValue().getFlagCode()).toList();

        fields.put(INTERPRETER_LANGUAGE, Collections.singletonList(interpreterLanguage));
        fields.put(OTHER_REASONABLE_ADJUSTMENTS_DETAILS, otherReasonableAdjustments);
        fields.put(REASONABLE_ADJUSTMENTS, reasonableAdjustments);

        return fields;
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

    private List<StrategicCaseFlag> getAppellantAndWitnessCaseFlags(AsylumCase asylumCase) {
        List<StrategicCaseFlag> caseFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(Lists::newArrayList).orElse(new ArrayList<>());

        Optional<List<IdValue<StrategicCaseFlag>>> caseFlagsOptional = asylumCase.read(WITNESS_LEVEL_FLAGS);
        caseFlagsOptional.ifPresent(idValues -> {
            List<StrategicCaseFlag> witnessCaseFlags = idValues
                .stream().map(IdValue::getValue).toList();
            if (!witnessCaseFlags.isEmpty()) {
                caseFlags.addAll(witnessCaseFlags);
            }
        });

        return caseFlags;
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

            if (interpreterLanguageFlag == null && flag.getCaseFlagValue().getSubTypeKey() != null) {
                interpreterLanguageFlag = flag;
            } else {
                otherLanguageFlags.add(flag);
            }
            i++;
        }

        return interpreterLanguageFlag != null
            ? interpreterLanguageFlag.getCaseFlagValue().getSubTypeKey()
            : "";
    }
}