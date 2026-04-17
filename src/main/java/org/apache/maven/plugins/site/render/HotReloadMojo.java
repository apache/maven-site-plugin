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
package org.apache.maven.plugins.site.render;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.doxia.site.inheritance.SiteModelInheritanceAssembler;
import org.apache.maven.doxia.siterenderer.DocumentRenderer;
import org.apache.maven.doxia.siterenderer.DoxiaDocumentRenderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.reporting.exec.MavenReportExecution;
import org.apache.maven.reporting.exec.MavenReportExecutor;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * Renders the site and watches for Doxia source file changes, re-rendering on modification.
 * The goal blocks until the user presses Enter.
 * <p>
 * This is intended for use during site development to provide immediate feedback on changes.
 * It only works on a single project and does neither consider child projects nor site staging.
 *
 * @since 3.22.0
 */
@Mojo(name = "hot-reload", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true, aggregator = true)
public class HotReloadMojo extends AbstractSiteRenderingMojo {

    /**
     * Directory where the project sites and report distributions will be generated (as html/css/...).
     */
    @Parameter(property = "siteOutputDirectory", defaultValue = "${project.reporting.outputDirectory}")
    protected File outputDirectory;

    /**
     * Polling interval in milliseconds for watching file changes.
     */
    @Parameter(property = "hot-reload.interval", defaultValue = "1000")
    private long pollingInterval;

    /**
     * Whether to generate reports during initial rendering.
     */
    @Parameter(property = "generateReports", defaultValue = "true")
    private boolean generateReports;

    @Inject
    public HotReloadMojo(
            SiteModelInheritanceAssembler assembler,
            SiteRenderer siteRenderer,
            MavenReportExecutor mavenReportExecutor,
            Prompter prompter) {
        super(assembler, siteRenderer, mavenReportExecutor);
        this.prompter = prompter;
    }

    private final Prompter prompter;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("maven.site.skip = true: Skipping site generation");
            return;
        }

        checkInputEncoding();

        try {
            List<Locale> localesList = getLocales();

            // Initial full render
            for (Locale locale : localesList) {
                getLog().info("Rendering site for "
                        + (!locale.equals(SiteTool.DEFAULT_LOCALE) ? "locale '" + locale + "'" : "default locale"));
                File localeOutputDirectory = getOutputDirectory(locale);
                List<MavenReportExecution> reports =
                        generateReports ? getReports(localeOutputDirectory) : Collections.emptyList();
                renderSite(locale, reports, localesList, localeOutputDirectory);
            }

            // Set up file watching
            FileAlterationMonitor monitor = new FileAlterationMonitor(pollingInterval);

            FileAlterationListener listener = new FileAlterationListenerAdaptor() {
                @Override
                public void onFileChange(File file) {
                    triggerRerender(file, localesList);
                }

                @Override
                public void onFileCreate(File file) {
                    triggerRerender(file, localesList);
                }

                @Override
                public void onFileDelete(File file) {
                    handleDeletion(file, localesList);
                }
            };

            // Watch siteDirectory
            if (siteDirectory != null && siteDirectory.isDirectory()) {
                FileAlterationObserver observer =
                        FileAlterationObserver.builder().setFile(siteDirectory).get();
                observer.addListener(listener);
                monitor.addObserver(observer);
                getLog().info("Watching for changes in " + siteDirectory);
            }

            // Watch generatedSiteDirectory
            if (generatedSiteDirectory != null && generatedSiteDirectory.isDirectory()) {
                FileAlterationObserver observer = FileAlterationObserver.builder()
                        .setFile(generatedSiteDirectory)
                        .get();
                observer.addListener(listener);
                monitor.addObserver(observer);
                getLog().info("Watching for changes in \"" + generatedSiteDirectory + "\"");
            }

            if (!mavenSession.getSettings().isInteractiveMode()) {
                getLog().info(
                                "Hot-reload: non-interactive mode detected, skipping file watching after initial render.");
            } else {
                monitor.start();
                getLog().info("Hot-reload is active. Press Enter to stop...");

                try {
                    prompter.prompt(System.lineSeparator());
                } catch (PrompterException e) {
                    throw new MojoExecutionException("Error waiting for user input", e);
                }

                monitor.stop();
                getLog().info("Hot-reload stopped.");
            }
        } catch (RendererException e) {
            throw new MojoExecutionException("Failed to render site", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during site generation", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Error in file monitor", e);
        }
    }

    private void handleDeletion(File deletedFile, List<Locale> localesList) {
        getLog().info("Deletion detected: \"" + deletedFile.getAbsolutePath() + "\" — removing rendered output...");
        try {
            for (Locale locale : localesList) {
                File localeOutputDirectory = getOutputDirectory(locale);
                SiteRenderingContext context = createSiteRenderingContext(locale);
                context.addSiteLocales(localesList);
                context.setInputEncoding(getInputEncoding());
                context.setOutputEncoding(getOutputEncoding());

                // determine the relative path of the deleted file against the watched source directories
                String relativePath = null;
                if (siteDirectory != null
                        && deletedFile.getAbsolutePath().startsWith(siteDirectory.getAbsolutePath())) {
                    relativePath = deletedFile
                            .getAbsolutePath()
                            .substring(siteDirectory.getAbsolutePath().length() + 1);
                } else if (generatedSiteDirectory != null
                        && deletedFile.getAbsolutePath().startsWith(generatedSiteDirectory.getAbsolutePath())) {
                    relativePath = deletedFile
                            .getAbsolutePath()
                            .substring(generatedSiteDirectory.getAbsolutePath().length() + 1);
                }

                if (relativePath != null) {
                    // replace the source extension with .html
                    int dotIndex = relativePath.lastIndexOf('.');
                    if (dotIndex > 0) {
                        // strip parser-specific subdirectory (e.g. "markdown/foo.md" -> "foo.html")
                        int separatorIndex = relativePath.indexOf(File.separatorChar);
                        String pathWithinParserDir =
                                separatorIndex > 0 ? relativePath.substring(separatorIndex + 1) : relativePath;
                        int dotIdx = pathWithinParserDir.lastIndexOf('.');
                        String outputRelativePath =
                                (dotIdx > 0 ? pathWithinParserDir.substring(0, dotIdx) : pathWithinParserDir) + ".html";
                        File outputFile = new File(localeOutputDirectory, outputRelativePath);
                        if (outputFile.exists()) {
                            if (outputFile.delete()) {
                                getLog().info("Deleted rendered output: \"" + outputFile.getAbsolutePath() + "\"");
                            } else {
                                getLog().warn("Failed to delete rendered output: \"" + outputFile.getAbsolutePath()
                                        + "\"");
                            }
                        } else {
                            getLog().debug("No rendered output found for \"" + deletedFile + "\"");
                        }
                    }
                } else {
                    getLog().debug("Deleted file not within watched directories: \"" + deletedFile + "\"");
                }
            }
        } catch (Exception e) {
            getLog().error("Error handling deletion: " + e.getMessage(), e);
        }
    }

    private void triggerRerender(File changedFile, List<Locale> localesList) {
        getLog().info("Change detected in \"" + changedFile.getAbsolutePath() + "\" — re-rendering...");
        try {
            for (Locale locale : localesList) {
                File localeOutputDirectory = getOutputDirectory(locale);
                SiteRenderingContext context = createSiteRenderingContext(locale);
                context.addSiteLocales(localesList);
                context.setInputEncoding(getInputEncoding());
                context.setOutputEncoding(getOutputEncoding());

                Map<String, DocumentRenderer> documents = locateDocuments(context, Collections.emptyList(), locale);

                // find only the document(s) whose source matches the changed file
                List<DocumentRenderer> affected = new ArrayList<>();
                for (DocumentRenderer doc : documents.values()) {
                    if (doc instanceof DoxiaDocumentRenderer) {
                        DoxiaDocumentRenderer doxiaDoc = (DoxiaDocumentRenderer) doc;
                        File sourceFile = new File(
                                doxiaDoc.getRenderingContext().getBasedir(),
                                doxiaDoc.getRenderingContext().getInputPath());
                        if (sourceFile.getAbsolutePath().equals(changedFile.getAbsolutePath())) {
                            affected.add(doc);
                        }
                    }
                }

                if (affected.isEmpty()) {
                    getLog().debug("No matching Doxia document found for \"" + changedFile + "\", re-rendering all.");
                    siteRenderer.render(documents.values(), context, localeOutputDirectory);
                } else {
                    getLog().info("Re-rendering " + affected.size() + " affected document(s).");
                    siteRenderer.render(affected, context, localeOutputDirectory);
                }
            }
            getLog().info("Re-render complete.");
        } catch (Exception e) {
            getLog().error("Error re-rendering site: " + e.getMessage(), e);
        }
    }

    private void renderSite(
            Locale locale, List<MavenReportExecution> reports, List<Locale> supportedLocales, File outputDirectory)
            throws IOException, RendererException, MojoFailureException, MojoExecutionException {
        SiteRenderingContext context = createSiteRenderingContext(locale);
        context.addSiteLocales(supportedLocales);
        context.setInputEncoding(getInputEncoding());
        context.setOutputEncoding(getOutputEncoding());

        Map<String, DocumentRenderer> documents = locateDocuments(context, reports, locale);

        siteRenderer.copyResources(context, outputDirectory);
        siteRenderer.render(documents.values(), context, outputDirectory);
    }

    private File getOutputDirectory(Locale locale) {
        File file;
        if (!locale.equals(SiteTool.DEFAULT_LOCALE)) {
            file = new File(outputDirectory, locale.toString());
        } else {
            file = outputDirectory;
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
