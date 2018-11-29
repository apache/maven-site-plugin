package org.apache.maven.plugins.site.run;

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

import org.apache.maven.doxia.siterenderer.DocumentRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.site.render.AbstractSiteRenderingMojo;
import org.apache.maven.reporting.exec.MavenReportExecution;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Starts the site up, rendering documents as requested for faster editing.
 * It uses Jetty as the web server.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
@Mojo( name = "run", aggregator = true, requiresReports = true )
public class SiteRunMojo
    extends AbstractSiteRenderingMojo
{
    /**
     * Where to create the dummy web application.
     */
    @Parameter( defaultValue = "${project.build.directory}/site-webapp" )
    private File tempWebappDirectory;

    /**
     * The port to execute the HTTP server on.
     */
    @Parameter( property = "port", defaultValue = "8080" )
    private int port;

    private static final int MAX_IDLE_TIME = 30000;

    /**
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        checkInputEncoding();

        Server server = new Server( port );
        server.setStopAtShutdown( true );

        WebAppContext webapp = createWebApplication();
        webapp.setServer( server );

        server.setHandler( webapp );

        getLog().info( "Starting Jetty on http://localhost:" + port + "/" );
        try
        {
            server.start();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error executing Jetty: " + e.getMessage(), e );
        }

        // Watch it
        try
        {
            server.getThreadPool().join();
        }
        catch ( InterruptedException e )
        {
            getLog().warn( "Jetty was interrupted", e );
        }
    }

    private WebAppContext createWebApplication()
        throws MojoExecutionException
    {
        File webXml = new File( tempWebappDirectory, "WEB-INF/web.xml" );
        webXml.getParentFile().mkdirs();


        try ( InputStream inStream = getClass().getResourceAsStream( "/run/web.xml" ); //
            FileOutputStream outStream = new FileOutputStream( webXml ) )
        {
            IOUtil.copy( inStream, outStream );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to construct temporary webapp for running site", e );
        }

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath( "/" );
        webapp.setResourceBase( tempWebappDirectory.getAbsolutePath() );
        webapp.setAttribute( DoxiaFilter.SITE_RENDERER_KEY, siteRenderer );
        webapp.getInitParams().put( "org.mortbay.jetty.servlet.Default.useFileMappedBuffer", "false" );

        // For external reports
        project.getReporting().setOutputDirectory( tempWebappDirectory.getAbsolutePath() );
        for ( MavenReportExecution mavenReportExecution : getReports() )
        {
            mavenReportExecution.getMavenReport().setReportOutputDirectory( tempWebappDirectory );
        }

        List<MavenReportExecution> reports = getReports(); // TODO: is it sane to call getReports() method a second time?

        List<Locale> localesList = getLocales();
        webapp.setAttribute( DoxiaFilter.LOCALES_LIST_KEY, localesList );

        // Default is first in the list
        Locale defaultLocale = localesList.get( 0 );
        Locale.setDefault( defaultLocale );

        try
        {
            Map<String, DoxiaBean> i18nDoxiaContexts = new HashMap<>();

            for ( Locale locale : localesList )
            {
                SiteRenderingContext i18nContext = createSiteRenderingContext( locale );
                i18nContext.setInputEncoding( getInputEncoding() );
                i18nContext.setOutputEncoding( getOutputEncoding() );

                SiteRenderingContext i18nGeneratedSiteContext = createSiteRenderingContext( locale );
                i18nGeneratedSiteContext.setInputEncoding( getInputEncoding() );
                i18nGeneratedSiteContext.setOutputEncoding( getOutputEncoding() );
                i18nGeneratedSiteContext.getSiteDirectories().clear();

                Map<String, DocumentRenderer> i18nDocuments = locateDocuments( i18nContext, reports, locale );
                DoxiaBean doxiaBean;
                if ( defaultLocale.equals( locale ) )
                {
                    i18nGeneratedSiteContext.addSiteDirectory( generatedSiteDirectory );
                    doxiaBean = new DoxiaBean( i18nContext, i18nDocuments, i18nGeneratedSiteContext );
                }
                else
                {
                    i18nGeneratedSiteContext.addSiteDirectory( new File( generatedSiteDirectory,
                                                                         locale.getLanguage() ) );
                    doxiaBean = new DoxiaBean( i18nContext, i18nDocuments, i18nGeneratedSiteContext );
                }

                i18nDoxiaContexts.put( locale.getLanguage(), doxiaBean );
                if ( defaultLocale.equals( locale ) )
                {
                    i18nDoxiaContexts.put( "default", doxiaBean );
                }

                if ( defaultLocale.equals( locale ) )
                {
                    siteRenderer.copyResources( i18nContext, tempWebappDirectory );
                }
                else
                {
                    siteRenderer.copyResources( i18nContext, new File( tempWebappDirectory, locale.getLanguage() ) );
                }
            }

            webapp.setAttribute( DoxiaFilter.I18N_DOXIA_CONTEXTS_KEY, i18nDoxiaContexts );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to set up webapp", e );
        }
        return webapp;
    }

    public void setTempWebappDirectory( File tempWebappDirectory )
    {
        this.tempWebappDirectory = tempWebappDirectory;
    }

    public void setPort( int port )
    {
        this.port = port;
    }
}
