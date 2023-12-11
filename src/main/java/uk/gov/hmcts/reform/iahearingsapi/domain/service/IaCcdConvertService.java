package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.exceptions.CcdDataDeserializationException;

@Service
public class IaCcdConvertService {

    private static final Logger LOG = LoggerFactory.getLogger(IaCcdConvertService.class);

    public AsylumCase convertToAsylumCaseData(Map<String, Object> dataMap) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try {
            return mapper.convertValue(dataMap, AsylumCase.class);
        } catch (Exception ex) {
            CcdDataDeserializationException ccdDeserializationException =
                new CcdDataDeserializationException("Error occurred when mapping case data to AsylumCase", ex);
            LOG.error("Error occurred when mapping case data to AsylumCase", ccdDeserializationException);
            throw ccdDeserializationException;
        }
    }

    public CaseDetails<AsylumCase> convertToAsylumCaseDetails(
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails) {
        AsylumCase asylumCase = convertToAsylumCaseData(caseDetails.getData());
        State state = caseDetails.getState() != null ? State.get(caseDetails.getState()) : null;
        String securityClassification = caseDetails.getSecurityClassification() != null
            ? caseDetails.getSecurityClassification().name()
            : null;

        return new CaseDetails<>(
            caseDetails.getId(),
            caseDetails.getJurisdiction(),
            state,
            asylumCase,
            caseDetails.getCreatedDate(),
            securityClassification);
    }

    public BailCase convertToBailCaseData(Map<String, Object> dataMap) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try {
            return mapper.convertValue(dataMap, BailCase.class);
        } catch (Exception ex) {
            CcdDataDeserializationException ccdDeserializationException =
                new CcdDataDeserializationException("Error occurred when mapping case data to BailCase", ex);
            LOG.error("Error occurred when mapping case data to BailCase", ccdDeserializationException);
            throw ccdDeserializationException;
        }
    }



}
