package org.trustify.operator.cdrs.v2alpha1.oidc;

import io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import org.trustify.operator.cdrs.v2alpha1.Trustify;

import java.util.Optional;

public class KeycloakSubscriptionDiscriminator implements ResourceDiscriminator<Subscription, Trustify> {
    @Override
    public Optional<Subscription> distinguish(Class<Subscription> resource, Trustify cr, Context<Trustify> context) {
        String name = KeycloakSubscription.getSubscriptionName(cr);
        ResourceID resourceID = new ResourceID(name, cr.getMetadata().getNamespace());
        var informerEventSource = (InformerEventSource<Subscription, Trustify>) context.eventSourceRetriever().getResourceEventSourceFor(Subscription.class);
        return informerEventSource.get(resourceID);
    }
}
