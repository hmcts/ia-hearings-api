package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;

public class PreSubmitCallbackResponse<T extends CaseData> {

    private T data;
    private final Set<String> errors = new LinkedHashSet<>();
    private State state;

    private PreSubmitCallbackResponse() {

    }

    public PreSubmitCallbackResponse(
        T data
    ) {
        requireNonNull(data);
        this.data = data;
    }

    public PreSubmitCallbackResponse(
        T data,
        State state
    ) {
        requireNonNull(data);
        this.data = data;
        this.state = state;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        requireNonNull(data);
        this.data = data;
    }

    public State getState() {
        return state;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addErrors(Collection<String> errors) {
        this.errors.addAll(errors);
    }

    public Set<String> getErrors() {
        return ImmutableSet.copyOf(errors);
    }

}
