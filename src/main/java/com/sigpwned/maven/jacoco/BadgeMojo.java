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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.sigpwned.maven.jacoco.util.Coverages;

/**
 * Generates a badge from JaCoCo test coverage reports.
 */
@Mojo( name = "badge", defaultPhase = LifecyclePhase.TEST )
public class BadgeMojo extends AbstractMojo {
    /**
     * What metric should be used to generate the badge: instruction, branch,
     * line, metric
     */
    @Parameter( defaultValue = "instruction", property = "metric", required = false )
    private Metric metric;
    
    /**
     * What coverage level is considered passing, from 0-100?
     */
    @Parameter( defaultValue = "70", property = "passing", required = false )
    private int passing;
    
    /**
     * Where was the report file generated?
     */
    @Parameter( defaultValue = "${project.build.directory}/jacoco-total.csv", property = "reportFile", required = false )
    private File reportFile;
    
    /**
     * Where should the badge be generated?
     */
    @Parameter( defaultValue = "${project.build.directory}/jacoco.svg", property = "outputFile", required = false )
    private File badgeFile;
    
    private static final String PASSING_COLOR="rgb(55,179,17)";

    private static final String FAILING_COLOR="rgb(192,64,49)";
    
    public void execute() throws MojoExecutionException {
        File badgeFile=this.badgeFile;
        if(!badgeFile.getParentFile().exists())
            badgeFile.getParentFile().mkdirs();
        
        File reportFile=this.reportFile;
        if(!reportFile.getParentFile().exists())
            reportFile.getParentFile().mkdirs();
        
        if(passing<0 || passing>100)
            throw new MojoExecutionException(
                passing,
                "Invalid passing score: "+passing,
                "Passing score must be from 0-100: "+passing);
        
        Coverage coverage;
        try {
            coverage = Coverages.report(reportFile, metric);
        }
        catch(IOException e) {
            throw new MojoExecutionException("Failed to read coverage", e);
        }

        int percent=(int)(100.0 * coverage.getPercent());
        
        boolean passed=percent >= passing;
        
        String template;
        try {
            try (InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream("template.svg")) {
                template = new String(read(in), StandardCharsets.UTF_8);
            }
        }
        catch(IOException e) { 
            throw new MojoExecutionException("Failed to load badge template", e);
        }
        
        String badge=template;
        badge = badge.replace("{{message}}", Integer.toString(percent)+"%");
        badge = badge.replace("{{color}}", passed ? PASSING_COLOR : FAILING_COLOR);
        
        try {
            try (OutputStream out=new FileOutputStream(badgeFile)) {
                out.write(badge.getBytes(StandardCharsets.UTF_8));
            }
        }
        catch(IOException e) {
            throw new MojoExecutionException("Failed to write badge", e);
        }
        
        getLog().info("Generated "+(passed ? "passing" : "failing")+" badge with "+percent+"% test coverage.");
    }
    
    private static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream result=new ByteArrayOutputStream();
        
        byte[] buf=new byte[64*1024];
        for(int nread=in.read(buf);nread!=-1;nread=in.read(buf))
            result.write(buf, 0, nread);
        
        return result.toByteArray();
    }
}
