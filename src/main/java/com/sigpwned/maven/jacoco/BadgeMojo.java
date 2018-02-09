/**
 * Copyright 2018 Andy Boothe
 * 
 *     Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sigpwned.maven.jacoco;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which generates a coverage badge for JaCoCo.
 */
@Mojo( name = "badge", defaultPhase = LifecyclePhase.TEST )
public class BadgeMojo extends AbstractMojo {
    public static enum Metric {
        instruction, branch, line, metric;
        
        public static Metric fromString(String s) {
            return valueOf(s.toLowerCase());
        }
        
        public String toString() {
            return name().toLowerCase();
        }
    }
    
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

        long covered=0, missed=0;
        try {
            try (BufferedReader csv=new BufferedReader(new InputStreamReader(new FileInputStream(new File(jacocoOutputDir, "jacoco.csv")), StandardCharsets.UTF_8))) {
                List<String> headers=Arrays.asList(csv.readLine().trim().split(","));
                
                String coveredName=metric.name().toUpperCase()+"_COVERED";
                int ci=headers.indexOf(coveredName);
                if(ci == -1)
                    throw new IOException("jacoco CSV report has no header: "+coveredName+", headers="+headers);
                
                String missedName=metric.name().toUpperCase()+"_MISSED";
                int mi=headers.indexOf(missedName);
                if(mi == -1)
                    throw new IOException("jacoco CSV report has no header: "+missedName+", headers="+headers);
                
                for(String line=csv.readLine();line!=null && !line.trim().equals("");line=csv.readLine()) {
                    List<String> fields=Arrays.asList(line.trim().split(","));
                    if(fields.size() != headers.size())
                        throw new IOException("jacoco CSV report has field/header mismatch: headers="+headers+", fields="+fields);
                    
                    long mycovered=Long.parseLong(fields.get(ci));
                    long mymissed=Long.parseLong(fields.get(mi));
                    
                    covered += mycovered;
                    missed += mymissed;
                }
            }
        }
        catch(IOException e) {
            throw new MojoExecutionException("Failed to read jacoco results", e);
        }
        
        int percent=Math.toIntExact(100L*covered/(covered+missed));
        
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
