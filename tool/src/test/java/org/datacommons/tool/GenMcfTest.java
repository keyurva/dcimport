// Copyright 2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.datacommons.tool;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import picocli.CommandLine;

public class GenMcfTest {
  @Rule public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void GenMcfTest_FatalTmcf() throws IOException {
    Main app = new Main();
    String tmcf = resourceFile("TmcfWithErrors.tmcf");
    String csv = resourceFile("Csv1.csv");
    CommandLine cmd = new CommandLine(app);
    cmd.execute("genmcf", tmcf, csv, "--output-dir=" + testFolder.getRoot().getPath());
    String actualReportString = TestUtil.getStringFromTestFile(testFolder, "report.json");
    String expectedReportString =
        TestUtil.getStringFromResource(this.getClass(), "GenMcfTest_FatalTmcfReport.json");
    TestUtil.assertReportFilesAreSimilar(expectedReportString, actualReportString);
    assertTrue(TestUtil.getStringFromTestFile(testFolder, "generated.mcf").isEmpty());
  }

  @Test
  public void GenMcfTest_SuccessTmcf() throws IOException {
    Main app = new Main();
    String tmcf = resourceFile("Tmcf1.tmcf");
    String csv = resourceFile("Csv1.csv");
    CommandLine cmd = new CommandLine(app);
    cmd.execute("genmcf", tmcf, csv, "--output-dir=" + testFolder.getRoot().getPath());
    String actualReportString = TestUtil.getStringFromTestFile(testFolder, "report.json");
    String expectedReportString =
        TestUtil.getStringFromResource(this.getClass(), "GenMcfTest_SuccessTmcfReport.json");
    Path actualGeneratedFilePath = Paths.get(testFolder.getRoot().getPath(), "generated.mcf");
    Path expectedGeneratedFilePath =
        Paths.get(this.getClass().getResource("GenMcfTest_SuccessTmcfGenerated.mcf").getPath());
    TestUtil.assertReportFilesAreSimilar(expectedReportString, actualReportString);
    assertTrue(areSimilarGeneratedMcf(expectedGeneratedFilePath, actualGeneratedFilePath));
  }

  private String resourceFile(String resource) {
    return this.getClass().getResource(resource).getPath();
  }

  // When testing GeneratedMcf, can't just check against an expected file because When generating
  // SVO MCF from csv and tmcf, Nodes will be assigned an ID that may not always be the same
  private boolean areSimilarGeneratedMcf(Path expectedFilePath, Path actualFilePath)
      throws IOException {
    Iterator<String> expectedFileLines = Files.lines(expectedFilePath).iterator();
    Iterator<String> actualFileLines = Files.lines(actualFilePath).iterator();
    while (expectedFileLines.hasNext() && actualFileLines.hasNext()) {
      String expectedLine = expectedFileLines.next();
      String actualLine = actualFileLines.next();
      if (expectedLine.contains("Node") && !expectedLine.contains("dcid")) {
        continue;
      }
      if (!actualLine.trim().equals(expectedLine.trim())) {
        return false;
      }
    }
    return true;
  }
}
