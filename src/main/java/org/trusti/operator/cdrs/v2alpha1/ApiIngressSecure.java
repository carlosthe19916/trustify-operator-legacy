package org.trusti.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressTLS;
import io.fabric8.kubernetes.api.model.networking.v1.IngressTLSBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.trusti.operator.Constants;
import org.trusti.operator.utils.CRDUtils;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Map;

@KubernetesDependent(labelSelector = ApiIngressSecure.LABEL_SELECTOR, resourceDiscriminator = ApiIngressSecureDiscriminator.class)
@ApplicationScoped
public class ApiIngressSecure extends ApiIngressBase {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=trusti-operator,component=api,component-variant=https";

    @Override
    @SuppressWarnings("unchecked")
    protected Ingress desired(Trusti cr, Context<Trusti> context) {
        return newIngress(
                cr,
                context,
                getIngressName(cr),
                Map.of(
                        "component", "api",
                        "component-variant", "https"
                ),
                Collections.emptyMap()
        );
    }

    @Override
    public boolean isMet(DependentResource<Ingress, Trusti> dependentResource, Trusti primary, Context<Trusti> context) {
        return context.getSecondaryResource(Ingress.class, new ApiIngressSecureDiscriminator())
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
    protected String getHostname(Trusti cr) {
        return CRDUtils
                .getValueFromSubSpec(cr.getSpec().hostnameSpec(), TrustiSpec.HostnameSpec::hostname)
                .orElseGet(() -> getClusterDomainOnOpenshift()
                        // Openshift
                        .map(domain -> CRDUtils
                                .getValueFromSubSpec(cr.getSpec().hostnameSpec(), TrustiSpec.HostnameSpec::hostname)
                                .orElseGet(() -> getOpenshiftHostname(cr, k8sClient.getConfiguration().getNamespace(), domain))
                        )
                        // Kubernetes vanilla
                        .orElse(null)
                );
    }

    @Override
    protected IngressTLS getIngressTLS(Trusti cr) {
        String tlsSecretName = CRDUtils.getValueFromSubSpec(cr.getSpec().httpSpec(), TrustiSpec.HttpSpec::tlsSecret)
                .orElse(null);

        return new IngressTLSBuilder()
                .withSecretName(tlsSecretName)
                .build();
    }

    public static String getIngressName(Trusti cr) {
        return cr.getMetadata().getName() + Constants.INGRESS_SECURE_SUFFIX;
    }

    public static String getOpenshiftHostname(Trusti cr, String namespace, String domain) {
        return "secure-" + namespace + "-" + cr.getMetadata().getName() + "." + domain;
    }
}
