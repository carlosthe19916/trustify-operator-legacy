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

@KubernetesDependent(labelSelector = DBService.LABEL_SELECTOR, resourceDiscriminator = DBServiceDiscriminator.class)
@ApplicationScoped
public class DBService extends CRUDKubernetesDependentResource<Service, Trusti> {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=trusti-operator,component=db";

    public DBService() {
        super(Service.class);
    }

    @Override
    public Service desired(Trusti cr, Context<Trusti> context) {
        return newService(cr, context);
    }

    @SuppressWarnings("unchecked")
    private Service newService(Trusti cr, Context<Trusti> context) {
        final var labels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        return new ServiceBuilder()
                .withNewMetadata()
                .withName(getServiceName(cr))
                .withNamespace(cr.getMetadata().getNamespace())
                .withLabels(labels)
                .addToLabels("component", "db")
                .withOwnerReferences(CRDUtils.getOwnerReference(cr))
                .endMetadata()
                .withSpec(getServiceSpec(cr))
                .build();
    }

    private ServiceSpec getServiceSpec(Trusti cr) {
        return new ServiceSpecBuilder()
                .addNewPort()
                .withPort(5432)
                .withProtocol(Constants.SERVICE_PROTOCOL)
                .endPort()
                .withSelector(Constants.DB_SELECTOR_LABELS)
                .withType("ClusterIP")
                .build();
    }

    public static String getServiceName(Trusti cr) {
        return cr.getMetadata().getName() + Constants.DB_SERVICE_SUFFIX;
    }

    public static String getJdbcUrl(Trusti cr) {
        return String.format(
                "jdbc:postgresql://%s:%s/%s",
                cr.getMetadata().getName() + Constants.DB_SERVICE_SUFFIX,
                5432,
                Constants.DB_NAME
        );
    }

}
