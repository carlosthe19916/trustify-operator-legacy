package org.trustify.operator.cdrs.v2alpha1.oidc;

import io.fabric8.openshift.api.model.operatorhub.v1.OperatorGroup;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import org.trustify.operator.cdrs.v2alpha1.Trustify;

import java.util.Optional;

public class KeycloakOperatorGroupDiscriminator implements ResourceDiscriminator<OperatorGroup, Trustify> {
    @Override
    public Optional<OperatorGroup> distinguish(Class<OperatorGroup> resource, Trustify cr, Context<Trustify> context) {
        String name = KeycloakOperatorGroup.getOperatorGroupName(cr);
        ResourceID resourceID = new ResourceID(name, cr.getMetadata().getNamespace());
        var informerEventSource = (InformerEventSource<OperatorGroup, Trustify>) context.eventSourceRetriever().getResourceEventSourceFor(OperatorGroup.class);
        return informerEventSource.get(resourceID);
    }
}
