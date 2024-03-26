package org.jboss.windup.operator.cdrs.v2alpha1;

public record WindupStatusCondition(String type, Boolean status, String message) {
    public static final String READY = "Ready";
    public static final String HAS_ERRORS = "HasErrors";
    public static final String ROLLING_UPDATE = "RollingUpdate";
}
