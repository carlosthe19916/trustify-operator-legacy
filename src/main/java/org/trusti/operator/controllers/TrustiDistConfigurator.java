package org.trusti.operator.controllers;

import io.fabric8.kubernetes.api.model.*;
import io.quarkus.logging.Log;
import org.trusti.operator.Constants;
import org.trusti.operator.cdrs.v2alpha1.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TrustiDistConfigurator {

    private final Trusti cr;

    private final List<EnvVar> allEnvVars;
    private final List<Volume> allVolumes;
    private final List<VolumeMount> allVolumeMounts;

    public TrustiDistConfigurator(Trusti cr) {
        this.cr = cr;
        this.allEnvVars = new ArrayList<>();
        this.allVolumes = new ArrayList<>();
        this.allVolumeMounts = new ArrayList<>();

        configureHttp();
        configureDatabase();
    }

    public List<EnvVar> getAllEnvVars() {
        return allEnvVars;
    }

    public List<Volume> getAllVolumes() {
        return allVolumes;
    }

    public List<VolumeMount> getAllVolumeMounts() {
        return allVolumeMounts;
    }

    private void configureHttp() {
        var optionMapper = optionMapper(cr.getSpec().httpSpec());

        configureTLS(optionMapper);

        List<EnvVar> envVars = optionMapper.getEnvVars();
        allEnvVars.addAll(envVars);
    }

    private void configureTLS(OptionMapper<TrustiSpec.HttpSpec> optionMapper) {
        final String certFileOptionName = "QUARKUS_HTTP_SSL_CERTIFICATE_FILE";
        final String keyFileOptionName = "QUARKUS_HTTP_SSL_CERTIFICATE_KEY_FILE";

        if (!ApiService.isTlsConfigured(cr)) {
            // for mapping and triggering warning in status if someone uses the fields directly
            optionMapper.mapOption(certFileOptionName);
            optionMapper.mapOption(keyFileOptionName);
            return;
        }

        optionMapper.mapOption(certFileOptionName, Constants.CERTIFICATES_FOLDER + "/tls.crt");
        optionMapper.mapOption(keyFileOptionName, Constants.CERTIFICATES_FOLDER + "/tls.key");

        optionMapper.mapOption("QUARKUS_HTTP_INSECURE_REQUESTS", "redirect");

        var volume = new VolumeBuilder()
                .withName("trusti-tls-certificates")
                .withNewSecret()
                .withSecretName(cr.getSpec().httpSpec().tlsSecret())
                .withOptional(false)
                .endSecret()
                .build();

        var volumeMount = new VolumeMountBuilder()
                .withName(volume.getName())
                .withMountPath(Constants.CERTIFICATES_FOLDER)
                .build();

        allVolumes.add(volume);
        allVolumeMounts.add(volumeMount);
    }

    private void configureDatabase() {
        String dbSecretName = DBSecret.getSecretName(cr);

        List<EnvVar> envVars = optionMapper(cr.getSpec())
                .mapOption("QUARKUS_DATASOURCE_USERNAME", spec -> new SecretKeySelector(Constants.DB_SECRET_USERNAME, dbSecretName, false))
                .mapOption("QUARKUS_DATASOURCE_PASSWORD", spec -> new SecretKeySelector(Constants.DB_SECRET_PASSWORD, dbSecretName, false))
                .mapOption("QUARKUS_DATASOURCE_JDBC_URL", spec -> DBService.getJdbcUrl(cr))
                .getEnvVars();

        allEnvVars.addAll(envVars);
    }

    private <T> OptionMapper<T> optionMapper(T optionSpec) {
        return new OptionMapper<>(optionSpec);
    }

    private static class OptionMapper<T> {
        private final T categorySpec;
        private final List<EnvVar> envVars;

        public OptionMapper(T optionSpec) {
            this.categorySpec = optionSpec;
            this.envVars = new ArrayList<>();
        }

        public List<EnvVar> getEnvVars() {
            return envVars;
        }

        public <R> OptionMapper<T> mapOption(String optionName, Function<T, R> optionValueSupplier) {
            if (categorySpec == null) {
                Log.debugf("No category spec provided for %s", optionName);
                return this;
            }

            R value = optionValueSupplier.apply(categorySpec);

            if (value == null || value.toString().trim().isEmpty()) {
                Log.debugf("No value provided for %s", optionName);
                return this;
            }

            EnvVarBuilder envVarBuilder = new EnvVarBuilder()
                    .withName(optionName);

            if (value instanceof SecretKeySelector) {
                envVarBuilder.withValueFrom(new EnvVarSourceBuilder().withSecretKeyRef((SecretKeySelector) value).build());
            } else {
                envVarBuilder.withValue(String.valueOf(value));
            }

            envVars.add(envVarBuilder.build());

            return this;
        }

        public <R> OptionMapper<T> mapOption(String optionName) {
            return mapOption(optionName, s -> null);
        }

        public <R> OptionMapper<T> mapOption(String optionName, R optionValue) {
            return mapOption(optionName, s -> optionValue);
        }

        protected <R extends Collection<?>> OptionMapper<T> mapOptionFromCollection(String optionName, Function<T, R> optionValueSupplier) {
            return mapOption(optionName, s -> {
                var value = optionValueSupplier.apply(s);
                if (value == null) return null;
                return value.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(","));
            });
        }
    }

}
