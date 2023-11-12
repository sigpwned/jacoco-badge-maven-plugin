/*
 * Copyright 2023 JRichardsz
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
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.Test;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class BadgeMojoTest {

  @Test
  public void shouldFailIfCsvReportDoesNotExist() throws Exception {
    File reportFile = new File("foo/bar/jacoco.csv");
    
    File badgesFolder = new File(
        System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());

    BadgeMojo mojo = new BadgeMojo();

    Field badgesFolderInstance = BadgeMojo.class.getDeclaredField("badgesFolder");
    badgesFolderInstance.setAccessible(true);
    badgesFolderInstance.set(mojo, badgesFolder);

    Field reportFileInstance = BadgeMojo.class.getDeclaredField("reportFile");
    reportFileInstance.setAccessible(true);
    reportFileInstance.set(mojo, reportFile);

    try {
      mojo.execute();
      fail("My method didn't throw when I expected it to: csv does not exist");
    }catch(Exception e) {
      assertEquals(true, e.getMessage().startsWith("reportFile does not exist"));
    }

  }
  
  @Test
  public void shouldFailIfPassingIsOutOfHumanLimits() throws Exception {
    File reportFile = File.createTempFile("jacoco.", ".csv");
    reportFile.deleteOnExit();
    Files.write(Resources.toByteArray(Resources.getResource("jacoco.csv")), reportFile);
    
    File badgesFolder = new File(
        System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());

    BadgeMojo mojo = new BadgeMojo();

    Field badgesFolderInstance = BadgeMojo.class.getDeclaredField("badgesFolder");
    badgesFolderInstance.setAccessible(true);
    badgesFolderInstance.set(mojo, badgesFolder);

    Field reportFileInstance = BadgeMojo.class.getDeclaredField("reportFile");
    reportFileInstance.setAccessible(true);
    reportFileInstance.set(mojo, reportFile);
    
    Field passingInstance = BadgeMojo.class.getDeclaredField("passing");
    passingInstance.setAccessible(true);
    passingInstance.set(mojo, -1);    

    try {
      mojo.execute();
      fail("My method didn't throw when I expected it to: Invalid passing score");
    }catch(Exception e) {
      assertEquals(true, e.getMessage().startsWith("Invalid passing score"));
    }
    
    passingInstance = BadgeMojo.class.getDeclaredField("passing");
    passingInstance.setAccessible(true);
    passingInstance.set(mojo, 200);    

    try {
      mojo.execute();
      fail("My method didn't throw when I expected it to: Invalid passing score");
    }catch(Exception e) {
      assertEquals(true, e.getMessage().startsWith("Invalid passing score"));
    }    

  }  
  
  @Test
  public void shouldFailIfJascocoCsvHasInvalidHeaders() throws Exception {
    File reportFile = File.createTempFile("wrong_headers.", ".csv");
    reportFile.deleteOnExit();
    Files.write(Resources.toByteArray(Resources.getResource("wrong_headers.csv")), reportFile);
    
    File badgesFolder = new File(
        System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());

    BadgeMojo mojo = new BadgeMojo();

    Field badgesFolderInstance = BadgeMojo.class.getDeclaredField("badgesFolder");
    badgesFolderInstance.setAccessible(true);
    badgesFolderInstance.set(mojo, badgesFolder);

    Field reportFileInstance = BadgeMojo.class.getDeclaredField("reportFile");
    reportFileInstance.setAccessible(true);
    reportFileInstance.set(mojo, reportFile);

    try {
      mojo.execute();
      fail("My method didn't throw when I expected it to: Invalid jacoco csv headers");
    }catch(Exception e) {
      assertEquals(true, e.getMessage().startsWith("Failed to read coverage"));
    }
   

  }    

  @Test
  public void shouldGenerateOneBadge() throws Exception {

    File reportFile = File.createTempFile("jacoco.", ".csv");
    Files.write(Resources.toByteArray(Resources.getResource("jacoco.csv")), reportFile);

    File badgesFolder = new File(
        System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());

    BadgeMojo mojo = new BadgeMojo();

    Field badgesFolderInstance = BadgeMojo.class.getDeclaredField("badgesFolder");
    badgesFolderInstance.setAccessible(true);
    badgesFolderInstance.set(mojo, badgesFolder);

    Field reportFileInstance = BadgeMojo.class.getDeclaredField("reportFile");
    reportFileInstance.setAccessible(true);
    reportFileInstance.set(mojo, reportFile);

    Field metricInstance = BadgeMojo.class.getDeclaredField("metric");
    metricInstance.setAccessible(true);
    metricInstance.set(mojo, Metric.branch);

    mojo.execute();

    assertEquals(true, new File(badgesFolder + File.separator + "branch.svg").exists());
    reportFile.delete();
    badgesFolder.delete();

  }

  @Test
  public void shouldGenerateAllBadges() throws Exception {

    File reportFile = File.createTempFile("jacoco.", ".csv");
    Files.write(Resources.toByteArray(Resources.getResource("jacoco.csv")), reportFile);

    File badgesFolder = new File(
        System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());

    BadgeMojo mojo = new BadgeMojo();

    Field badgesFolderInstance = BadgeMojo.class.getDeclaredField("badgesFolder");
    badgesFolderInstance.setAccessible(true);
    badgesFolderInstance.set(mojo, badgesFolder);

    Field reportFileInstance = BadgeMojo.class.getDeclaredField("reportFile");
    reportFileInstance.setAccessible(true);
    reportFileInstance.set(mojo, reportFile);

    mojo.execute();

    assertEquals(true, new File(badgesFolder + File.separator + "branch.svg").exists());
    assertEquals(true, new File(badgesFolder + File.separator + "line.svg").exists());
    assertEquals(true, new File(badgesFolder + File.separator + "method.svg").exists());
    assertEquals(true, new File(badgesFolder + File.separator + "complexity.svg").exists());

    reportFile.delete();
    badgesFolder.delete();
  }
  
  @Test
  public void shouldGenerateRedBadge() throws Exception {

    File reportFile = File.createTempFile("jacoco_not_passing.", ".csv");
    Files.write(Resources.toByteArray(Resources.getResource("jacoco_not_passing.csv")), reportFile);

    File badgesFolder = new File(
        System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());

    BadgeMojo mojo = new BadgeMojo();

    Field badgesFolderInstance = BadgeMojo.class.getDeclaredField("badgesFolder");
    badgesFolderInstance.setAccessible(true);
    badgesFolderInstance.set(mojo, badgesFolder);

    Field reportFileInstance = BadgeMojo.class.getDeclaredField("reportFile");
    reportFileInstance.setAccessible(true);
    reportFileInstance.set(mojo, reportFile);

    Field metricInstance = BadgeMojo.class.getDeclaredField("metric");
    metricInstance.setAccessible(true);
    metricInstance.set(mojo, Metric.branch);
    
    Field passingInstance = BadgeMojo.class.getDeclaredField("passing");
    passingInstance = BadgeMojo.class.getDeclaredField("passing");
    passingInstance.setAccessible(true);
    passingInstance.set(mojo, 80);      

    mojo.execute();

    assertEquals(true, new File(badgesFolder + File.separator + "branch.svg").exists());
    reportFile.delete();
    badgesFolder.delete();

  }  

}
