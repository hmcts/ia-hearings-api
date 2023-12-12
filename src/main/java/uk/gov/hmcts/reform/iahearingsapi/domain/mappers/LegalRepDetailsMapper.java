package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_MOBILE_PHONE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_PHONE;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;

@Component
public class LegalRepDetailsMapper {

    public PartyDetailsModel map(AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getLegalRepPartyId(asylumCase))
            .partyType(PartyType.IND.getPartyType())
            .partyRole("LGRP")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName(caseDataMapper.getName(asylumCase, LEGAL_REP_NAME))
                    .lastName(caseDataMapper.getName(asylumCase, LEGAL_REP_FAMILY_NAME))
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase))
                    .hearingChannelEmail(
                        caseDataMapper.getHearingChannelEmail(asylumCase, LEGAL_REPRESENTATIVE_EMAIL_ADDRESS))
                    .hearingChannelPhone(
                        caseDataMapper.getHearingChannelPhone(asylumCase, LEGAL_REP_MOBILE_PHONE_NUMBER))
                    .build())
            .build();
    }

    public PartyDetailsModel map(BailCase bailCase, BailCaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getLegalRepPartyId(bailCase))
            .partyType(PartyType.IND.getPartyType())
            .partyRole("LGRP")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName(caseDataMapper.getStringValueByDefinition(bailCase,
                                                                         BailCaseFieldDefinition.LEGAL_REP_NAME))
                    .lastName(caseDataMapper.getStringValueByDefinition(bailCase,
                                                                        BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME))
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(bailCase))
                    .hearingChannelEmail(
                        caseDataMapper.getHearingChannelEmailPhone(bailCase, LEGAL_REP_EMAIL))
                    .hearingChannelPhone(
                        caseDataMapper.getHearingChannelEmailPhone(bailCase, LEGAL_REP_PHONE))
                    .build())
            .build();
    }
}
