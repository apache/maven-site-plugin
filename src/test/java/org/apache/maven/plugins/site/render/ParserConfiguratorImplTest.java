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

import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.doxia.parser.AbstractParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParserConfiguratorImplTest {

    /** Minimal parser, only the properties inherited from {@link AbstractParser} matter. */
    private static class TestParser extends AbstractParser {
        TestParser() {
            // neutral start values, independent of the defaults of the parser base class
            setEmitComments(false);
            setEmitAnchorsForIndexableEntries(false);
        }

        @Override
        public void parse(Reader source, Sink sink, String reference) throws ParseException {
            // not used
        }

        @Override
        public int getType() {
            return TXT_TYPE;
        }
    }

    private static final Path MARKDOWN_FILE = Paths.get("/project/src/site/markdown/index.md");
    private static final Path OTHER_FILE = Paths.get("/project/src/site/markdown/other/index.md");

    private PlexusContainer container;
    private ClassRealm realm;
    private MojoDescriptor mojoDescriptor;

    @BeforeEach
    void setUp() throws Exception {
        realm = new ClassWorld("test", getClass().getClassLoader()).newRealm("plugin");
        mojoDescriptor = new MojoDescriptor();
        mojoDescriptor.setRealm(realm);
        container = mock(PlexusContainer.class);
        // the same lookup Maven performs for a mojo without explicit configurator
        when(container.lookup(ComponentConfigurator.class, "basic")).thenReturn(new BasicComponentConfigurator());
    }

    @AfterEach
    void tearDown() throws Exception {
        realm.getWorld().disposeRealm("plugin");
    }

    private static PlexusConfiguration configuration(String xml) throws Exception {
        return new XmlPlexusConfiguration(
                org.codehaus.plexus.util.xml.Xpp3DomBuilder.build(new java.io.StringReader(xml)));
    }

    private ParserConfiguratorImpl createConfigurator(Map<String, List<PlexusConfiguration>> configurations)
            throws Exception {
        return new ParserConfiguratorImpl(configurations, container, mojoDescriptor);
    }

    private static Map<String, List<PlexusConfiguration>> single(String parserId, PlexusConfiguration... configs) {
        Map<String, List<PlexusConfiguration>> map = new LinkedHashMap<>();
        map.put(parserId, Arrays.asList(configs));
        return map;
    }

    @Test
    void appliesConfigurationOfMatchingParserWithoutPatterns() throws Exception {
        try (ParserConfiguratorImpl configurator = createConfigurator(single(
                "markdown", configuration("<configuration><emitComments>true</emitComments></configuration>")))) {
            TestParser parser = new TestParser();
            parser.setEmitComments(false);
            assertTrue(configurator.configure("markdown", MARKDOWN_FILE, parser));
            assertTrue(parser.isEmitComments());
        }
    }

    @Test
    void ignoresConfigurationOfOtherParser() throws Exception {
        try (ParserConfiguratorImpl configurator = createConfigurator(single(
                "markdown", configuration("<configuration><emitComments>true</emitComments></configuration>")))) {
            TestParser parser = new TestParser();
            assertFalse(configurator.configure("xdoc", MARKDOWN_FILE, parser));
            assertFalse(parser.isEmitComments());
        }
    }

    @Test
    void appliesConfigurationOnlyToMatchingPatterns() throws Exception {
        try (ParserConfiguratorImpl configurator = createConfigurator(single(
                "markdown",
                configuration("<configuration><patterns><pattern>glob:**/other/*.md</pattern></patterns>"
                        + "<emitComments>true</emitComments></configuration>")))) {
            TestParser notMatching = new TestParser();
            assertFalse(configurator.configure("markdown", MARKDOWN_FILE, notMatching));
            assertFalse(notMatching.isEmitComments());

            TestParser matching = new TestParser();
            assertTrue(configurator.configure("markdown", OTHER_FILE, matching));
            assertTrue(matching.isEmitComments());
        }
    }

    @Test
    void supportsRegexPatternsAndSeveralPatterns() throws Exception {
        try (ParserConfiguratorImpl configurator = createConfigurator(single(
                "markdown",
                configuration("<configuration><patterns><pattern>glob:**/nothing/*.md</pattern>"
                        + "<pattern>regex:.*/other/.*\\.md</pattern></patterns>"
                        + "<emitComments>true</emitComments></configuration>")))) {
            assertTrue(configurator.configure("markdown", OTHER_FILE, new TestParser()));
            assertFalse(configurator.configure("markdown", MARKDOWN_FILE, new TestParser()));
        }
    }

    @Test
    void firstMatchingConfigurationWins() throws Exception {
        try (ParserConfiguratorImpl configurator = createConfigurator(single(
                "markdown",
                configuration("<configuration><patterns><pattern>glob:**/other/*.md</pattern></patterns>"
                        + "<emitComments>true</emitComments></configuration>"),
                configuration("<configuration><emitComments>false</emitComments>"
                        + "<emitAnchorsForIndexableEntries>true</emitAnchorsForIndexableEntries></configuration>")))) {
            TestParser first = new TestParser();
            assertTrue(configurator.configure("markdown", OTHER_FILE, first));
            assertTrue(first.isEmitComments());
            assertFalse(first.isEmitAnchorsForIndexableEntries());

            TestParser second = new TestParser();
            assertTrue(configurator.configure("markdown", MARKDOWN_FILE, second));
            assertFalse(second.isEmitComments());
            assertTrue(second.isEmitAnchorsForIndexableEntries());
        }
    }

    @Test
    void withoutConfigurationNothingIsConfigured() throws Exception {
        try (ParserConfiguratorImpl configurator = createConfigurator(null)) {
            assertFalse(configurator.configure("markdown", MARKDOWN_FILE, new TestParser()));
        }
        try (ParserConfiguratorImpl configurator = createConfigurator(Collections.emptyMap())) {
            assertFalse(configurator.configure("markdown", MARKDOWN_FILE, new TestParser()));
        }
    }

    @Test
    void invalidPatternSyntaxIsRejectedEarly() throws Exception {
        PlexusConfiguration configuration =
                configuration("<configuration><patterns><pattern>**/*.md</pattern></patterns></configuration>");
        assertThrows(IllegalArgumentException.class, () -> createConfigurator(single("markdown", configuration)));
    }

    @Test
    void failureOfComponentConfiguratorIsReportedAsIllegalState() throws Exception {
        ComponentConfigurator failing = mock(ComponentConfigurator.class);
        doThrow(new ComponentConfigurationException("boom"))
                .when(failing)
                .configureComponent(any(), any(PlexusConfiguration.class), eq(realm));
        when(container.lookup(ComponentConfigurator.class, "basic")).thenReturn(failing);
        try (ParserConfiguratorImpl configurator = createConfigurator(single(
                "markdown", configuration("<configuration><emitComments>true</emitComments></configuration>")))) {
            assertThrows(
                    IllegalStateException.class,
                    () -> configurator.configure("markdown", MARKDOWN_FILE, new TestParser()));
        }
    }

    @Test
    void closeReleasesTheComponentConfigurator() throws Exception {
        ComponentConfigurator componentConfigurator = new BasicComponentConfigurator();
        when(container.lookup(ComponentConfigurator.class, "basic")).thenReturn(componentConfigurator);
        createConfigurator(null).close();
        verify(container).release(componentConfigurator);
    }

    @Test
    void usesConfiguratorIdOfMojoDescriptor() throws Exception {
        mojoDescriptor.setComponentConfigurator("custom");
        ComponentConfigurator custom = new BasicComponentConfigurator();
        when(container.lookup(ComponentConfigurator.class, "custom")).thenReturn(custom);
        try (ParserConfiguratorImpl configurator = createConfigurator(null)) {
            assertEquals(custom, configurator.getComponentConfigurator("custom"));
        }
    }

    @Test
    void stringArrayValues() throws Exception {
        assertEquals(
                Arrays.asList("a", "b"),
                PlexusConfigurationUtils.getStringArrayValues(
                        configuration("<patterns><pattern>a</pattern><pattern>b</pattern></patterns>")));
        assertTrue(PlexusConfigurationUtils.getStringArrayValues(null).isEmpty());
    }
}
