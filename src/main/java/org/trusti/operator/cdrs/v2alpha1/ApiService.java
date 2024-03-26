package org.trusti.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.trusti.operator.Constants;
import org.trusti.operator.utils.CRDUtils;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@KubernetesDependent(labelSelector = ApiService.LABEL_SELECTOR, resourceDiscriminator = ApiServiceDiscriminator.class)
@ApplicationScoped
public class ApiService extends CRUDKubernetesDependentResource<Service, Trusti> {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=trusti-operator,component=api";

    public ApiService() {
        super(Service.class);
    }

    @Override
    public Service desired(Trusti cr, Context context) {
        return newService(cr, context);
    }

    @SuppressWarnings("unchecked")
    private Service newService(Trusti cr, Context context) {
        final var labels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        return new ServiceBuilder()
                .withNewMetadata()
                .withName(getServiceName(cr))
                .withNamespace(cr.getMetadata().getNamespace())
                .withLabels(labels)
                .addToLabels("component", "api")
                .withOwnerReferences(CRDUtils.getOwnerReference(cr))
                .endMetadata()
                .withSpec(getServiceSpec(cr))
                .build();
    }

    private ServiceSpec getServiceSpec(Trusti cr) {
        return new ServiceSpecBuilder()
                .addNewPort()
                .withPort(getServicePort(cr))
                .withProtocol(Constants.SERVICE_PROTOCOL)
                .endPort()
                .withSelector(Constants.API_SELECTOR_LABELS)
                .withType("ClusterIP")
                .build();
    }

    public static int getServicePort(Trusti cr) {
        return Constants.HTTP_PORT;
    }

    public static String getServiceName(Trusti cr) {
        return cr.getMetadata().getName() + Constants.API_SERVICE_SUFFIX;
    }

    public static boolean isTlsConfigured(Trusti cr) {
        var tlsSecret = CRDUtils.getValueFromSubSpec(cr.getSpec().httpSpec(), TrustiSpec.HttpSpec::tlsSecret);
        return tlsSecret.isPresent() && !tlsSecret.get().trim().isEmpty();
    }
}
