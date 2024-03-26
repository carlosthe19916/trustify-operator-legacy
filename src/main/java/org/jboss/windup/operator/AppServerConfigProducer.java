package org.jboss.windup.operator;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.profile.IfBuildProfile;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

@Dependent
public class AppServerConfigProducer {

    @Produces
    @DefaultBean
    public AppServerConfig wildflyConfig() {
        return new AppServerConfig(
                new String[]{"/bin/sh", "-c", "${JBOSS_HOME}/bin/jboss-cli.sh --connect --commands=ls | grep 'server-state=running'"},
                new String[]{"/bin/sh", "-c", "${JBOSS_HOME}/bin/jboss-cli.sh --connect --commands='ls deployment' | grep 'api.war'"}
        );
    }

    @Produces
    @IfBuildProfile("eap")
    public AppServerConfig eapConfig() {
        return new AppServerConfig(
                new String[]{"/bin/sh", "-c", "/opt/eap/bin/livenessProbe.sh"},
                new String[]{"/bin/sh", "-c", "/opt/eap/bin/readinessProbe.sh"}
        );
    }

}
