package org.jboss.windup.operator;

import io.fabric8.kubernetes.api.model.SecretKeySelector;

public record ValueOrSecret(String name, String value, SecretKeySelector secret) {
}
