package org.trusti.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategyBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;
import org.trusti.operator.Config;
import org.trusti.operator.Constants;
import org.trusti.operator.controllers.TrustiDistConfigurator;
import org.trusti.operator.utils.CRDUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@KubernetesDependent(labelSelector = ApiDeployment.LABEL_SELECTOR, resourceDiscriminator = ApiDeploymentDiscriminator.class)
@ApplicationScoped
public class ApiDeployment extends CRUDKubernetesDependentResource<Deployment, Trusti>
        implements Matcher<Deployment, Trusti>, Condition<Deployment, Trusti> {

    public static final String LABEL_SELECTOR = "app.kubernetes.io/managed-by=trusti-operator,component=api";

    @Inject
    Config config;

    public ApiDeployment() {
        super(Deployment.class);
    }

    @Override
    protected Deployment desired(Trusti cr, Context<Trusti> context) {
        TrustiDistConfigurator distConfigurator = new TrustiDistConfigurator(cr);
        return newDeployment(cr, context, distConfigurator);
    }

    @Override
    public Result<Deployment> match(Deployment actual, Trusti cr, Context<Trusti> context) {
        final var container = actual.getSpec()
                .getTemplate().getSpec().getContainers()
                .stream()
                .findFirst();

        return Result.nonComputed(container
                .map(c -> c.getImage() != null)
                .orElse(false)
        );
    }

    @Override
    public boolean isMet(DependentResource<Deployment, Trusti> dependentResource, Trusti primary, Context<Trusti> context) {
        return context.getSecondaryResource(Deployment.class, new ApiDeploymentDiscriminator())
                .map(deployment -> {
                    final var status = deployment.getStatus();
                    if (status != null) {
                        final var readyReplicas = status.getReadyReplicas();
                        return readyReplicas != null && readyReplicas >= 1;
                    }
                    return false;
                })
                .orElse(false);
    }

    @SuppressWarnings("unchecked")
    private Deployment newDeployment(Trusti cr, Context<Trusti> context, TrustiDistConfigurator distConfigurator) {
        final var contextLabels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        return new DeploymentBuilder()
                .withNewMetadata()
                .withName(getDeploymentName(cr))
                .withNamespace(cr.getMetadata().getNamespace())
                .withLabels(contextLabels)
                .addToLabels("component", "api")
                .withAnnotations(Map.of(
                        "app.openshift.io/connects-to", "[" +
                                "{\"apiVersion\":\"apps/v1\",\"kind\":\"Deployment\",\"name\":\"" + DBDeployment.getDeploymentName(cr) + "\"}" +
                                "]"
                ))
                .withOwnerReferences(CRDUtils.getOwnerReference(cr))
                .endMetadata()
                .withSpec(getDeploymentSpec(cr, context, distConfigurator))
                .build();
    }

    @SuppressWarnings("unchecked")
    private DeploymentSpec getDeploymentSpec(Trusti cr, Context<Trusti> context, TrustiDistConfigurator distConfigurator) {
        final var contextLabels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        Map<String, String> selectorLabels = Constants.API_SELECTOR_LABELS;
        String image = config.apiImage();
        String importerImage = config.importerImage();
        String imagePullPolicy = config.imagePullPolicy();

        List<EnvVar> envVars = distConfigurator.getAllEnvVars();
        List<Volume> volumes = distConfigurator.getAllVolumes();
        List<VolumeMount> volumeMounts = distConfigurator.getAllVolumeMounts();

        TrustiSpec.ResourcesLimitSpec resourcesLimitSpec = CRDUtils.getValueFromSubSpec(cr.getSpec(), TrustiSpec::apiResourceLimitSpec)
                .orElse(null);

        envVars.add(new EnvVarBuilder()
                .withName("TRUSTI_IMPORTER_IMAGE")
                .withValue(importerImage)
                .build()
        );
        envVars.add(new EnvVarBuilder()
                .withName("TRUSTI_NAMESPACE")
                .withValue(cr.getMetadata().getNamespace())
                .build()
        );
        envVars.add(new EnvVarBuilder()
                .withName("TRUSTI_DOMAIN")
                .withValue(String.format("http://%s:%s", ApiService.getServiceName(cr), ApiService.getServicePort(cr)))
                .build()
        );

        return new DeploymentSpecBuilder()
                .withStrategy(new DeploymentStrategyBuilder()
                        .withType("Recreate")
                        .build()
                )
                .withReplicas(1)
                .withSelector(new LabelSelectorBuilder()
                        .withMatchLabels(selectorLabels)
                        .build()
                )
                .withTemplate(new PodTemplateSpecBuilder()
                                .withNewMetadata()
                                .withLabels(Stream
                                        .concat(contextLabels.entrySet().stream(), selectorLabels.entrySet().stream())
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                                )
                                .endMetadata()
                                .withSpec(new PodSpecBuilder()
                                                .withRestartPolicy("Always")
                                                .withTerminationGracePeriodSeconds(70L)
                                                .withImagePullSecrets(cr.getSpec().imagePullSecrets())
                                                .withContainers(new ContainerBuilder()
                                                                .withName(Constants.TRUSTI_API_NAME)
                                                                .withImage(image)
                                                                .withImagePullPolicy(imagePullPolicy)
                                                                .withEnv(envVars)
                                                                .withPorts(
                                                                        new ContainerPortBuilder()
                                                                                .withName("http")
                                                                                .withProtocol("TCP")
                                                                                .withContainerPort(8080)
                                                                                .build(),
                                                                        new ContainerPortBuilder()
                                                                                .withName("https")
                                                                                .withProtocol("TCP")
                                                                                .withContainerPort(8443)
                                                                                .build()
                                                                )
                                                                .withLivenessProbe(new ProbeBuilder()
                                                                        .withHttpGet(new HTTPGetActionBuilder()
                                                                                .withPath("/q/health/live")
                                                                                .withNewPort("http")
                                                                                .build()
                                                                        )
                                                                        .withInitialDelaySeconds(60)
                                                                        .withTimeoutSeconds(10)
                                                                        .withPeriodSeconds(10)
                                                                        .withSuccessThreshold(1)
                                                                        .withFailureThreshold(3)
                                                                        .build()
                                                                )
//                                        .withReadinessProbe(new ProbeBuilder()
//                                                .withHttpGet(new HTTPGetActionBuilder()
//                                                        .withPath("/q/health/ready")
//                                                        .withNewPort("http")
//                                                        .build()
//                                                )
//                                                .withInitialDelaySeconds(5)
//                                                .withTimeoutSeconds(1)
//                                                .withPeriodSeconds(10)
//                                                .withSuccessThreshold(1)
//                                                .withFailureThreshold(3)
//                                                .build()
//                                        )
                                                                .withVolumeMounts(volumeMounts)
                                                                .withResources(new ResourceRequirementsBuilder()
                                                                        .withRequests(Map.of(
                                                                                "cpu", new Quantity(CRDUtils.getValueFromSubSpec(resourcesLimitSpec, TrustiSpec.ResourcesLimitSpec::cpuRequest).orElse("0.5")),
                                                                                "memory", new Quantity(CRDUtils.getValueFromSubSpec(resourcesLimitSpec, TrustiSpec.ResourcesLimitSpec::memoryRequest).orElse("0.5Gi"))
                                                                        ))
                                                                        .withLimits(Map.of(
                                                                                "cpu", new Quantity(CRDUtils.getValueFromSubSpec(resourcesLimitSpec, TrustiSpec.ResourcesLimitSpec::cpuLimit).orElse("1")),
                                                                                "memory", new Quantity(CRDUtils.getValueFromSubSpec(resourcesLimitSpec, TrustiSpec.ResourcesLimitSpec::memoryLimit).orElse("1Gi"))
                                                                        ))
                                                                        .build()
                                                                )
                                                                .build()
                                                )
                                                .withVolumes(volumes)
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    public static String getDeploymentName(Trusti cr) {
        return cr.getMetadata().getName() + Constants.API_DEPLOYMENT_SUFFIX;
    }
}
