package uk.gov.hmcts.reform.iahearingsapi.domain.service;

public interface FeatureToggler {

    boolean getValue(String key, Boolean defaultValue);

}
