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
package org.apache.maven.plugins.site.run;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import org.apache.maven.doxia.site.inheritance.SiteModelInheritanceAssembler;
import org.apache.maven.doxia.siterenderer.DocumentRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.site.render.AbstractSiteRenderingMojo;
import org.apache.maven.reporting.exec.MavenReportExecution;
import org.apache.maven.reporting.exec.MavenReportExecutor;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;

/**
 * Starts the site up, rendering documents as requested for faster editing.
 * It uses the HTTP server provided by the JDK.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @see org.apache.maven.plugins.site.render.AutoRefreshMojo {@code auto-refresh} goal for automatic rerendering based on file system changes.
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.TEST, requiresReports = true)
public class SiteRunMojo extends AbstractSiteRenderingMojo {
    /**
     * Where to create the temporary site directory served by the HTTP server.
     */
    @Parameter(defaultValue = "${project.build.directory}/site-webapp")
    private File tempWebappDirectory;

    /**
     * The host to execute the HTTP server on.
     */
    @Parameter(property = "host", defaultValue = "localhost")
    private String host;

    /**
     * The port to execute the HTTP server on.
     */
    @Parameter(property = "port", defaultValue = "8080")
    private int port;

    @Inject
    public SiteRunMojo(
            SiteModelInheritanceAssembler assembler,
            SiteRenderer siteRenderer,
            MavenReportExecutor mavenReportExecutor) {
        super(assembler, siteRenderer, mavenReportExecutor);
    }

    /**
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkInputEncoding();

        tempWebappDirectory.mkdirs();

        Map<String, DoxiaBean> i18nDoxiaContexts = createDoxiaContexts();

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating HTTP server on " + host + ":" + port, e);
        }

        ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "site:run");
            thread.setDaemon(true);
            return thread;
        });
        server.setExecutor(executor);
        server.createContext(
                "/", new DoxiaHandler(tempWebappDirectory, siteRenderer, i18nDoxiaContexts, getLocales(), getLog()));

        server.start();

        int listeningPort = server.getAddress().getPort();
        getLog().info(buffer().a("Started site:run HTTP server on ")
                .strong("http://" + host + ":" + listeningPort + "/")
                .build());

        // Watch it
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime()
                .addShutdownHook(new Thread(
                        () -> {
                            server.stop(0);
                            executor.shutdown();
                            latch.countDown();
                        },
                        "site:run-shutdown"));

        try {
            latch.await();
        } catch (InterruptedException e) {
            getLog().warn("site:run server was interrupted", e);
        }
    }

    private Map<String, DoxiaBean> createDoxiaContexts() throws MojoExecutionException {
        Map<String, DoxiaBean> i18nDoxiaContexts;

        try {
            // For external reports
            project.getReporting().setOutputDirectory(tempWebappDirectory.getAbsolutePath());

            List<Locale> localesList = getLocales();
            i18nDoxiaContexts = new HashMap<>();

            for (Locale locale : localesList) {
                SiteRenderingContext i18nContext = createSiteRenderingContext(locale);
                i18nContext.setInputEncoding(getInputEncoding());
                i18nContext.setOutputEncoding(getOutputEncoding());

                File outputDirectory = getOutputDirectory(locale);
                List<MavenReportExecution> reports = getReports(outputDirectory);

                Map<String, DocumentRenderer> i18nDocuments = locateDocuments(i18nContext, reports, locale);
                DoxiaBean doxiaBean = new DoxiaBean(i18nContext, i18nDocuments);

                if (!locale.equals(SiteTool.DEFAULT_LOCALE)) {
                    i18nDoxiaContexts.put(locale.toString(), doxiaBean);
                    siteRenderer.copyResources(i18nContext, new File(tempWebappDirectory, locale.toString()));
                } else {
                    i18nDoxiaContexts.put("default", doxiaBean);
                    siteRenderer.copyResources(i18nContext, tempWebappDirectory);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to set up site:run contexts", e);
        }
        return i18nDoxiaContexts;
    }

    private File getOutputDirectory(Locale locale) {
        File file;
        if (!locale.equals(SiteTool.DEFAULT_LOCALE)) {
            file = new File(tempWebappDirectory, locale.toString());
        } else {
            file = tempWebappDirectory;
        }

        // Safety
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    public void setTempWebappDirectory(File tempWebappDirectory) {
        this.tempWebappDirectory = tempWebappDirectory;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
