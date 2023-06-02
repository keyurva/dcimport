package org.datacommons.service;

import io.micronaut.core.util.StringUtils;
import io.micronaut.runtime.Micronaut;
import lombok.extern.slf4j.Slf4j;

import static org.datacommons.service.Constants.DC_IMPORT_JAR_LOCATION;

@Slf4j
public class Application {
  static {
    log.info("dcimport jar location: {}", DC_IMPORT_JAR_LOCATION);
  }

  public static void main(String[] args) {
    Micronaut.build(args).mainClass(Application.class).start();
  }
}
