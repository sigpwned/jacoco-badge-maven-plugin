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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CoverageReport implements AutoCloseable {
    public static class Row {
        public static Row fromStrings(List<String> fields) {
            String grp=fields.get(HEADERS.indexOf("GROUP"));
            String pkg=fields.get(HEADERS.indexOf("PACKAGE"));
            String cls=fields.get(HEADERS.indexOf("CLASS"));

            Map<Metric,Coverage> coverages=new EnumMap<>(Metric.class);
            for(Metric metric : Metric.values())
                coverages.put(metric, new Coverage(
                    Long.parseLong(fields.get(HEADERS.indexOf(metric.name().toUpperCase()+"_COVERED"))),
                    Long.parseLong(fields.get(HEADERS.indexOf(metric.name().toUpperCase()+"_MISSED")))));
            
            return new Row(grp, pkg, cls, coverages);
        }
        
        private final String grp;
        private final String pkg;
        private final String cls;
        private final Map<Metric,Coverage> coverages;
        
        public Row(String grp, String pkg, String cls, Map<Metric, Coverage> coverages) {
            this.grp = grp;
            this.pkg = pkg;
            this.cls = cls;
            this.coverages = Collections.unmodifiableMap(coverages);
        }

        public String getGroup() {
            return grp;
        }

        public String getPackage() {
            return pkg;
        }

        public String getKlass() {
            return cls;
        }
        
        public Coverage getCoverage(Metric metric) {
            return getCoverages().get(metric);
        }

        private Map<Metric, Coverage> getCoverages() {
            return coverages;
        }
        
        public List<String> toStrings() {
            List<String> result=new ArrayList<>();
            result.add(getGroup());
            result.add(getPackage());
            result.add(getKlass());
            for(Coverage coverage : getCoverages().values()) {
                result.add(Long.toString(coverage.getMissed()));
                result.add(Long.toString(coverage.getCovered()));
            }
            return Collections.unmodifiableList(result);
        }
    }
    
    public static final List<String> HEADERS=Arrays.asList(
        "GROUP", "PACKAGE", "CLASS", "INSTRUCTION_MISSED",
        "INSTRUCTION_COVERED", "BRANCH_MISSED", "BRANCH_COVERED",
        "LINE_MISSED", "LINE_COVERED", "COMPLEXITY_MISSED",
        "COMPLEXITY_COVERED", "METHOD_MISSED", "METHOD_COVERED");
    
    private final BufferedReader lines;
    
    public CoverageReport(Reader r) throws IOException {
        this(new BufferedReader(r));
    }
    
    public CoverageReport(BufferedReader lines) throws IOException {
        this.lines = lines;
        String line=lines.readLine();
        if(line == null)
            throw new EOFException();
        
        List<String> headers=Arrays.asList(line.trim().split(","));
        if(!getHeaders().equals(HEADERS))
            throw new IOException("unexpected headers; expected="+HEADERS+", found="+headers);
    }

    public CoverageReport.Row next() throws IOException {
        CoverageReport.Row result;
        
        String line=getLines().readLine();
        if(line != null)
            result = Row.fromStrings(Arrays.asList(line.split(",")));
        else
            result = null;
        
        return result;
    }
    
    public void close() throws IOException {
        getLines().close();
    }

    private BufferedReader getLines() {
        return lines;
    }

    private List<String> getHeaders() {
        return HEADERS;
    }
}
