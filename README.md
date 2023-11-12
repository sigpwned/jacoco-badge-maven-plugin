# jacoco-badge-maven-plugin

<p float="left">
  <img src="./.coverage/branch.svg">
  <img src="./.coverage/method.svg">
  <img src="./.coverage/line.svg">
  <img src="./.coverage/complexity.svg">
</p>

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

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>generate-code-coverage-report</id>
      <phase>test</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
    <execution>
      <id>check</id>
      <goals>
        <goal>check</goal>
      </goals>
      <configuration>
        <rules>
          <rule
            implementation="org.jacoco.maven.RuleConfiguration">
            <element>BUNDLE</element>
            <limits>
              <limit
                implementation="org.jacoco.report.check.Limit">
                <counter>INSTRUCTION</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.80</minimum>
              </limit>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

With this configuration, running `mvn test` should generate a friendly
HTML report of test coverage at `target/site/jacoco/index.html` and the most important file `/target/site/jacoco/jacoco.csv` which is required to get the badges percentages.

### Configuring Badge Generation

Once JaCoCo has been configured to generate a formatted report, it's
time to generate a badge from that report. Here is an example
configuration of this plugin to generate a badge based on unit tests:

```xml
<plugin>
  <groupId>com.sigpwned</groupId>
  <artifactId>jacoco-badge-maven-plugin</artifactId>
  <version>0.1.6</version>
  <executions>
    <execution>
      <id>generate-jacoco-badge</id>
      <phase>package</phase>
      <goals>
        <!-- Generate a badge from a unified JaCoCo report -->
        <goal>badge</goal>
      </goals>
      <configuration>
        <!-- jacoco csv report previously generated -->
        <reportFile>${project.build.directory}/site/jacoco/jacoco.csv</reportFile>
        <!-- target folder where badges will be generated -->
        <badgesFolder>${project.basedir}/.coverage</badgesFolder>
        <!-- minimum allowed threshold -->
        <passing>70</passing>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Including the Badge in your README

If you configured **<artifactId>jacoco-maven-plugin</artifactId>** to create the jacoco csv report in the **test** phase and the **jacoco-badge-maven-plugin** in the **package** phase, you only need to run `mvn clean install` which by maven definition executed the complation, tests, jacoco report and our badge generator plugin. You should see a log like:

```
[INFO] --- jacoco-badge:0.1.6:badge (generate-badge) @ jacoco-badge-maven-plugin ---
[INFO] badge branch:66 pass:70 file:/home/foo/jrichardsz/jacoco-easy-badger-maven-plugin/.coverage/branch.svg
[INFO] badge line:77 pass:70 file:/home/foo/jrichardsz/jacoco-easy-badger-maven-plugin/.coverage/line.svg
[INFO] badge method:77 pass:70 file:/home/foo/jrichardsz/jacoco-easy-badger-maven-plugin/.coverage/method.svg
[INFO] badge complexity:71 pass:70 file:/home/foo/jrichardsz/jacoco-easy-badger-maven-plugin/.coverage/complexity.svg
[INFO] 
```

So if the svg files are well generatd, there are a couple of good ways to include the badges files into your README:

**GitHub Only**

It is not a convention but you could storage the svg badges in the **.coverage** folder, add the follwing markdown piece (usually at the begining) and push changes

```
# acme-library

<p float="left">
  <img src="./.coverage/branch.svg">
  <img src="./.coverage/method.svg">
  <img src="./.coverage/line.svg">
  <img src="./.coverage/complexity.svg">
</p>

A simple tool from acme team.
```

Rendered:

![image](https://github.com/jrichardsz/jacoco-easy-badger-maven-plugin/assets/3322836/444cc7b7-67d5-4f95-87cb-b6172281aee8)

**CodeBuild**

Save the generated badge file as an artifact; configure S3 to send an event every time a badge is uploaded; use a lambda function to copy the latest badge to a known, fixed location; embed a link to that fixed location.