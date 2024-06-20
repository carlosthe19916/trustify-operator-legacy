package org.trustify.operator.cdrs.v2alpha1.oidc;

import io.fabric8.openshift.api.model.operatorhub.v1alpha1.ClusterServiceVersion;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import org.trustify.operator.cdrs.v2alpha1.Trustify;

import java.util.Optional;

public class KeycloakCSVDiscriminator implements ResourceDiscriminator<ClusterServiceVersion, Trustify> {
    @Override
    public Optional<ClusterServiceVersion> distinguish(Class<ClusterServiceVersion> resource, Trustify cr, Context<Trustify> context) {
        String name = KeycloakCSV.getCSVName(cr);
        ResourceID resourceID = new ResourceID(name, cr.getMetadata().getNamespace());
        var informerEventSource = (InformerEventSource<ClusterServiceVersion, Trustify>) context.eventSourceRetriever().getResourceEventSourceFor(ClusterServiceVersion.class);
        return informerEventSource.get(resourceID);
    }
}
