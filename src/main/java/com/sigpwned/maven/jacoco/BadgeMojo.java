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
 * Goal which generates a coverage badge for JaCoCo.
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
     * Where did JaCoCo generate its output?
     */
    @Parameter( property="jacocoOutputDir", required = true )
    private File jacocoOutputDir;
    
    /**
     * Where should the badge be generated?
     */
    @Parameter( defaultValue = "${project.build.outputDirectory}/jacoco.svg", property = "outputFile", required = false )
    private File outputFile;
    
    private static final String PASSING_COLOR="rgb(55,179,17)";

    private static final String FAILING_COLOR="rgb(192,64,49)";
    
    public void execute() throws MojoExecutionException {
        File sourceDirectory=jacocoOutputDir;
        if(!sourceDirectory.exists())
            throw new MojoExecutionException(
                sourceDirectory,
                "No jacoco directory: "+sourceDirectory.getPath(),
                "Jacoco output directory does not exist: "+sourceDirectory.getPath());
        
        File target=outputFile;
        if(!outputFile.getParentFile().exists())
            outputFile.getParentFile().mkdirs();
        
        if(passing<0 || passing>100)
            throw new MojoExecutionException(
                passing,
                "Invalid passing score: "+passing,
                "Passing score must be from 0-100: "+passing);
        
        String template;
        try {
            try (InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream("template.svg")) {
                template = new String(read(in), StandardCharsets.UTF_8);
            }
        }
        catch(IOException e) { 
            throw new MojoExecutionException("Failed to load badge template", e);
        }

        Coverage coverage;
        try {
            coverage = Coverages.report(new File(jacocoOutputDir, "jacoco.csv"), metric);
        }
        catch(IOException e) {
            throw new MojoExecutionException("Failed to read jacoco coverage report", e);
        }
        
        int percent=(int)(100.0 * coverage.getPercent());
        
        boolean passed=percent > passing;
        
        String badge=template;
        badge = badge.replace("{{percentage}}", Integer.toString(percent));
        badge = badge.replace("{{color}}", passed ? PASSING_COLOR : FAILING_COLOR);
        
        try {
            try (OutputStream out=new FileOutputStream(target)) {
                out.write(badge.getBytes(StandardCharsets.UTF_8));
            }
        }
        catch(IOException e) {
            throw new MojoExecutionException("Failed to write badge", e);
        }
    }
    
    private static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream result=new ByteArrayOutputStream();
        
        byte[] buf=new byte[64*1024];
        for(int nread=in.read(buf);nread!=-1;nread=in.read(buf))
            result.write(buf, 0, nread);
        
        return result.toByteArray();
    }
}
