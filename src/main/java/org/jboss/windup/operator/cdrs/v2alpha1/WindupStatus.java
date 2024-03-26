package org.jboss.windup.operator.cdrs.v2alpha1;

import java.util.List;

public record WindupStatus(List<WindupStatusCondition> conditions) {
    public WindupStatus() {
        this(null);
    }
}
