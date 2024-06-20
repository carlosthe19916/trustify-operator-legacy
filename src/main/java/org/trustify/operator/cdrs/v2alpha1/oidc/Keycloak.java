package org.trustify.operator.cdrs.v2alpha1.oidc;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import jakarta.enterprise.context.ApplicationScoped;
import org.trustify.operator.Constants;
import org.trustify.operator.cdrs.v2alpha1.Trustify;
import org.trustify.operator.utils.CRDUtils;

import java.util.HashMap;
import java.util.Map;

//@KubernetesDependent(labelSelector = Keycloak.LABEL_SELECTOR, resourceDiscriminator = KeycloakDiscriminator.class)
//@ApplicationScoped
public class Keycloak extends CRUDKubernetesDependentResource<GenericKubernetesResource, Trustify> {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=trustify-operator,component=oidc";

    public Keycloak() {
        super(GenericKubernetesResource.class);
    }

    @Override
    public GenericKubernetesResource desired(Trustify cr, Context<Trustify> context) {
        return newKeycloak(cr, context);
    }

    @SuppressWarnings("unchecked")
    private GenericKubernetesResource newKeycloak(Trustify cr, Context<Trustify> context) {
        final var labels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        Map<String, Object> spec = new HashMap<>();
        spec.put("instances", "1");

        return new GenericKubernetesResourceBuilder()
                .withNewMetadata()
                .withName(getKeycloakName(cr))
                .withNamespace(cr.getMetadata().getNamespace())
                .withLabels(labels)
                .addToLabels("component", "oidc")
                .withOwnerReferences(CRDUtils.getOwnerReference(cr))
                .endMetadata()
                .addToAdditionalProperties("spec", spec)
                .build();
    }

    public static String getKeycloakName(Trustify cr) {
        return cr.getMetadata().getName() + "-keycloak";
    }

}
