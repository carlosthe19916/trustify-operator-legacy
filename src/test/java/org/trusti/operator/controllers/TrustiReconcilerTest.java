package org.trusti.operator.controllers;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.trusti.operator.cdrs.v2alpha1.DBDeployment;
import org.trusti.operator.cdrs.v2alpha1.DBService;
import org.trusti.operator.cdrs.v2alpha1.ApiDeployment;
import org.trusti.operator.cdrs.v2alpha1.ApiIngress;
import org.trusti.operator.cdrs.v2alpha1.ApiService;
import org.trusti.operator.cdrs.v2alpha1.Trusti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class TrustiReconcilerTest {

    public static final String TEST_APP = "myapp";

    @ConfigProperty(name = "related.image.db")
    String dbImage;

    @ConfigProperty(name = "related.image.api")
    String apiImage;

    @Inject
    KubernetesClient client;

    @Inject
    Operator operator;

    @BeforeEach
    public void startOperator() {
        operator.start();
    }

    @AfterEach
    public void stopOperator() {
        operator.stop();
    }

    @Test
    @Order(1)
    public void reconcileShouldWork() throws InterruptedException {
        final var app = new Trusti();
        final var metadata = new ObjectMetaBuilder()
                .withName(TEST_APP)
                .withNamespace(client.getNamespace())
                .build();
        app.setMetadata(metadata);

        // Delete prev instance if exists already
        if (client.resource(app).get() != null) {
            client.resource(app).delete();
            Thread.sleep(10_000);
        }

        // Instantiate Trusti
        client.resource(app).serverSideApply();

        // Verify resources
        await()
                .ignoreException(NullPointerException.class)
                .atMost(5, TimeUnit.MINUTES)
                .untilAsserted(() -> {
                    // Database
                    final var dbDeployment = client.apps()
                            .deployments()
                            .inNamespace(metadata.getNamespace())
                            .withName(DBDeployment.getDeploymentName(app))
                            .get();
                    final var dbContainer = dbDeployment.getSpec()
                            .getTemplate()
                            .getSpec()
                            .getContainers()
                            .stream()
                            .findFirst();
                    assertThat("DB container not found", dbContainer.isPresent(), is(true));
                    assertThat("DB container image not valid", dbContainer.get().getImage(), is(dbImage));

                    assertEquals(1, dbDeployment.getStatus().getReadyReplicas(), "Expected DB deployment number of replicas doesn't match");

                    // Database service
                    final var dbService = client.services()
                            .inNamespace(metadata.getNamespace())
                            .withName(DBService.getServiceName(app))
                            .get();
                    final var dbPort = dbService.getSpec()
                            .getPorts()
                            .get(0)
                            .getPort();
                    assertThat("DB service port not valid", dbPort, is(5432));


                    // Api Deployment
                    final var apiDeployment = client.apps()
                            .deployments()
                            .inNamespace(metadata.getNamespace())
                            .withName(ApiDeployment.getDeploymentName(app))
                            .get();
                    final var webContainer = apiDeployment.getSpec()
                            .getTemplate()
                            .getSpec()
                            .getContainers()
                            .stream()
                            .findFirst();
                    assertThat("Api container not found", webContainer.isPresent(), is(true));
                    assertThat("Api container image not valid", webContainer.get().getImage(), is(apiImage));
                    List<Integer> webContainerPorts = webContainer.get().getPorts().stream()
                            .map(ContainerPort::getContainerPort)
                            .toList();
                    assertTrue(webContainerPorts.contains(8080), "Api container port 8080 not found");

                    assertEquals(1, apiDeployment.getStatus().getReadyReplicas(), "Expected Api deployment number of replicas doesn't match");

                    // Api service
                    final var apiService = client.services()
                            .inNamespace(metadata.getNamespace())
                            .withName(ApiService.getServiceName(app))
                            .get();
                    final var apiServicePorts = apiService.getSpec()
                            .getPorts()
                            .stream()
                            .map(ServicePort::getPort)
                            .toList();
                    assertTrue(apiServicePorts.contains(8080), "Api service port not valid");

                    // Ingress
                    final var ingress = client.network().v1().ingresses()
                            .inNamespace(metadata.getNamespace())
                            .withName(ApiIngress.getIngressName(app))
                            .get();

                    final var rules = ingress.getSpec().getRules();
                    assertThat(rules.size(), is(1));

                    final var paths = rules.get(0).getHttp().getPaths();
                    assertThat(paths.size(), is(1));

                    final var path = paths.get(0);

                    final var serviceBackend = path.getBackend().getService();
                    assertThat(serviceBackend.getName(), is(ApiService.getServiceName(app)));
                    assertThat(serviceBackend.getPort().getNumber(), is(8080));
                });
    }
}