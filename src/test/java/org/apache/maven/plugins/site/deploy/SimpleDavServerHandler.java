package org.apache.maven.plugins.site.deploy;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler.Context;
import org.eclipse.jetty.servlet.ServletHolder;
import org.apache.maven.plugins.site.deploy.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Olivier Lamy
 * @since 3.0-beta-2
 *
 */
public class SimpleDavServerHandler
{
    
    private Logger log = LoggerFactory.getLogger( getClass() );
    
    private Server server;
    
    private File siteTargetPath;
    
    List<HttpRequest> httpRequests = new ArrayList<HttpRequest>();
    
    public SimpleDavServerHandler(final File targetPath )
        throws Exception
    {
        this.siteTargetPath = targetPath;
        Handler repoHandler = new AbstractHandler()
        {
            public void handle( String target, Request r, HttpServletRequest request, HttpServletResponse response )
                throws IOException, ServletException
            {
                String targetPath = request.getPathInfo();
                
                HttpRequest rq = new HttpRequest();
                rq.method = request.getMethod();
                rq.path = targetPath;

                @SuppressWarnings( "rawtypes" )
                Enumeration headerNames = request.getHeaderNames();
                while ( headerNames.hasMoreElements() )
                {
                    String name = (String) headerNames.nextElement();
                    rq.headers.put( name, request.getHeader( name ) );
                }
                
                httpRequests.add( rq );
                
              
                if ( request.getMethod().equalsIgnoreCase( "PUT" ) )
                {
                    File targetFile = new File( siteTargetPath, targetPath );
                    log.info( "writing file " + targetFile.getPath() );
                    FileUtils.writeByteArrayToFile( targetFile, IOUtils.toByteArray( request.getInputStream() ) );
                }
                
                //PrintWriter writer = response.getWriter();

                response.setStatus( HttpServletResponse.SC_OK );

                ( (Request) request ).setHandled( true );
            }
        };
        server = new Server( 0 );
        server.setHandler( repoHandler );
        server.start();

    }

    public SimpleDavServerHandler( Servlet servlet )
        throws Exception
    {
        siteTargetPath = null;
        server = new Server( 0 );
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet( new ServletHolder( servlet ), "/" );

        server.start();
    }
    
    public int getPort()
    {
        return server.getURI().getPort();
    }

    public void stop()
        throws Exception
    {
        server.stop();
    }
    
    

}
