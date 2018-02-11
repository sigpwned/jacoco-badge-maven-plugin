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
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.sigpwned.maven.jacoco.util.Coverages;

/**
 * Fails the build if code coverage is lower than the given passing threshold
 */
@Mojo( name = "pass", defaultPhase = LifecyclePhase.TEST )
public class PassMojo extends AbstractMojo {
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
     * Where should the report be generated?
     */
    @Parameter( defaultValue = "${project.build.directory}/jacoco-total.csv", property = "reportFile", required = false )
    private File reportFile;
    
    public void execute() throws MojoExecutionException {
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
        
        getLog().info("jacoco coverage="+percent+" pass="+passing);
        
        if(!passed)
            throw new MojoExecutionException("Project did not pass sufficient test coverage: "+percent+"% < "+passing+"%");
    }
}
