package org.trustify.operator.cdrs.v2alpha1.oidc;

import io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionBuilder;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionSpec;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import jakarta.enterprise.context.ApplicationScoped;
import org.trustify.operator.Constants;
import org.trustify.operator.cdrs.v2alpha1.Trustify;
import org.trustify.operator.utils.CRDUtils;

import java.util.Map;

@KubernetesDependent(labelSelector = KeycloakSubscription.LABEL_SELECTOR, resourceDiscriminator = KeycloakSubscriptionDiscriminator.class)
@ApplicationScoped
public class KeycloakSubscription extends CRUDKubernetesDependentResource<Subscription, Trustify> {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=trustify-operator,component=keycloak";

    public KeycloakSubscription() {
        super(Subscription.class);
    }

    @Override
    public void delete(Trustify primary, Context<Trustify> context) {
        super.delete(primary, context);
    }

    @Override
    public Subscription desired(Trustify cr, Context<Trustify> context) {
        return newSubscription(cr, context);
    }

    @SuppressWarnings("unchecked")
    private Subscription newSubscription(Trustify cr, Context<Trustify> context) {
        final var labels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        return new SubscriptionBuilder()
                .withNewMetadata()
                .withName(getSubscriptionName(cr))
                .withNamespace(cr.getMetadata().getNamespace())
                .withLabels(labels)
                .addToLabels("component", "keycloak")
                .withOwnerReferences(CRDUtils.getOwnerReference(cr))
                .endMetadata()
                .withSpec(getSubscriptionSpec(cr))
                .build();
    }

    private SubscriptionSpec getSubscriptionSpec(Trustify cr) {
        return new SubscriptionSpecBuilder()
                .withName("keycloak-operator")
                .withChannel("fast")
                .withSource("operatorhubio-catalog")
                .withSourceNamespace("olm")
                .build();
    }

    public static String getSubscriptionName(Trustify cr) {
        return cr.getMetadata().getName() + "-keycloak-operator";
    }

}
