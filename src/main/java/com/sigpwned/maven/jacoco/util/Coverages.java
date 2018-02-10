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
package com.sigpwned.maven.jacoco.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.sigpwned.maven.jacoco.Coverage;
import com.sigpwned.maven.jacoco.CoverageReport;
import com.sigpwned.maven.jacoco.Metric;

public class Coverages {
    public static Coverage report(File file, Metric metric) throws IOException {
        long covered=0, missed=0;
        try (CoverageReport report=new CoverageReport(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            for(CoverageReport.Row row=report.next();row!=null;row=report.next()) {
                Coverage coverage=row.getCoverage(metric);
                covered += coverage.getCovered();
                missed += coverage.getMissed();
            }
        }
        return new Coverage(covered, missed);
    }
}
