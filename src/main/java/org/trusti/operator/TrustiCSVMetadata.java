package org.trusti.operator;

import io.quarkiverse.operatorsdk.annotations.CSVMetadata;
import io.quarkiverse.operatorsdk.annotations.SharedCSVMetadata;

@CSVMetadata(
        displayName = "Trusti Operator",
        permissionRules = {
                @CSVMetadata.PermissionRule(
                        apiGroups = {""},
                        resources = {"pods", "persistentvolumeclaims", "services", "configmaps", "secrets"},
                        verbs = {"*"}
                ),
                @CSVMetadata.PermissionRule(
                        apiGroups = {"route.openshift.io"},
                        resources = {"routes"},
                        verbs = {"*"}
                ),
                @CSVMetadata.PermissionRule(
                        apiGroups = {"networking.k8s.io"},
                        resources = {"ingresses"},
                        verbs = {"*"}
                ),
                @CSVMetadata.PermissionRule(
                        apiGroups = {"apps", "extensions"},
                        resources = {"deployments"},
                        verbs = {"*"}
                )
        },
        installModes = {
                @CSVMetadata.InstallMode(type = "OwnNamespace", supported = true),
                @CSVMetadata.InstallMode(type = "SingleNamespace", supported = false),
                @CSVMetadata.InstallMode(type = "MultiNamespace", supported = false),
                @CSVMetadata.InstallMode(type = "AllNamespaces", supported = false)
        },
        icon = @CSVMetadata.Icon(fileName = "icon.png", mediatype = "image/png")
)
public class TrustiCSVMetadata implements SharedCSVMetadata {
}
