/*
 * Copyright 2018 Andy Boothe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sigpwned.maven.jacoco;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.Test;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sigpwned.maven.jacoco.util.Coverages;

public class CoverageReportTest {

    @Test
    public void shouldGenerateCoverageReport() throws IOException {
        File tmp = File.createTempFile("report.", ".csv");
        try {
            Files.write(Resources.toByteArray(Resources.getResource("report.csv")), tmp);

            Coverage coverage = Coverages.report(tmp, Metric.instruction);

            int percent = (int) (100.0 * coverage.getPercent());

            assertThat(percent, is(64));
        } finally {
            tmp.delete();
        }
    }

    @Test
    public void shouldGenerateZeroCoverage() throws Exception {
        BadgeMojo mojo = new BadgeMojo();

        Field skip = BadgeMojo.class.getDeclaredField("skip");
        skip.setAccessible(true);
        skip.set(mojo, true);

        File badge = File.createTempFile("coverage", ".svg");
        try {
            Field badgeFile = BadgeMojo.class.getDeclaredField("badgeFile");
            badgeFile.setAccessible(true);
            badgeFile.set(mojo, badge);
        } finally {
            badge.delete();
        }

        File csv = File.createTempFile("report.", ".csv");
        try {
            Field reportFile = BadgeMojo.class.getDeclaredField("reportFile");
            reportFile.setAccessible(true);
            reportFile.set(mojo, csv);
        } finally {
            csv.delete();
        }

        mojo.execute();

        Field lastCoverage = BadgeMojo.class.getDeclaredField("lastCoverage");
        lastCoverage.setAccessible(true);
        Coverage coverage = (Coverage) lastCoverage.get(mojo);
        assertEquals(coverage, null);

        Field lastPercent = BadgeMojo.class.getDeclaredField("lastPercent");
        lastPercent.setAccessible(true);
        int percent = (int) lastPercent.get(mojo);
        assertEquals(percent, 0);

    }
}
