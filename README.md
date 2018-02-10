# jacoco-badge-maven-plugin ![Build Status](https://codebuild.us-east-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiYW50WHpPZStGUnVwT0VIWUpkUkhQZGVNWllGdWZuT3cvb3lVRk1ic0p6d0ZUdCt6ZWlyaDRub1E0b2lNNXlUdEQ2YlpBNEhXNTRsaDRBU3p2VnFXTENBPSIsIml2UGFyYW1ldGVyU3BlYyI6IkdjS0JTcFErUURac3VTbisiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master) ![Test Coverage](target/jacoco.svg)

A simple maven plugin to generate JaCoCo build badges

## Configuration

Before we can generate a JaCoCo test coverage badge, we need to run
JaCoCo test coverage!

JaCoCo measures test coverage of surefire unit tests and failsafe
integration tests. Both cases require that JaCoCo be configured to
integrate directly with the plugin in question.

### Configuring JaCoCo for Unit Tests

Here is a simple setup for surefire and JaCoCo that generates a
formatted HTML JaCoCo test coverage report.

    <!-- Set up surefire to run just unit tests and take
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>2.15</version>
      <configuration>
        <argLine>${surefireArgLine}</argLine>
        <skipTests>${skip.unit.tests}</skipTests>
        <excludes>
          <!-- Standard approach to excluding integration tests -->
          <exclude>**/IT*.java</exclude>
        </excludes>
      </configuration>
    </plugin>

    <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <version>0.8.0</version>
      <configuration>
        <excludes>
          <!-- Any exclusions you require, *.class -->
        </excludes>
      </configuration>
      <executions>
        <!-- Ask JaCoCo to generate a test report from surefire tests -->
        <execution>
          <id>pre-unit-test</id>
          <goals>
            <goal>prepare-agent</goal>
          </goals>
          <configuration>
            <destFile>${project.build.directory}/coverage-reports/jacoco-ut.exec</destFile>
            <propertyName>surefireArgLine</propertyName>
          </configuration>
        </execution>

        <!-- Ask JaCoCo to format test report into browsable HTML -->
        <execution>
          <id>post-unit-test</id>
          <phase>test</phase>
          <goals>
            <goal>report</goal>
          </goals>
          <configuration>
            <dataFile>${project.build.directory}/coverage-reports/jacoco-ut.exec</dataFile>
            <outputDirectory>${project.reporting.outputDirectory}/jacoco-ut</outputDirectory>
          </configuration>
        </execution>
      </executions>
    </plugin>

With this configuration, running `mvn test` should generate a friendly
HTML report of test coverage at `target/site/jacoco-ut/index.html`.

### Configuring Badge Generation

Once JaCoCo has been configured to generate a formatted report, it's
time to generate a badge from that report. Here is an example
configuration of this plugin to generate a badge based on unit tests:

    <plugin>
      <groupId>org.sigpwned</groupId>
      <artifactId>jacoco-badge-builder</artifactId>
      <version>0.1.0</version>
      <executions>
        <execution>
          <id>post-unit-test</id>
          <phase>test</phase>
          <goals>
            <goal>report</goal>
          </goals>
          <configuration>
            <!-- What coverage level is considered passing? Optional, default 70. -->
            <passing>70</passing>

            <!-- Legal values: instruction, branch, line, method. Optional, default instruction. -->
            <metric>instruction</metric>

            <!-- Where was the JaCoCo report generated? -->
            <jacocoOutputDirectory>${project.reporting.outputDirectory}/jacoco-ut</jacocoOutputDirectory>

            <!-- Where should the badge be generated? Optional, default ${project.reporting.outputDirectory} -->
            <badgeOutputDirectory>${project.reporting.outputDirectory}</badgetOutputDirectory>
          </configuration>
       </execution>
      </executions>
    </plugin>

## Including the Badge in your README

There are a couple of good ways to include the badge file into your
README:

* GitHub Only -- Generate the badge file into a resource directory,
  and then embed a reference to the badge into your README. This has
  the benefit of being very simple, but won't allow for the testing of
  pull requests, etc.

* CodeBuild -- Save the generated badge file as an artifact; configure
  S3 to send an event every time a badge is uploaded; use a lambda
  function to copy the latest badge to a known, fixed location; embed
  a link to that fixed location.
