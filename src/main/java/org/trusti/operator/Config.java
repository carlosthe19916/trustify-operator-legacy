package org.trusti.operator;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "related.image")
public interface Config {

    @WithName("api")
    String apiImage();

    @WithName("db")
    String dbImage();

    @WithName("importer")
    String importerImage();

    @WithName("pull-policy")
    String imagePullPolicy();
}
