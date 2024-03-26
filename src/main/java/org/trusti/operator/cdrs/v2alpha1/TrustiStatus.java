package org.trusti.operator.cdrs.v2alpha1;

import java.util.List;

public record TrustiStatus(List<TrustiStatusCondition> conditions) {
    public TrustiStatus() {
        this(null);
    }
}
