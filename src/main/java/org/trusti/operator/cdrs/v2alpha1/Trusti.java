package org.trusti.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import org.trusti.operator.Constants;

@Group(Constants.CRDS_GROUP)
@Version(Constants.CRDS_VERSION)
public class Trusti extends CustomResource<TrustiSpec, TrustiStatus> implements Namespaced {

    @Override
    protected TrustiSpec initSpec() {
        return new TrustiSpec();
    }

    @Override
    protected TrustiStatus initStatus() {
        return new TrustiStatus();
    }

}

