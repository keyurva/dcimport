package org.datacommons.service;

import com.google.common.collect.ImmutableMap;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static io.micronaut.http.MediaType.APPLICATION_JSON_TYPE;
import static io.micronaut.http.MediaType.TEXT_HTML_TYPE;

public class Constants {
  private static final String DCIMPORT_JAR_ARGUMENT_NAME = "dcimport.jar";

  public static final String DC_IMPORT_JAR_LOCATION =
      System.getProperty(DCIMPORT_JAR_ARGUMENT_NAME);

  public static final Path TEMP_DIRECTORY;

  public static final ImmutableMap<String, MediaType> MEDIA_TYPES =
      ImmutableMap.<String, MediaType>builder()
          .put("json", APPLICATION_JSON_TYPE)
          .put("html", TEXT_HTML_TYPE)
          .buildOrThrow();

  static {
    try {
      TEMP_DIRECTORY = Files.createDirectory(Files.createTempDirectory("").resolve("dcimport"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (StringUtils.isEmpty(DC_IMPORT_JAR_LOCATION)) {
      throw new IllegalStateException(
          String.format("%s argument not specified.", DCIMPORT_JAR_ARGUMENT_NAME));
    }
  }
}
