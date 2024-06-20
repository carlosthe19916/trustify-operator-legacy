package org.trustify.operator.cdrs.v2alpha1.oidc;

import io.fabric8.openshift.api.model.operatorhub.v1alpha1.ClusterServiceVersion;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;
import jakarta.enterprise.context.ApplicationScoped;
import org.trustify.operator.cdrs.v2alpha1.Trustify;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@KubernetesDependent(labelSelector = KeycloakCSV.LABEL_SELECTOR, resourceDiscriminator = KeycloakCSVDiscriminator.class)
@ApplicationScoped
public class KeycloakCSV extends KubernetesDependentResource<ClusterServiceVersion, Trustify>
        implements Condition<ClusterServiceVersion, Trustify> {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=trustify-operator,component=keycloak";

    public KeycloakCSV() {
        super(ClusterServiceVersion.class);
    }

    @Override
    public boolean isMet(DependentResource<ClusterServiceVersion, Trustify> dependentResource, Trustify primary, Context<Trustify> context) {
        return context.getSecondaryResource(ClusterServiceVersion.class, new KeycloakCSVDiscriminator())
                .flatMap(csv -> Optional.ofNullable(csv.getStatus())
                        .flatMap(status -> Optional.ofNullable(status.getPhase()))
                        .map(phase -> phase.equals("Succeeded"))
                )
                .orElse(false);
    }

    public static String getCSVName(Trustify cr) {
//        return cr.getMetadata().getName() + "-keycloak-operator";
        return "keycloak-operator.v25.0.0";
    }

}
