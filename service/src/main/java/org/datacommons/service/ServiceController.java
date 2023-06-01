package org.datacommons.service;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
import static io.micronaut.http.MediaType.TEXT_PLAIN;

@Slf4j
@Controller
public class ServiceController {

  @Post(value = "/lint", consumes = MULTIPART_FORM_DATA, produces = TEXT_PLAIN)
  public HttpResponse<String> lint(CompletedFileUpload file) {
    log.info("lint {}", file.getFilename());
    try {
      Path tempDir = Files.createTempDirectory(UUID.randomUUID().toString());
      Path path = tempDir.resolve(file.getFilename());
      log.info("Writing to file: " + path.toAbsolutePath());
      Files.write(path, file.getBytes());
      lintProcess(path).waitFor();
      return HttpResponse.ok("lint success: " + path.toAbsolutePath());
    } catch (Exception e) {
      log.error("lint error", e);
      return HttpResponse.badRequest("Upload Failed");
    }
  }

  private static Process lintProcess(Path mcfPath) throws IOException {
    String[] cmd = {
      "java",
      "-jar",
      "/Users/keyur/dev/dcimport/datacommons-import-tool-0.1-alpha.1-jar-with-dependencies.jar",
      "lint",
      mcfPath.toAbsolutePath().toString()
    };
    log.info("Starting: {}", Arrays.toString(cmd));
    ProcessBuilder processBuilder = new ProcessBuilder(cmd).directory(mcfPath.getParent().toFile());
    return processBuilder.start();
  }

  @Get(value = "/hello", produces = TEXT_PLAIN)
  public String hello() {
    return "Hello world";
  }
}
