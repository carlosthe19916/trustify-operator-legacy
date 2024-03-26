package org.trusti.operator;

import java.util.Map;

public class Constants {
    public static final String CRDS_GROUP = "org.trusti";
    public static final String CRDS_VERSION = "v1";

    public static final String CONTEXT_LABELS_KEY = "labels";

    //
    public static final String TRUSTI_NAME = "trusti";
    public static final String TRUSTI_API_NAME = "trusti-api";
    public static final String TRUSTI_DB_NAME = "trusti-db";

    //
    public static final Map<String, String> DB_SELECTOR_LABELS = Map.of(
            "trusti-operator/group", "db"
    );
    public static final Map<String, String> API_SELECTOR_LABELS = Map.of(
            "trusti-operator/group", "api"
    );

    //
    public static final Integer HTTP_PORT = 8080;
    public static final Integer HTTPS_PORT = 8443;
    public static final String SERVICE_PROTOCOL = "TCP";

    //
    public static final String DB_PVC_SUFFIX = "-" + TRUSTI_DB_NAME + "-pvc";
    public static final String DB_SECRET_SUFFIX = "-" + TRUSTI_DB_NAME + "-secret";
    public static final String DB_DEPLOYMENT_SUFFIX = "-" + TRUSTI_DB_NAME + "-deployment";
    public static final String DB_SERVICE_SUFFIX = "-" + TRUSTI_DB_NAME + "-service";

    public static final String API_DEPLOYMENT_SUFFIX = "-" + TRUSTI_API_NAME + "-deployment";
    public static final String API_SERVICE_SUFFIX = "-" + TRUSTI_API_NAME + "-service";


    public static final String INGRESS_SUFFIX = "-" + TRUSTI_API_NAME + "-ingress";
    public static final String INGRESS_SECURE_SUFFIX = "-" + TRUSTI_API_NAME + "-secure-ingress";

    //
    public static final String DB_SECRET_USERNAME = "username";
    public static final String DB_SECRET_PASSWORD = "password";
    public static final String DB_NAME = "trusti";

    public static final String POSTGRESQL_PVC_SIZE = "10G";

    public static final String CERTIFICATES_FOLDER = "/mnt/certificates";
    public static final String WORKSPACES_FOLDER = "/mnt/workspace";
}
