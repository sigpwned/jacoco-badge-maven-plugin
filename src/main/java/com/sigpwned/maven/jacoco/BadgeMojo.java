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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.sigpwned.maven.jacoco.util.Coverages;

/**
 * Generates a badge from JaCoCo test coverage reports.
 */
@Mojo(name = "badge", defaultPhase = LifecyclePhase.VERIFY)
public class BadgeMojo extends AbstractMojo {
  /**
   * What metric should be used to generate the badge: instruction, branch, line, metric
   */
  @Parameter(property = "metric", required = false)
  private Metric metric;

  /**
   * What coverage level is considered passing, from 0-100?
   */
  @Parameter(defaultValue = "70", property = "passing", required = false)
  private int passing;

  /**
   * Where was the report file generated?
   */
  @Parameter(defaultValue = "${project.build.directory}/site/jacoco/jacoco.csv",
      property = "reportFile", required = false)
  private File reportFile;

  /**
   * Where should the badge be generated?
   */
  @Parameter(defaultValue = "${project.basedir}/.coverage",
      property = "badgesFolder", required = false)
  private File badgesFolder;

  private static final String PASSING_COLOR = "rgb(55,179,17)";

  private static final String FAILING_COLOR = "rgb(192,64,49)";

  @SuppressWarnings("unused")
  private static final String INFO_COLOR = "rgb(18,80,172)";

  // the skipTest family should be taken in account so we don't break the build

  @Parameter(property = "skipTests", defaultValue = "false")
  private boolean skipTests;

  @Parameter(property = "skipITs", defaultValue = "false")
  private boolean skipITs;

  @Deprecated
  @Parameter(property = "maven.test.skip.exec", defaultValue = "false")
  private boolean skipExec;

  @Parameter(property = "maven.test.skip", defaultValue = "false")
  private boolean skip;

  public void execute() throws MojoExecutionException {
    String template;
    try {
      try (InputStream in =
          Thread.currentThread().getContextClassLoader().getResourceAsStream("template.svg")) {
        template = new String(read(in), StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to load badge template", e);
    }

    File badgesFolderInstance = this.badgesFolder;
    if (!badgesFolderInstance.exists())
      badgesFolderInstance.mkdirs();

    File reportFile = this.reportFile;
    if (!reportFile.getParentFile().exists())
      reportFile.getParentFile().mkdirs();

    if (passing < 0 || passing > 100)
      throw new MojoExecutionException(passing, "Invalid passing score: " + passing,
          "Passing score must be from 0-100: " + passing);

    ArrayList<Metric> metrics = new ArrayList<>();
    if (metric == null) {
      metrics.add(Metric.branch);
      metrics.add(Metric.line);
      metrics.add(Metric.method);
      metrics.add(Metric.complexity);
    } else {
      metrics.add(metric);
    }

    for (Metric metricToGenerate : metrics) {
      Coverage coverage = null;
      int percent;

      if (skipTests || skipITs || skipExec || skip) {
        percent = 0;
        getLog().info("Tests skipped, generating zero coverage");
      } else {
        try {
          coverage = Coverages.report(reportFile, metricToGenerate);
        } catch (IOException e) {
          throw new MojoExecutionException("Failed to read coverage", e);
        }
        percent = (int) (100.0 * coverage.getPercent());
      }


      boolean passed = percent >= passing;

      String badge = template;
      badge = badge.replace("{{message}}", Integer.toString(percent) + "%");
      badge = badge.replace("{{color}}", passed ? PASSING_COLOR : FAILING_COLOR);
      badge = badge.replace("{{label}}", metricToGenerate.name()=="complexity"?"complex":metricToGenerate.name());

      String absoluteBadgePath =
          this.badgesFolder + File.separator + metricToGenerate.name() + ".svg";
      try {
        try (OutputStream out = new FileOutputStream(absoluteBadgePath)) {
          out.write(badge.getBytes(StandardCharsets.UTF_8));
        }
      } catch (IOException e) {
        throw new MojoExecutionException("Failed to write badge", e);
      }

      getLog().info(String.format("badge %s:%s pass:%s file:%s", metricToGenerate.name(),
          percent, passing, absoluteBadgePath));
    }
  }

  private static byte[] read(InputStream in) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();

    byte[] buf = new byte[64 * 1024];
    for (int nread = in.read(buf); nread != -1; nread = in.read(buf))
      result.write(buf, 0, nread);

    return result.toByteArray();
  }
}
