package org.trusti.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;

import java.util.Optional;

public class ApiIngressDiscriminator implements ResourceDiscriminator<Ingress, Trusti> {
    @Override
    public Optional<Ingress> distinguish(Class<Ingress> resource, Trusti trusti, Context<Trusti> context) {
        String ingressName = ApiIngress.getIngressName(trusti);
        ResourceID resourceID = new ResourceID(ingressName, trusti.getMetadata().getNamespace());
        var informerEventSource = (InformerEventSource<Ingress, Trusti>) context.eventSourceRetriever().getResourceEventSourceFor(Ingress.class);
        return informerEventSource.get(resourceID);
    }
}
