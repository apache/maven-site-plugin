package org.apache.maven.plugins.it;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Goal which creates several pages in a report.
 *
 * @goal test
 * @phase site
 */
public class MyReport
    extends AbstractMavenReport
{

    public String getOutputName()
    {
        return "MSITE-842";
    }

    public String getName( Locale locale )
    {
        return "MSITE-842";
    }

    public String getDescription( Locale locale )
    {
        return "Test Report for MSITE-842";
    }

    @Override
    protected void executeReport( Locale locale ) throws MavenReportException
    {
        // Main Sink
        Sink mainSink = getSink();
        if ( mainSink == null )
        {
            throw new MavenReportException("Could not get the main Sink");
        }

        // Write to the main Sink
        mainSink.text( "Main Sink" );

        // Create a new sink!
        Sink anotherSink;
        try
        {
            anotherSink = getSinkFactory().createSink( outputDirectory, "another-page.html" );
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "Could not create sink for another-page.html in " +
                    outputDirectory.getAbsolutePath(), e );
        }

        // Write to the other Sink
        anotherSink.text( "Another Sink" );

        // Create a new sink, in a subdirectory
        File subDirectory = new File( outputDirectory, "sub" );
        Sink subDirectorySink;
        try
        {
            subDirectorySink = getSinkFactory().createSink( subDirectory, "sub.html" );
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "Could not create sink for sub.html in " +
                    subDirectory.getAbsolutePath(), e );
        }

        // Write to the sink in the subdirectory
        subDirectorySink.text( "Subdirectory Sink" );

    }
}
