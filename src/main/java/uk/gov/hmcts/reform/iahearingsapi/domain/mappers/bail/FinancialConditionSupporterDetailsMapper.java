package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_3;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_4;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_1_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_EMAIL_ADDRESS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_FAMILY_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_MOBILE_NUMBER_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_TELEPHONE_NUMBER_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_EMAIL_ADDRESS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_FAMILY_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_MOBILE_NUMBER_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_TELEPHONE_NUMBER_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_EMAIL_ADDRESS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_FAMILY_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_MOBILE_NUMBER_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_TELEPHONE_NUMBER_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_EMAIL_ADDRESS_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_FAMILY_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_MOBILE_NUMBER_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_TELEPHONE_NUMBER_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterBookingStatus.NOT_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper.appendBookingStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.InterpreterLanguagesUtils.FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.InterpreterLanguagesUtils.FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES;

@Component
@AllArgsConstructor
public class FinancialConditionSupporterDetailsMapper {

    public static final List<BailCaseFieldDefinition> FCS_N_HAS_SUPPORT_FIELD = List.of(
        HAS_FINANCIAL_COND_SUPPORTER,
        HAS_FINANCIAL_COND_SUPPORTER_2,
        HAS_FINANCIAL_COND_SUPPORTER_3,
        HAS_FINANCIAL_COND_SUPPORTER_4
    );

    public static final List<BailCaseFieldDefinition> FCS_N_PARTY_ID_FIELD = List.of(
        SUPPORTER_1_PARTY_ID,
        SUPPORTER_2_PARTY_ID,
        SUPPORTER_3_PARTY_ID,
        SUPPORTER_4_PARTY_ID
    );

    public static final List<BailCaseFieldDefinition> FCS_N_GIVEN_NAME_FIELD = List.of(
        SUPPORTER_GIVEN_NAMES,
        SUPPORTER_2_GIVEN_NAMES,
        SUPPORTER_3_GIVEN_NAMES,
        SUPPORTER_4_GIVEN_NAMES
    );

    public static final List<BailCaseFieldDefinition> FCS_N_FAMILY_NAME_FIELD = List.of(
        SUPPORTER_FAMILY_NAMES,
        SUPPORTER_2_FAMILY_NAMES,
        SUPPORTER_3_FAMILY_NAMES,
        SUPPORTER_4_FAMILY_NAMES
    );

    public static final List<BailCaseFieldDefinition> FCS_N_EMAIL_FIELD = List.of(
        SUPPORTER_EMAIL_ADDRESS_1,
        SUPPORTER_2_EMAIL_ADDRESS_1,
        SUPPORTER_3_EMAIL_ADDRESS_1,
        SUPPORTER_4_EMAIL_ADDRESS_1
    );

    public static final List<BailCaseFieldDefinition> FCS_N_MOBILE_PHONE_FIELD = List.of(
        SUPPORTER_MOBILE_NUMBER_1,
        SUPPORTER_2_MOBILE_NUMBER_1,
        SUPPORTER_3_MOBILE_NUMBER_1,
        SUPPORTER_4_MOBILE_NUMBER_1
    );

    public static final List<BailCaseFieldDefinition> FCS_N_TELEPHONE_FIELD = List.of(
        SUPPORTER_TELEPHONE_NUMBER_1,
        SUPPORTER_2_TELEPHONE_NUMBER_1,
        SUPPORTER_3_TELEPHONE_NUMBER_1,
        SUPPORTER_4_TELEPHONE_NUMBER_1
    );

    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    public List<PartyDetailsModel> map(BailCase bailCase, BailCaseDataToServiceHearingValuesMapper caseDataMapper) {

        List<PartyDetailsModel> fcsDetailsList = new ArrayList<>();

        int i = 0;
        while (i < 4) {
            if (BailMapperUtils.isFcsSupporting(bailCase, FCS_N_HAS_SUPPORT_FIELD.get(i))) {
                PartyDetailsModel fcsPartyDetailsModel = PartyDetailsModel.builder()
                    .partyID(caseDataMapper.getStringValueByDefinition(bailCase, FCS_N_PARTY_ID_FIELD.get(i)))
                    .partyType(PartyType.IND.getPartyType())
                    .partyRole("FINS")
                    .individualDetails(
                        IndividualDetailsModel.builder()
                            .firstName(caseDataMapper
                                           .getStringValueByDefinition(bailCase, FCS_N_GIVEN_NAME_FIELD.get(i)))
                            .lastName(caseDataMapper
                                          .getStringValueByDefinition(bailCase, FCS_N_FAMILY_NAME_FIELD.get(i)))
                            .preferredHearingChannel(caseDataMapper.getHearingChannel(bailCase))
                            .hearingChannelEmail(
                                caseDataMapper.getHearingChannelEmailPhone(bailCase, FCS_N_EMAIL_FIELD.get(i)))
                            .hearingChannelPhone(fetchHearingChannelPhone(bailCase, caseDataMapper, i))
                            .build())
                    .build();

                languageAndAdjustmentsMapper.processBailPartyCaseFlags(bailCase, fcsPartyDetailsModel);

                fcsDetailsList.add(fcsPartyDetailsModel);

                appendFcsBookingStatus(bailCase, fcsPartyDetailsModel, i);
            }
            i++;
        }

        return fcsDetailsList;
    }

    private List<String> fetchHearingChannelPhone(BailCase bailCase,
                                                  BailCaseDataToServiceHearingValuesMapper caseDataMapper,
                                                  int index) {
        List<String> phoneNumber =
            caseDataMapper.getHearingChannelEmailPhone(bailCase, FCS_N_MOBILE_PHONE_FIELD.get(index));

        if (phoneNumber.isEmpty()) {
            return caseDataMapper.getHearingChannelEmailPhone(bailCase, FCS_N_TELEPHONE_FIELD.get(index));
        }
        return phoneNumber;
    }

    private void appendFcsBookingStatus(BailCase bailCase,
                                        PartyDetailsModel fcsPartyDetailsModel,
                                        int index) {

        Optional<InterpreterBookingStatus> spokenBookingStatus = bailCase
            .read(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUSES.get(index), InterpreterBookingStatus.class)
            .filter(interpreterBookingStatus -> !interpreterBookingStatus.equals(NOT_REQUESTED));

        Optional<InterpreterBookingStatus> signBookingStatus = bailCase
            .read(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUSES.get(index), InterpreterBookingStatus.class)
            .filter(interpreterBookingStatus -> !interpreterBookingStatus.equals(NOT_REQUESTED));

        appendBookingStatus(spokenBookingStatus, signBookingStatus, fcsPartyDetailsModel);
    }



}
