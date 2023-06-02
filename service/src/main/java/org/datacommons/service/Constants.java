package org.datacommons.service;

import io.micronaut.core.util.StringUtils;

public class Constants {
    private static final String DCIMPORT_JAR_ARGUMENT_NAME = "dcimport.jar";

    public static final String DC_IMPORT_JAR_LOCATION = System.getProperty(DCIMPORT_JAR_ARGUMENT_NAME);

    static {
        if (StringUtils.isEmpty(DC_IMPORT_JAR_LOCATION)) {
            throw new IllegalStateException(String.format("%s argument not specified.", DCIMPORT_JAR_ARGUMENT_NAME));
        }
    }
}
