package org.tkit.onecx.workspace.api.bff.rs.v1;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Shell path mapping configuration
 */
@ConfigDocFilename("onecx-workspace-api-bff.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.path")
public interface PathConfig {

    /**
     * path mapping configuration
     */
    @WithName("shell-mapping")
    ShellMappingConfig shellMapping();

    interface ShellMappingConfig {

        /**
         * Enable or disable shell mapping
         */
        @WithDefault("false")
        @WithName("enabled")
        boolean enabled();

        /**
         * Prefix to be used for paths
         */
        @WithDefault("ui/")
        @WithName("prefix")
        String prefix();
    }
}
