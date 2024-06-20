package org.trustify.operator.cdrs.v2alpha1.oidc;

import io.fabric8.openshift.api.model.operatorhub.v1.OperatorGroup;
import io.fabric8.openshift.api.model.operatorhub.v1.OperatorGroupBuilder;
import io.fabric8.openshift.api.model.operatorhub.v1.OperatorGroupSpec;
import io.fabric8.openshift.api.model.operatorhub.v1.OperatorGroupSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import jakarta.enterprise.context.ApplicationScoped;
import org.trustify.operator.Constants;
import org.trustify.operator.cdrs.v2alpha1.Trustify;
import org.trustify.operator.utils.CRDUtils;

import java.util.Map;

@KubernetesDependent(labelSelector = KeycloakOperatorGroup.LABEL_SELECTOR, resourceDiscriminator = KeycloakOperatorGroupDiscriminator.class)
@ApplicationScoped
public class KeycloakOperatorGroup extends CRUDKubernetesDependentResource<OperatorGroup, Trustify> {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=trustify-operator,component=keycloak";

    public KeycloakOperatorGroup() {
        super(OperatorGroup.class);
    }

    @Override
    public OperatorGroup desired(Trustify cr, Context<Trustify> context) {
        return newOperatorGroup(cr, context);
    }

    @SuppressWarnings("unchecked")
    private OperatorGroup newOperatorGroup(Trustify cr, Context<Trustify> context) {
        final var labels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        return new OperatorGroupBuilder()
                .withNewMetadata()
                    .withName(getOperatorGroupName(cr))
                    .withNamespace(cr.getMetadata().getNamespace())
                    .withLabels(labels)
                    .addToLabels("component", "oidc")
                    .withOwnerReferences(CRDUtils.getOwnerReference(cr))
                .endMetadata()
                .withSpec(getOperatorGroupSpec(cr))
                .build();
    }

    private OperatorGroupSpec getOperatorGroupSpec(Trustify cr) {
        return new OperatorGroupSpecBuilder()
                .addToTargetNamespaces(cr.getMetadata().getNamespace())
                .build();
    }

    public static String getOperatorGroupName(Trustify cr) {
        return cr.getMetadata().getName() + "-keycloak-operator";
    }

}
