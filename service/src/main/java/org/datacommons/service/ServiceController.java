package org.datacommons.service;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.micronaut.http.MediaType.*;
import static org.datacommons.service.Constants.DC_IMPORT_JAR_LOCATION;

@Slf4j
@Controller
public class ServiceController {
  @Post(value = "/lint", consumes = MULTIPART_FORM_DATA, produces = APPLICATION_OCTET_STREAM)
  public HttpResponse<byte[]> lint(CompletedFileUpload file) {
    log.info("lint {}", file.getFilename());
    try {
      Path tempDir = Files.createTempDirectory(UUID.randomUUID().toString());
      Path path = tempDir.resolve(file.getFilename());
      log.info("Writing to file: " + path.toAbsolutePath());
      Files.write(path, file.getBytes());
      lintProcess(path);
      return HttpResponse.ok(zipResults(tempDir));
    } catch (Exception e) {
      log.error("lint error", e);
      return HttpResponse.badRequest("lint failure".getBytes()).contentType(TEXT_PLAIN);
    }
  }

  private static int lintProcess(Path mcfPath) throws Exception {
    String[] cmd = {
      "java", "-jar", DC_IMPORT_JAR_LOCATION, "lint", mcfPath.toAbsolutePath().toString()
    };
    log.info("Starting: {}", Arrays.toString(cmd));
    return new ProcessBuilder(cmd).directory(mcfPath.getParent().toFile()).start().waitFor();
  }

  private static byte[] zipResults(Path workingDirectory) throws IOException {
    Path resultsDir = workingDirectory.resolve("dc_generated");
    log.info("Zipping results: " + resultsDir.toAbsolutePath());

    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    try (ZipOutputStream zout = new ZipOutputStream(bout)) {
      Files.walkFileTree(
          resultsDir,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              zout.putNextEntry(new ZipEntry(resultsDir.relativize(file).toString()));
              zout.write(Files.readAllBytes(file));
              zout.closeEntry();
              return FileVisitResult.CONTINUE;
            }
          });
    }

    return bout.toByteArray();
  }

  @Get(value = "/hello", produces = TEXT_PLAIN)
  public String hello() {
    return "Hello world";
  }
}
