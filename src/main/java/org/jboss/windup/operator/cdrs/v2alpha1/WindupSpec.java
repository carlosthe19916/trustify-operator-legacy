package org.jboss.windup.operator.cdrs.v2alpha1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.SecretKeySelector;

import java.util.List;

public record WindupSpec(
        @JsonPropertyDescription("Number of instances of the executor pod. Default is 1.")
        int executorInstances,

        @JsonPropertyDescription("Size of the PVC where the reports will be stored")
        String dataSize,

        @JsonPropertyDescription("Secret(s) that might be used when pulling an image from a private container image registry or repository.")
        List<LocalObjectReference> imagePullSecrets,

        @JsonProperty("db")
        @JsonPropertyDescription("In this section you can find all properties related to connect to a database.")
        DatabaseSpec databaseSpec,

        @JsonProperty("hostname")
        @JsonPropertyDescription("In this section you can configure hostname and related properties.")
        HostnameSpec hostnameSpec,

        @JsonProperty("http")
        @JsonPropertyDescription("In this section you can configure Keycloak features related to HTTP and HTTPS")
        HttpSpec httpSpec,

        @JsonProperty("sso")
        @JsonPropertyDescription("In this section you can configure SSO settings.")
        SSOSpec ssoSpec,

        @JsonProperty("webResourceLimits")
        @JsonPropertyDescription("In this section you can configure resource limits settings for the Web Console.")
        ResourcesLimitSpec webResourceLimitSpec,

        @JsonProperty("executorResourceLimits")
        @JsonPropertyDescription("In this section you can configure resource limits settings for the Executor.")
        ResourcesLimitSpec executorResourceLimitSpec,

        @JsonProperty("jgroups")
        @JsonPropertyDescription("In this section you can configure JGroups settings.")
        JGroupsSpec jgroupsSpec
) {

    public WindupSpec() {
        this(
                1,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public record DatabaseSpec(
            @JsonPropertyDescription("Size of the PVC to create.")
            String size,

            @JsonProperty("resourceLimits")
            @JsonPropertyDescription("In this section you can configure resource limits settings.")
            ResourcesLimitSpec resourceLimitSpec
    ) {
    }

    public record HostnameSpec(
            @JsonPropertyDescription("Hostname for the server.")
            String hostname
    ) {
    }

    public record HttpSpec(
            @JsonPropertyDescription("A secret containing the TLS configuration for HTTPS. Reference: https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets.")
            String tlsSecret
    ) {
    }

    public record SSOSpec(
            @JsonPropertyDescription("Server url.")
            String serverUrl,

            @JsonPropertyDescription("Realm.")
            String realm,

            @JsonPropertyDescription("SSL required property. Valid values are: 'ALL', 'EXTERNAL', 'NONE'.")
            String sslRequired,

            @JsonPropertyDescription("Client id.")
            String clientId
    ) {
    }

    public record ResourcesLimitSpec(
            @JsonPropertyDescription("Requested CPU.")
            String cpuRequest,

            @JsonPropertyDescription("Limit CPU.")
            String cpuLimit,

            @JsonPropertyDescription("Requested memory.")
            String memoryRequest,

            @JsonPropertyDescription("Limit Memory.")
            String memoryLimit
    ) {
    }

    public record JGroupsSpec(
            @JsonPropertyDescription("The name of the secret containing the keystore file")
            String encryptSecret,

            @JsonPropertyDescription("The name of the keystore file within the secret")

            String encryptKeystore,

            @JsonPropertyDescription("The name associated with the server certificate.")
            String encryptName,

            @JsonPropertyDescription("The reference to a secret holding the password for the keystore and certificate.")
            SecretKeySelector encryptPassword,

            @JsonPropertyDescription("JGroups cluster password.")
            SecretKeySelector clusterPassword
    ) {
    }
}
