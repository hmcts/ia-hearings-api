package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;

import java.util.*;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.*;


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
        SUPPORTER_EMAIL_ADDRESS,
        SUPPORTER_2_EMAIL_ADDRESS,
        SUPPORTER_3_EMAIL_ADDRESS,
        SUPPORTER_4_EMAIL_ADDRESS
    );

    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    public List<PartyDetailsModel> map(BailCase bailCase, BailCaseDataToServiceHearingValuesMapper caseDataMapper) {

        List<PartyDetailsModel> fcsDetailsList = new ArrayList<>();

        int i = 0;
        while (i < 4) {
            if (BailMapperUtils.isFcsSupporting(bailCase, FCS_N_HAS_SUPPORT_FIELD.get(i))) {
                PartyDetailsModel fcsPartyDetailsModel = PartyDetailsModel.builder()
                    .partyID(caseDataMapper.getValueByDefinition(bailCase, FCS_N_PARTY_ID_FIELD.get(i)))
                    .partyType(PartyType.IND.getPartyType())
                    .partyRole("FINS")
                    .individualDetails(
                        IndividualDetailsModel.builder()
                            .firstName(caseDataMapper.getValueByDefinition(bailCase, FCS_N_GIVEN_NAME_FIELD.get(i)))
                            .lastName(caseDataMapper.getValueByDefinition(bailCase, FCS_N_FAMILY_NAME_FIELD.get(i)))
                            .preferredHearingChannel(caseDataMapper.getHearingChannel(bailCase))
                            .hearingChannelEmail(
                                caseDataMapper.getHearingChannelEmailPhone(bailCase, FCS_N_EMAIL_FIELD.get(i)))
                            .hearingChannelPhone(
                                caseDataMapper.getHearingChannelEmailPhone(bailCase, SPONSOR_MOBILE_NUMBER))
                            .build())
                    .build();

                languageAndAdjustmentsMapper.processAsylumPartyCaseFlags(bailCase, fcsPartyDetailsModel);

                fcsDetailsList.add(fcsPartyDetailsModel);
            }
            i++;
        }

        return fcsDetailsList;
    }
}
