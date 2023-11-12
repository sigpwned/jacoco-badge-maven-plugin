/*
 * Copyright 2018 Andy Boothe
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sigpwned.maven.jacoco;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sigpwned.maven.jacoco.util.Coverages;

public class CoverageReportTest {

  @Test
  public void shouldComputeTheGlobalCoverageMetric() throws IOException {
    File tmp = File.createTempFile("jacoco.", ".csv");
    try {
      Files.write(Resources.toByteArray(Resources.getResource("jacoco.csv")), tmp);

      Coverage coverage = Coverages.report(tmp, Metric.instruction);

      int percent = (int) (100.0 * coverage.getPercent());

      assertEquals(percent, 94);
    } finally {
      tmp.delete();
    }
  }

  @Test
  public void shouldComputeTheBranchMetric() throws IOException {
    File tmp = File.createTempFile("jacoco.", ".csv");
    try {
      Files.write(Resources.toByteArray(Resources.getResource("jacoco.csv")), tmp);

      Coverage coverage = Coverages.report(tmp, Metric.branch);

      int percent = (int) (100.0 * coverage.getPercent());

      assertEquals(percent, 80);
    } finally {
      tmp.delete();
    }
  }

  @Test
  public void shouldComputeTheFunctionsMetric() throws IOException {
    File tmp = File.createTempFile("jacoco.", ".csv");
    try {
      Files.write(Resources.toByteArray(Resources.getResource("jacoco.csv")), tmp);

      Coverage coverage = Coverages.report(tmp, Metric.method);

      int percent = (int) (100.0 * coverage.getPercent());

      assertEquals(percent, 100);
    } finally {
      tmp.delete();
    }
  }

  @Test
  public void shouldComputeTheLinesMetric() throws IOException {
    File tmp = File.createTempFile("jacoco.", ".csv");
    try {
      Files.write(Resources.toByteArray(Resources.getResource("jacoco.csv")), tmp);

      Coverage coverage = Coverages.report(tmp, Metric.line);

      int percent = (int) (100.0 * coverage.getPercent());

      assertEquals(percent, 92);
    } finally {
      tmp.delete();
    }
  }
  
  @Test
  public void shouldFailIfCsvReportHasNotExpectedHeaders() throws IOException {
    File tmp = File.createTempFile("wrong_headers.", ".csv");
    try {
      Files.write(Resources.toByteArray(Resources.getResource("wrong_headers.csv")), tmp);
      Coverages.report(tmp, Metric.line);
      fail("My method didn't throw when I expected it to: csv headers are wrong");
    }catch(Exception e) {
      assertEquals(true, e.getMessage().startsWith("unexpected headers"));
    }
    finally {
      tmp.delete();
    }
  }
  
}
