package org.trusti.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;

import java.util.Optional;

public class ApiDeploymentDiscriminator implements ResourceDiscriminator<Deployment, Trusti> {
    @Override
    public Optional<Deployment> distinguish(Class<Deployment> resource, Trusti trusti, Context<Trusti> context) {
        String deploymentName = ApiDeployment.getDeploymentName(trusti);
        ResourceID resourceID = new ResourceID(deploymentName, trusti.getMetadata().getNamespace());
        var informerEventSource = (InformerEventSource<Deployment, Trusti>) context.eventSourceRetriever().getResourceEventSourceFor(Deployment.class);
        return informerEventSource.get(resourceID);
    }
}