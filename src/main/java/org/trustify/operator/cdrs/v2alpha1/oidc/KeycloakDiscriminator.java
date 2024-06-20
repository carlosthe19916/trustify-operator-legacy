package org.trustify.operator.cdrs.v2alpha1.oidc;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import org.trustify.operator.cdrs.v2alpha1.Trustify;

import java.util.Optional;

public class KeycloakDiscriminator implements ResourceDiscriminator<GenericKubernetesResource, Trustify> {
    @Override
    public Optional<GenericKubernetesResource> distinguish(Class<GenericKubernetesResource> resource, Trustify cr, Context<Trustify> context) {
        String keycloakName = Keycloak.getKeycloakName(cr);
        ResourceID resourceID = new ResourceID(keycloakName, cr.getMetadata().getNamespace());
        var informerEventSource = (InformerEventSource<GenericKubernetesResource, Trustify>) context.eventSourceRetriever().getResourceEventSourceFor(GenericKubernetesResource.class);
        return informerEventSource.get(resourceID);
    }
}
