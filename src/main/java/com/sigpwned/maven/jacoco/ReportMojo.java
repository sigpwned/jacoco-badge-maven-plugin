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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Objects;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generates a single aggregate project test coverage report from the disparate
 * module-specific test coverage reports
 */
@Mojo( name = "report", defaultPhase = LifecyclePhase.TEST )
public class ReportMojo extends AbstractMojo {
    /**
     * What metric should be used to generate the badge: instruction, branch,
     * line, metric
     */
    @Parameter( defaultValue = "instruction", property = "metric", required = false )
    private Metric metric;
    
    /**
     * Where should the report be generated?
     */
    @Parameter( defaultValue = "${project.build.directory}/jacoco-total.csv", property = "reportFile", required = false )
    private File reportFile;
    
    /**
     * Where should the badge be generated?
     */
    @Parameter( defaultValue = "${project.basedir}", readonly=true )
    private File home;
    
    public void execute() throws MojoExecutionException {
        File reportFile=this.reportFile;
        if(!reportFile.getParentFile().exists())
            reportFile.getParentFile().mkdirs();
        
        try {
            try (OutputStream out=new FileOutputStream(reportFile)) {
                out.write(join(CoverageReport.HEADERS,",").getBytes(StandardCharsets.UTF_8));
                out.write('\n');
                
                Files.walkFileTree(home.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if(file.getFileName().toString().equals("jacoco.csv")) {
                            getLog().info("Found JaCoCo report file at: "+file);
                            try (CoverageReport report=new CoverageReport(new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8))) {
                                for(CoverageReport.Row row=report.next();row!=null;row=report.next()) {
                                    out.write(join(row.toStrings(), ",").getBytes(StandardCharsets.UTF_8));
                                    out.write('\n');
                                }
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
        catch(IOException e) {
            throw new MojoExecutionException("Failed to process JaCoCo reports", e);
        }
        
        getLog().info("JaCoCo report generated.");
    }
    
    private static <T> String join(Iterable<T> ts, String delimiter) {
        StringBuilder result=new StringBuilder();
        
        Iterator<T> iterator=ts.iterator();
        if(iterator.hasNext()) {
            result.append(Objects.toString(iterator.next()));
            while(iterator.hasNext()) {
                result.append(delimiter);
                result.append(Objects.toString(iterator.next()));
            }
        }
        
        return result.toString();
    }
}
