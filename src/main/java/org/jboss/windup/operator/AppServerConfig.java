package org.jboss.windup.operator;

public record AppServerConfig(String[] webLivenessProbeCmd, String[] webReadinessProbeCmd) {
}
