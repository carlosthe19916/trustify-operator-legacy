package org.trustify.operator.cdrs.v2alpha1;

import java.util.List;

public record TrustifyStatus(List<TrustifyStatusCondition> conditions) {
    public TrustifyStatus() {
        this(null);
    }
}
