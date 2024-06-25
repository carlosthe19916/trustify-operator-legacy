FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build
COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/
USER quarkus
WORKDIR /code
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline
COPY src/main /code/src/main
RUN ./mvnw package -DskipTests

FROM scratch

# Core bundle labels.
LABEL operators.operatorframework.io.bundle.channel.default.v1=alpha
LABEL operators.operatorframework.io.bundle.channels.v1=alpha
LABEL operators.operatorframework.io.bundle.manifests.v1=manifests/
LABEL operators.operatorframework.io.bundle.mediatype.v1=registry+v1
LABEL operators.operatorframework.io.bundle.metadata.v1=metadata/
LABEL operators.operatorframework.io.bundle.package.v1=trustify-operator
LABEL operators.operatorframework.io.metrics.builder=qosdk-bundle-generator/6.6.6+6212e1b
LABEL operators.operatorframework.io.metrics.mediatype.v1=metrics+v1
LABEL operators.operatorframework.io.metrics.project_layout=quarkus.javaoperatorsdk.io/v1-alpha

# Copy files to locations specified by labels.
COPY --from=build /code/target/bundle/trustify-operator/manifests /manifests/
COPY --from=build /code/target/bundle/trustify-operator/metadata /metadata/
