package org.jboss.windup.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressTLS;
import io.fabric8.kubernetes.api.model.networking.v1.IngressTLSBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.jboss.windup.operator.Constants;
import org.jboss.windup.operator.utils.CRDUtils;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Map;

@KubernetesDependent(labelSelector = WebIngressSecure.LABEL_SELECTOR, resourceDiscriminator = WebIngressSecureDiscriminator.class)
@ApplicationScoped
public class WebIngressSecure extends WebIngressBase {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=windup-operator,component=web,component-variant=https";

    @Override
    @SuppressWarnings("unchecked")
    protected Ingress desired(Windup cr, Context<Windup> context) {
        return newIngress(
                cr,
                context,
                getIngressName(cr),
                Map.of(
                        "component", "web",
                        "component-variant", "https"
                ),
                Collections.emptyMap()
        );
    }

    @Override
    public boolean isMet(DependentResource<Ingress, Windup> dependentResource, Windup primary, Context<Windup> context) {
        return context.getSecondaryResource(Ingress.class, new WebIngressSecureDiscriminator())
                .map(in -> {
                    final var status = in.getStatus();
                    if (status != null) {
                        final var ingresses = status.getLoadBalancer().getIngress();
                        // only set the status if the ingress is ready to provide the info we need
                        return ingresses != null && !ingresses.isEmpty();
                    }
                    return false;
                })
                .orElse(false);
    }

    @Override
    protected String getHostname(Windup cr) {
        return CRDUtils
                .getValueFromSubSpec(cr.getSpec().hostnameSpec(), WindupSpec.HostnameSpec::hostname)
                .orElseGet(() -> getClusterDomainOnOpenshift()
                        // Openshift
                        .map(domain -> CRDUtils
                                .getValueFromSubSpec(cr.getSpec().hostnameSpec(), WindupSpec.HostnameSpec::hostname)
                                .orElseGet(() -> getOpenshiftHostname(cr, k8sClient.getConfiguration().getNamespace(), domain))
                        )
                        // Kubernetes vanilla
                        .orElse(null)
                );
    }

    @Override
    protected IngressTLS getIngressTLS(Windup cr) {
        String tlsSecretName = CRDUtils.getValueFromSubSpec(cr.getSpec().httpSpec(), WindupSpec.HttpSpec::tlsSecret)
                .orElse(null);

        return new IngressTLSBuilder()
                .withSecretName(tlsSecretName)
                .build();
    }

    public static String getIngressName(Windup cr) {
        return cr.getMetadata().getName() + Constants.INGRESS_SECURE_SUFFIX;
    }

    public static String getOpenshiftHostname(Windup cr, String namespace, String domain) {
        return "secure-" + namespace + "-" + cr.getMetadata().getName() + "." + domain;
    }
}
