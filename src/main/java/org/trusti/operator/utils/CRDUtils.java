package org.trusti.operator.utils;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import org.trusti.operator.cdrs.v2alpha1.Trusti;

import java.util.Optional;
import java.util.function.Function;

public class CRDUtils {

    public static OwnerReference getOwnerReference(Trusti cr) {
        return new OwnerReferenceBuilder()
                .withApiVersion(cr.getApiVersion())
                .withKind(cr.getKind())
                .withName(cr.getMetadata().getName())
                .withUid(cr.getMetadata().getUid())
                .withController(true)
                .build();
    }
    
    public static <T, R> Optional<R> getValueFromSubSpec(T subSpec, Function<T, R> valueSupplier) {
        if (subSpec != null) {
            return Optional.ofNullable(valueSupplier.apply(subSpec));
        } else {
            return Optional.empty();
        }
    }

}
