package org.trusti.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.trusti.operator.Constants;
import org.trusti.operator.utils.CRDUtils;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@KubernetesDependent(labelSelector = DBPersistentVolumeClaim.LABEL_SELECTOR, resourceDiscriminator = DBPersistentVolumeClaimDiscriminator.class)
@ApplicationScoped
public class DBPersistentVolumeClaim extends CRUDKubernetesDependentResource<PersistentVolumeClaim, Trusti>
        implements Creator<PersistentVolumeClaim, Trusti> {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=trusti-operator,component=db";

    public DBPersistentVolumeClaim() {
        super(PersistentVolumeClaim.class);
    }

    @Override
    protected PersistentVolumeClaim desired(Trusti cr, Context<Trusti> context) {
        return newPersistentVolumeClaim(cr, context);
    }

    @SuppressWarnings("unchecked")
    private PersistentVolumeClaim newPersistentVolumeClaim(Trusti cr, Context<Trusti> context) {
        final var labels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        String pvcStorageSize = CRDUtils.getValueFromSubSpec(cr.getSpec().databaseSpec(), TrustiSpec.DatabaseSpec::size)
                .orElse(Constants.POSTGRESQL_PVC_SIZE);

        return new PersistentVolumeClaimBuilder()
                .withNewMetadata()
                .withName(getPersistentVolumeClaimName(cr))
                .withNamespace(cr.getMetadata().getNamespace())
                .withLabels(labels)
                .addToLabels("component", "db")
                .withOwnerReferences(CRDUtils.getOwnerReference(cr))
                .endMetadata()
                .withSpec(new PersistentVolumeClaimSpecBuilder()
                        .withAccessModes("ReadWriteOnce")
                        .withResources(new VolumeResourceRequirementsBuilder()
                                .withRequests(Map.of("storage", new Quantity(pvcStorageSize)))
                                .build()
                        )
                        .build()
                )
                .build();
    }

    @Override
    public Matcher.Result<PersistentVolumeClaim> match(PersistentVolumeClaim actual, Trusti cr, Context<Trusti> context) {
        final var desiredPersistentVolumeClaimName = getPersistentVolumeClaimName(cr);
        return Matcher.Result.nonComputed(actual
                .getMetadata()
                .getName()
                .equals(desiredPersistentVolumeClaimName)
        );
    }

    public static String getPersistentVolumeClaimName(Trusti cr) {
        return cr.getMetadata().getName() + Constants.DB_PVC_SUFFIX;
    }

}
