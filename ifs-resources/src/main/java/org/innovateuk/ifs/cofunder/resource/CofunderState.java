package org.innovateuk.ifs.cofunder.resource;

import org.innovateuk.ifs.identity.IdentifiableEnum;
import org.innovateuk.ifs.workflow.resource.ProcessState;
import org.innovateuk.ifs.workflow.resource.State;

public enum CofunderState implements ProcessState, IdentifiableEnum {
    CREATED(56, State.CREATED),
    REJECTED(57, State.REJECTED),
    ACCEPTED(58, State.ACCEPTED);

    private final long id;
    private final State backingState;

    CofunderState(long id, State backingState) {
        this.id = id;
        this.backingState = backingState;
    }

    @Override
    public String getStateName() {
        return backingState.name();
    }

    @Override
    public State getBackingState() {
        return backingState;
    }

    @Override
    public long getId() {
        return id;
    }
}