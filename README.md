# jacoco-easy-badger-maven-plugin

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
          <id>prepare-code-coverage</id>
          <goals>
            <goal>prepare-agent</goal>
          </goals>
          <configuration>
            <propertyName>surefireArgLine</propertyName>
          </configuration>
        </execution>

        <!-- Ask JaCoCo to format test report into browsable HTML -->
        <!-- Multi-module builds should include report-aggregate and use the -->
        <!-- default outputDirectory. -->
        <!-- Single-module builds should exclude report-aggregate and use -->
        <!-- the given outputDirectory. -->
        <execution>
          <id>report-code-coverage</id>
          <goals>
            <goal>report</goal>
            <!-- <goal>report-aggregate</goal> -->
          </goals>
          <configuration>
            <!-- Multi-module builds should 
            <outputDirectory>${project.reporting.outputDirectory}/jacoco-aggregate</outputDirectory>
          </configuration>
        </execution>

        <!-- Make sure we have at least 70% coverage -->
        <execution>
          <id>verify-test-coverage</id>
          <goals>
            <goal>check</goal>
          </goals>
          <configuration>
            <rules>
              <rule>
                <element>BUNDLE</element>
                <excludes>
                  <exclude>*Mojo</exclude>
                </excludes>
                <limits>
                  <limit>
                    <counter>INSTRUCTION</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>70%</minimum>
                  </limit>
                </limits>
              </rule>
            </rules>
            <outputDirectory>${project.reporting.outputDirectory}/jacoco-aggregate</outputDirectory>
          </configuration>
        </execution>
      </executions>
    </plugin>

With this configuration, running `mvn test` should generate a friendly
HTML report of test coverage at `target/site/jacoco-aggregate/index.html`.

### Configuring Badge Generation

Once JaCoCo has been configured to generate a formatted report, it's
time to generate a badge from that report. Here is an example
configuration of this plugin to generate a badge based on unit tests:

```xml
<plugin>
  <groupId>com.jrichardsz</groupId>
  <artifactId>jacoco-easy-badger-maven-plugin</artifactId>
  <version>1.0.0</version>
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
        <!-- minimum threshold allowed  -->
        <passing>70</passing>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Including the Badge in your README

There are a couple of good ways to include the badge file into your
README:

**GitHub Only**

It is not a convention but you could storage the svg badges in the **.coverage** folder, add the follwing markdown piece (usually at the begining) and push changes

```
# sql-ops

<p float="left">
  <img src="./.coverage/branch.svg">
  <img src="./.coverage/method.svg">
  <img src="./.coverage/line.svg">
  <img src="./.coverage/complexity.svg">
</p>

A simple tool to execute any sql script.
```

Rendered:

![image](https://github.com/jrichardsz/jacoco-easy-badger-maven-plugin/assets/3322836/444cc7b7-67d5-4f95-87cb-b6172281aee8)

**CodeBuild**

Save the generated badge file as an artifact; configure S3 to send an event every time a badge is uploaded; use a lambda function to copy the latest badge to a known, fixed location; embed a link to that fixed location.

## Contributors

<table>
  <tbody>
    <td>
      <img src="https://avatars.githubusercontent.com/u/1236302?s=48&v=4" width="100px;"/>
      <br />
      <label><a href="http://sigpwned.com">Sigpwned</a></label>
      <br />
    </td>   
    <td>
      <img src="https://avatars0.githubusercontent.com/u/3322836?s=460&v=4" width="100px;"/>
      <br />
      <label><a href="http://jrichardsz.github.io/">JRichardsz</a></label>
      <br />
    </td>    
  </tbody>
</table>