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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.siterenderer.ParserConfigurator;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * Configures a parser based on a {@link PlexusConfiguration} for a particular parser id and optionally matching one of multiple patterns.
 * It internally leverages the {@link ComponentConfigurator} for calling the right methods inside the parser implementation.
 */
public class ParserConfiguratorImpl implements ParserConfigurator, Closeable {

    private static final class ParserConfigurationKey {

        ParserConfigurationKey(String parserId, PlexusConfiguration patternsConfiguration) {
            this(parserId, PlexusConfigurationUtils.getStringArrayValues(patternsConfiguration));
        }

        ParserConfigurationKey(String parserId, Collection<String> patterns) {
            this.parserId = parserId;
            // lazily populate all matchers
            matchers = patterns.stream()
                    .map(p -> FileSystems.getDefault().getPathMatcher(p))
                    .collect(Collectors.toList());
        }

        private final String parserId;

        /**
         * List of {@link PathMatcher}s for all of the patterns passed to the constructor
         */
        private List<PathMatcher> matchers;

        /**
         * Returns {@code true} the given file path matches one of the {@link #patterns} given via {@link #addPattern(String)}
         * @param filePath the file path to check
         * @return {@code true} if the given file path matches at least one of the patterns, {@code false} otherwise.
         * @throws  IllegalArgumentException
         *          If one of the patterns does not comply with the form: {@code syntax:pattern}
         * @throws  java.util.regex.PatternSyntaxException
         *          If one of the regex patterns is invalid
         * @throws  UnsupportedOperationException
         *          If one of the patterns syntax prefix is not known to the implementation
         * @see FileSystem#getPathMatcher(String)
         */
        public boolean matches(String parserId, Path filePath) {
            if (this.parserId.equals(parserId)) {
                return false;
            }
            if (matchers.isEmpty()) {
                return true; // no patterns mean always match
            }
            return matchers.stream().anyMatch(m -> m.matches(filePath));
        }
    }

    private final org.codehaus.plexus.classworlds.realm.ClassRealm pluginClassRealm;
    private final PlexusContainer plexusContainer;
    private final ComponentConfigurator componentConfigurator;

    private final Map<ParserConfigurationKey, PlexusConfiguration> parserConfigurations;

    public ParserConfiguratorImpl(
            Map<String, List<PlexusConfiguration>> parserConfigurations,
            PlexusContainer plexusContainer,
            MojoDescriptor mojoDescriptor)
            throws ComponentLookupException {
        this.parserConfigurations = new LinkedHashMap<>();
        if (parserConfigurations != null) {
            for (Map.Entry<String, List<PlexusConfiguration>> parserConfigurationPerId :
                    parserConfigurations.entrySet()) {
                String parserId = parserConfigurationPerId.getKey();
                for (PlexusConfiguration configuration : parserConfigurationPerId.getValue()) {
                    ParserConfigurationKey key =
                            new ParserConfigurationKey(parserId, configuration.getChild("patterns"));
                    this.parserConfigurations.put(key, configuration);
                }
            }
        }
        pluginClassRealm = mojoDescriptor.getRealm();
        this.plexusContainer = plexusContainer;
        componentConfigurator = getComponentConfigurator(mojoDescriptor.getComponentConfigurator());
    }

    ComponentConfigurator getComponentConfigurator(String configuratorId) throws ComponentLookupException {
        // logic copied from
        // https://github.com/apache/maven/blob/267de063eec17111688fd1a27d4e3aae6c8d0c51/maven-core/src/main/java/org/apache/maven/plugin/internal/DefaultMavenPluginManager.java#L696C9-L700C10
        if (configuratorId == null || configuratorId.isEmpty()) {
            configuratorId = "basic"; // TODO: support v4
        }
        return plexusContainer.lookup(ComponentConfigurator.class, configuratorId);
    }

    public void configureParser(PlexusConfiguration configuration, Parser parser)
            throws ComponentConfigurationException {
        componentConfigurator.configureComponent(parser, configuration, pluginClassRealm);
    }

    @Override
    public void close() throws IOException {
        try {
            plexusContainer.release(componentConfigurator);
        } catch (ComponentLifecycleException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean configure(String parserId, Path filePath, Parser parser) {
        Optional<PlexusConfiguration> config = parserConfigurations.entrySet().stream()
                .filter(c -> c.getKey().matches(parserId, filePath))
                .findFirst()
                .map(Map.Entry::getValue);
        config.ifPresent(c -> {
            try {
                configureParser(c, parser);
            } catch (ComponentConfigurationException e) {
                throw new IllegalStateException("Could not configure parser " + parser, e);
            }
        });
        return config.isPresent();
    }
}
