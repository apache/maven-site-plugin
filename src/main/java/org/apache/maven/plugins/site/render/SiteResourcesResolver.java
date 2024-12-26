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
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginContainerException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.site.SiteResourcesAttachMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.dir.DirectoryArchiver;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.transfer.ArtifactNotFoundException;

/**
 * Resolves and extracts/copies inherited site resources from parent projects.
 * @see SiteResourcesAttachMojo
 */
@Named
@Singleton
public class SiteResourcesResolver {

    private final UnArchiver unArchiver;
    private final RepositorySystem repoSystem;
    private final MavenPluginManager pluginManager;

    @Inject
    public SiteResourcesResolver(
            @Named(SiteResourcesAttachMojo.ARCHIVE_EXTENSION) UnArchiver unArchiver,
            RepositorySystem repoSystem,
            MavenPluginManager pluginManager) {
        super();
        this.unArchiver = unArchiver;
        this.repoSystem = repoSystem;
        this.pluginManager = pluginManager;
    }

    public void resolveParentSiteResources(
            MavenSession mavenSession, MavenProject project, File generatedSiteDirectory, Log log) throws IOException {
        MavenProject parent = project.getParent();
        if (parent != null) {
            // start with the topmost one to allow child projects to override parent resources
            resolveParentSiteResources(mavenSession, parent, generatedSiteDirectory, log);
            // is it part of a multi-module project?
            if (parent.getBasedir() != null) {
                resolveLocalSiteResources(mavenSession, parent, generatedSiteDirectory, log);
            } else {
                resolveRepositorySiteResources(
                        RepositoryUtils.toArtifact(parent.getArtifact()),
                        mavenSession.getRepositorySession(),
                        parent.getRemoteProjectRepositories(),
                        generatedSiteDirectory,
                        log);
            }
        }
    }

    protected void resolveRepositorySiteResources(
            Artifact artifact,
            RepositorySystemSession repoSession,
            List<RemoteRepository> remoteRepositories,
            File generatedSiteDirectory,
            Log log)
            throws IOException {
        final Artifact siteResourcesArtifact;
        try {
            siteResourcesArtifact = resolveSiteResources(artifact, repoSession, remoteRepositories);
        } catch (ArtifactResolutionException e) {
            throw new IOException("Error resolving site resources artifact for " + artifact, e);
        }
        if (siteResourcesArtifact != null) {
            try {
                extractArchive(siteResourcesArtifact.getFile(), generatedSiteDirectory);
            } catch (ArchiverException e) {
                throw new IOException("Error extracting archive " + siteResourcesArtifact.getFile(), e);
            }
            log.info("Copied inherited site resources from " + artifact + " to " + generatedSiteDirectory);
        } else {
            log.debug("No site resources found for " + artifact);
        }
    }

    protected Artifact resolveSiteResources(
            Artifact parentProjectArtifact,
            RepositorySystemSession repoSession,
            List<RemoteRepository> remoteRepositories)
            throws ArtifactResolutionException {
        try {
            Artifact siteResourcesArtifact = new DefaultArtifact(
                    parentProjectArtifact.getGroupId(),
                    parentProjectArtifact.getArtifactId(),
                    SiteResourcesAttachMojo.CLASSIFIER,
                    SiteResourcesAttachMojo.ARCHIVE_EXTENSION,
                    parentProjectArtifact.getVersion());
            ArtifactRequest request =
                    new ArtifactRequest(siteResourcesArtifact, remoteRepositories, "remote-site-resources");
            ArtifactResult result = repoSystem.resolveArtifact(repoSession, request);
            return result.getArtifact();
        } catch (ArtifactResolutionException e) {
            if (e.getCause() instanceof ArtifactNotFoundException) {
                // this is no error
                return null;
            }
            throw e;
        }
    }

    protected void extractArchive(File archiveFile, File destDirectory) throws ArchiverException {
        unArchiver.setSourceFile(archiveFile);
        unArchiver.setDestDirectory(destDirectory);
        unArchiver.setOverwrite(true);
        unArchiver.extract();
    }

    private void resolveLocalSiteResources(
            MavenSession mavenSession, MavenProject currentProject, File generatedSiteDirectory, Log log)
            throws IOException {
        // evaluate the configuration of the according mojo
        Plugin sitePlugin = currentProject.getPlugin("org.apache.maven.plugins:maven-site-plugin");
        if (sitePlugin != null) {
            // just use the first potential execution's configuration
            Optional<Xpp3Dom> configuration = sitePlugin.getExecutions().stream()
                    .filter(e -> e.getGoals().contains("attach-site-resources"))
                    .filter(e -> e.getPhase() != null && !e.getPhase().isEmpty())
                    .map(e -> (Xpp3Dom) e.getConfiguration())
                    .findFirst();
            if (configuration.isPresent()) {
                try {
                    copySiteResourcesToDirectory(
                            mavenSession, currentProject, sitePlugin, configuration.get(), generatedSiteDirectory, log);
                } catch (ArchiverException e1) {
                    throw new IOException("Error copying site resources to " + generatedSiteDirectory, e1);
                } catch (MojoNotFoundException
                        | PluginResolutionException
                        | PluginDescriptorParsingException
                        | InvalidPluginDescriptorException
                        | PluginContainerException
                        | PluginConfigurationException e1) {
                    throw new IOException(
                            "Error extracting mojo configuration from " + currentProject.getArtifact(), e1);
                }
            }
        }
    }

    void copySiteResourcesToDirectory(
            MavenSession mavenSession,
            MavenProject project,
            Plugin plugin,
            Xpp3Dom configuration,
            File destDirectory,
            Log log)
            throws ArchiverException, IOException, MojoNotFoundException, PluginResolutionException,
                    PluginDescriptorParsingException, InvalidPluginDescriptorException, PluginContainerException,
                    PluginConfigurationException {
        // modify MavenSession to use the basedir of another project as context
        MavenSession mavenSessionForOtherProject = mavenSession.clone();
        mavenSessionForOtherProject.setCurrentProject(project);

        MojoDescriptor mojoDescriptor = pluginManager.getMojoDescriptor(
                plugin,
                SiteResourcesAttachMojo.GOAL_NAME,
                project.getRemotePluginRepositories(),
                mavenSession.getRepositorySession());
        MojoExecution mojoExecution = new MojoExecution(mojoDescriptor, configuration);
        // TODO: use different baseDir for resolving default configuration
        SiteResourcesAttachMojo mojo = pluginManager.getConfiguredMojo(
                SiteResourcesAttachMojo.class, mavenSessionForOtherProject, mojoExecution);
        // this is a pseudo archiver which just copies from source to destination considering the fileSet configuration
        DirectoryArchiver archiver = new DirectoryArchiver();
        mojo.createArchive(archiver, destDirectory);
        log.info("Copied inherited site resources from " + mojo.getSiteDirectory() + " to " + destDirectory);
    }
}
