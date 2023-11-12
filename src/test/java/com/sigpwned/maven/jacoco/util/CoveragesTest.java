package com.sigpwned.maven.jacoco.util;

import java.io.IOException;
import org.junit.Test;

public class CoveragesTest {

  //@TODO: com.sigpwned.maven.jacoco.util.Coverages should not be static
  @Test
  public void shouldComputeTheGlobalCoverageMetric() throws IOException {
    new Coverages();
  }
}
