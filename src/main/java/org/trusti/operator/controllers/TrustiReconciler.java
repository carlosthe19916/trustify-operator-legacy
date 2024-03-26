package org.trusti.operator.controllers;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import org.jboss.logging.Logger;
import org.trusti.operator.cdrs.v2alpha1.*;

import java.time.Duration;
import java.util.Map;

import static io.javaoperatorsdk.operator.api.reconciler.Constants.WATCH_CURRENT_NAMESPACE;

@ControllerConfiguration(
        namespaces = WATCH_CURRENT_NAMESPACE,
        name = "trusti",
        dependents = {
                @Dependent(name = "db-pvc", type = DBPersistentVolumeClaim.class, useEventSourceWithName = TrustiReconciler.PVC_EVENT_SOURCE),
                @Dependent(name = "db-secret", type = DBSecret.class),
                @Dependent(name = "db-deployment", type = DBDeployment.class, dependsOn = {"db-pvc", "db-secret"}, readyPostcondition = DBDeployment.class, useEventSourceWithName = TrustiReconciler.DEPLOYMENT_EVENT_SOURCE),
                @Dependent(name = "db-service", type = DBService.class, dependsOn = {"db-deployment"}, useEventSourceWithName = TrustiReconciler.SERVICE_EVENT_SOURCE),

                @Dependent(name = "api-deployment", type = ApiDeployment.class, dependsOn = {"db-service"}, readyPostcondition = ApiDeployment.class, useEventSourceWithName = TrustiReconciler.DEPLOYMENT_EVENT_SOURCE),
                @Dependent(name = "api-service", type = ApiService.class, dependsOn = {"db-service"}, useEventSourceWithName = TrustiReconciler.SERVICE_EVENT_SOURCE),

                @Dependent(name = "ingress", type = ApiIngress.class, dependsOn = {"db-service"}, readyPostcondition = ApiIngress.class, useEventSourceWithName = TrustiReconciler.INGRESS_EVENT_SOURCE),
                @Dependent(name = "ingress-secure", type = ApiIngressSecure.class, dependsOn = {"db-service"}, readyPostcondition = ApiIngressSecure.class, useEventSourceWithName = TrustiReconciler.INGRESS_EVENT_SOURCE)
        }
)
public class TrustiReconciler implements Reconciler<Trusti>, ContextInitializer<Trusti>,
        EventSourceInitializer<Trusti> {

    private static final Logger logger = Logger.getLogger(TrustiReconciler.class);

    public static final String PVC_EVENT_SOURCE = "PVCEventSource";
    public static final String DEPLOYMENT_EVENT_SOURCE = "DeploymentEventSource";
    public static final String SERVICE_EVENT_SOURCE = "ServiceEventSource";
    public static final String INGRESS_EVENT_SOURCE = "IngressEventSource";

    @Override
    public void initContext(Trusti cr, Context<Trusti> context) {
        final var labels = Map.of(
                "app.kubernetes.io/managed-by", "trusti-operator",
                "app.kubernetes.io/name", cr.getMetadata().getName(),
                "app.kubernetes.io/part-of", cr.getMetadata().getName(),
                "trusti-operator/cluster", org.trusti.operator.Constants.TRUSTI_NAME
        );
        context.managedDependentResourceContext().put(org.trusti.operator.Constants.CONTEXT_LABELS_KEY, labels);
    }

    @Override
    public UpdateControl<Trusti> reconcile(Trusti cr, Context context) {
        return context.managedDependentResourceContext()
                .getWorkflowReconcileResult()
                .map(wrs -> {
                    if (wrs.allDependentResourcesReady()) {
                        return UpdateControl.<Trusti>noUpdate();
                    } else {
                        final var duration = Duration.ofSeconds(5);
                        return UpdateControl.<Trusti>noUpdate().rescheduleAfter(duration);
                    }
                })
                .orElseThrow();
    }

    @Override
    public Map<String, EventSource> prepareEventSources(EventSourceContext<Trusti> context) {
        var pcvInformerConfiguration = InformerConfiguration.from(PersistentVolumeClaim.class, context).build();
        var deploymentInformerConfiguration = InformerConfiguration.from(Deployment.class, context).build();
        var serviceInformerConfiguration = InformerConfiguration.from(Service.class, context).build();
        var ingressInformerConfiguration = InformerConfiguration.from(Ingress.class, context).build();

        var pcvInformerEventSource = new InformerEventSource<>(pcvInformerConfiguration, context);
        var deploymentInformerEventSource = new InformerEventSource<>(deploymentInformerConfiguration, context);
        var serviceInformerEventSource = new InformerEventSource<>(serviceInformerConfiguration, context);
        var ingressInformerEventSource = new InformerEventSource<>(ingressInformerConfiguration, context);

        return Map.of(
                PVC_EVENT_SOURCE, pcvInformerEventSource,
                DEPLOYMENT_EVENT_SOURCE, deploymentInformerEventSource,
                SERVICE_EVENT_SOURCE, serviceInformerEventSource,
                INGRESS_EVENT_SOURCE, ingressInformerEventSource
        );
    }
}
