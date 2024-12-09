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
import org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.site.SiteResourcesAttachMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.dir.DirectoryArchiver;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.StringUtils;
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
    private final ComponentConfigurator componentConfigurator;

    @Inject
    public SiteResourcesResolver(
            @Named(SiteResourcesAttachMojo.ARCHIVE_EXTENSION) UnArchiver unArchiver,
            RepositorySystem repoSystem,
            ComponentConfigurator componentConfigurator) {
        super();
        this.unArchiver = unArchiver;
        this.repoSystem = repoSystem;
        this.componentConfigurator = componentConfigurator;
    }

    public void resolveParentSiteResources(
            MavenSession mavenSession,
            MavenProject project,
            PluginDescriptor pluginDescriptor,
            File destDirectory,
            Log log)
            throws IOException {
        MavenProject parent = project.getParent();
        if (parent != null) {
            // start with the topmost one to allow child projects to override parent resources
            resolveParentSiteResources(mavenSession, parent, pluginDescriptor, destDirectory, log);
            // is it part of a multi-module project?
            if (parent.getBasedir() != null) {
                resolveLocalSiteResources(mavenSession, parent, pluginDescriptor, destDirectory, log);
            } else {
                resolveRepositorySiteResources(
                        RepositoryUtils.toArtifact(parent.getArtifact()),
                        mavenSession.getRepositorySession(),
                        parent.getRemoteProjectRepositories(),
                        destDirectory,
                        log);
            }
        }
    }

    protected void resolveRepositorySiteResources(
            Artifact artifact,
            RepositorySystemSession repoSession,
            List<RemoteRepository> remoteRepositories,
            File destDirectory,
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
                extractArchive(siteResourcesArtifact.getFile(), destDirectory);
            } catch (ArchiverException e) {
                throw new IOException("Error extracting archive " + siteResourcesArtifact.getFile(), e);
            }
            log.info("Copied inherited site resources from " + artifact + " to " + destDirectory);
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
            MavenSession mavenSession,
            MavenProject currentProject,
            PluginDescriptor pluginDescriptor,
            File destDirectory,
            Log log)
            throws IOException {
        // evaluate the configuration of the according mojo
        Plugin sitePlugin = currentProject.getPlugin("org.apache.maven.plugins:maven-site-plugin");
        // also evaluate pluginMgmt
        if (sitePlugin != null) {
            // just use the first potential execution's configuration
            Optional<Xpp3Dom> configuration = sitePlugin.getExecutions().stream()
                    .filter(e -> e.getGoals().contains(SiteResourcesAttachMojo.GOAL_NAME))
                    .map(e -> (Xpp3Dom) e.getConfiguration())
                    .findFirst();
            if (configuration.isPresent()) {
                try {
                    copySiteResourcesToDirectory(
                            mavenSession, currentProject, pluginDescriptor, configuration.get(), destDirectory, log);
                } catch (ArchiverException e1) {
                    throw new IOException("Error copying site resources to " + destDirectory, e1);
                } catch (ComponentConfigurationException e1) {
                    throw new IOException(
                            "Error extracting mojo configuration from " + currentProject.getArtifact(), e1);
                }
            } else {
                log.debug("No automatic execution of goal " + SiteResourcesAttachMojo.GOAL_NAME + " found in "
                        + currentProject.getArtifact());
            }
        } else {
            log.debug("No site plugin found in project " + currentProject.getArtifact());
        }
    }

    void copySiteResourcesToDirectory(
            MavenSession mavenSession,
            MavenProject project,
            PluginDescriptor pluginDescriptor,
            Xpp3Dom pomConfiguration,
            File destDirectory,
            Log log)
            throws ArchiverException, IOException, ComponentConfigurationException {
        // get mojo descriptor of sibling mojo!
        MojoDescriptor attachMojoDescriptor = pluginDescriptor.getMojo(SiteResourcesAttachMojo.GOAL_NAME);
        MojoExecution attachMojoExecution = new MojoExecution(attachMojoDescriptor, "implicit-attach-resources");
        SiteResourcesAttachMojo mojo = new SiteResourcesAttachMojo();
        // modify MavenSession to use the basedir of another project as context
        MavenSession mavenSessionForOtherProject = mavenSession.clone();
        mavenSessionForOtherProject.setCurrentProject(project);
        ExpressionEvaluator expressionEvaluator =
                new PluginParameterExpressionEvaluator(mavenSessionForOtherProject, attachMojoExecution);

        componentConfigurator.configureComponent(
                mojo,
                getEffectiveMojoConfiguration(pomConfiguration, attachMojoDescriptor),
                expressionEvaluator,
                pluginDescriptor.getClassRealm());
        // this is a pseudo archiver which just copies from source to destination considering the fileSet configuration
        DirectoryArchiver archiver = new DirectoryArchiver();
        mojo.createArchive(archiver, destDirectory);
        log.info("Copied inherited site resources from " + mojo.getSiteDirectory() + " to " + destDirectory);
    }

    /** Logic similar to {@link DefaultLifecycleExecutionPlanCalculator#finalizeMojoConfiguration},
     * i.e. merges pom configuration with mojo default configuration and removes configuration elements not belonging to mojo parameters.
     *
     * This will also remove configuration added via the {@link org.apache.maven.model.plugin.DefaultReportingConverter} in Maven 3.
     * This is {@code reportPlugins} and {@code outputDirectory} which cannot be injected into mojos.
     */
    private PlexusConfiguration getEffectiveMojoConfiguration(Xpp3Dom pomConfiguration, MojoDescriptor mojoDescriptor) {
        Xpp3Dom mojoConfiguration = createFromPlexusConfiguration(mojoDescriptor.getMojoConfiguration());
        Xpp3Dom finalConfiguration = new Xpp3Dom("configuration");

        if (mojoDescriptor.getParameters() != null) {
            for (Parameter parameter : mojoDescriptor.getParameters()) {
                Xpp3Dom parameterConfiguration = pomConfiguration.getChild(parameter.getName());

                if (parameterConfiguration == null) {
                    parameterConfiguration = pomConfiguration.getChild(parameter.getAlias());
                }

                Xpp3Dom parameterDefaults = mojoConfiguration.getChild(parameter.getName());

                parameterConfiguration = Xpp3Dom.mergeXpp3Dom(parameterConfiguration, parameterDefaults, Boolean.TRUE);

                if (parameterConfiguration != null) {
                    parameterConfiguration = new Xpp3Dom(parameterConfiguration, parameter.getName());

                    if (StringUtils.isEmpty(parameterConfiguration.getAttribute("implementation"))
                            && StringUtils.isNotEmpty(parameter.getImplementation())) {
                        parameterConfiguration.setAttribute("implementation", parameter.getImplementation());
                    }

                    finalConfiguration.addChild(parameterConfiguration);
                }
            }
        }
        return new XmlPlexusConfiguration(finalConfiguration);
    }

    /**
     * Converts a {@link PlexusConfiguration} to a new {@link Xpp3Dom}.
     * @param configuration
     * @return the converted configuration
     */
    private static Xpp3Dom createFromPlexusConfiguration(PlexusConfiguration configuration) {
        Xpp3Dom dom = new Xpp3Dom(configuration.getName());

        dom.setValue(configuration.getValue(null));

        String[] attributeNames = configuration.getAttributeNames();
        for (String attributeName : attributeNames) {
            dom.setAttribute(attributeName, configuration.getAttribute(attributeName));
        }

        PlexusConfiguration[] children = configuration.getChildren();
        for (PlexusConfiguration child : children) {
            dom.addChild(createFromPlexusConfiguration(child));
        }
        return dom;
    }
}
