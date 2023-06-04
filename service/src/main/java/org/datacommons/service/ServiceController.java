package org.datacommons.service;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
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
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.micronaut.http.MediaType.*;
import static org.datacommons.service.Constants.DC_IMPORT_JAR_LOCATION;
import static org.datacommons.service.Constants.TEMP_DIRECTORY;

@Slf4j
@Controller
public class ServiceController {
  @Get("/file/{path:.+}")
  public StreamedFile file(@PathVariable String path) throws IOException {
    log.info("file: {}", path);
    return new StreamedFile(TEMP_DIRECTORY.resolve(path).toFile().toURI().toURL());
  }

  @Post(value = "/lint.json", consumes = MULTIPART_FORM_DATA)
  public LintResult lintJson(CompletedFileUpload file) {
    log.info("lint.json {}", file.getFilename());
    try {
      Path mcfPath = lintUploadedFile(file);
      return lintResult(mcfPath);
    } catch (Exception e) {
      log.error("lint.json error", e);
      return LintResult.builder().error("lint failure").build();
    }
  }

  @Post(value = "/lint", consumes = MULTIPART_FORM_DATA, produces = APPLICATION_OCTET_STREAM)
  public HttpResponse<byte[]> lint(CompletedFileUpload file) {
    log.info("lint {}", file.getFilename());
    try {
      Path mcfPath = lintUploadedFile(file);
      return HttpResponse.ok(zipResults(mcfPath));
    } catch (Exception e) {
      log.error("lint error", e);
      return HttpResponse.badRequest("lint failure".getBytes()).contentType(TEXT_PLAIN);
    }
  }

  private static Path lintUploadedFile(CompletedFileUpload file) throws Exception {
    Path workingDirectory =
        Files.createDirectory(TEMP_DIRECTORY.resolve(UUID.randomUUID().toString()));
    Path path = workingDirectory.resolve(file.getFilename());
    log.info("Writing to file: " + path.toAbsolutePath());
    Files.write(path, file.getBytes());
    lintProcess(path);
    return path;
  }

  private static int lintProcess(Path mcfPath) throws Exception {
    String[] cmd = {
      "java", "-jar", DC_IMPORT_JAR_LOCATION, "lint", mcfPath.toAbsolutePath().toString()
    };
    log.info("Starting: {}", Arrays.toString(cmd));
    return new ProcessBuilder(cmd).directory(mcfPath.getParent().toFile()).start().waitFor();
  }

  private static LintResult lintResult(Path mcfPath) throws IOException {
    LintResult.LintResultBuilder builder = LintResult.builder();
    Path resultsDir = mcfPath.getParent().resolve("dc_generated");
    Stream.of(resultsDir.toFile().listFiles())
        .filter(file -> file.isFile())
        .forEach(file -> builder.file("/file/" + TEMP_DIRECTORY.relativize(file.toPath())));
    log.info("lint result: {}", builder);
    return builder.build();
  }

  private static byte[] zipResults(Path mcfPath) throws IOException {
    Path resultsDir = mcfPath.getParent().resolve("dc_generated");
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
