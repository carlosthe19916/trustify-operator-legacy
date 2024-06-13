package org.trustify.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;

import java.util.Optional;

public class DBDeploymentDiscriminator implements ResourceDiscriminator<Deployment, Trustify> {
    @Override
    public Optional<Deployment> distinguish(Class<Deployment> resource, Trustify cr, Context<Trustify> context) {
        String deploymentName = DBDeployment.getDeploymentName(cr);
        ResourceID resourceID = new ResourceID(deploymentName, cr.getMetadata().getNamespace());
        var informerEventSource = (InformerEventSource<Deployment, Trustify>) context.eventSourceRetriever().getResourceEventSourceFor(Deployment.class);
        return informerEventSource.get(resourceID);
    }
}
