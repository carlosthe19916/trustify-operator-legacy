package org.trusti.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.Service;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;

import java.util.Optional;

public class DBServiceDiscriminator implements ResourceDiscriminator<Service, Trusti> {
    @Override
    public Optional<Service> distinguish(Class<Service> resource, Trusti trusti, Context<Trusti> context) {
        String serviceName = DBService.getServiceName(trusti);
        ResourceID resourceID = new ResourceID(serviceName, trusti.getMetadata().getNamespace());
        var informerEventSource = (InformerEventSource<Service, Trusti>) context.eventSourceRetriever().getResourceEventSourceFor(Service.class);
        return informerEventSource.get(resourceID);
    }
}
