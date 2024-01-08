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

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.maven.doxia.parser.Parser;

/** Configuration for a Doxia {@link Parser} (bound to a specific markup source path pattern)
 * @since 4.0.0
 **/
public class ParserConfiguration implements org.apache.maven.doxia.siterenderer.ParserConfiguration {

    /**
     * List of patterns in the format described at {@link FileSystem#getPathMatcher(String)}, i.e. {@code <syntax>:<pattern>}
     * where {@code <syntax} is either {@code glob} or {@code regex}.
     * If one of the patterns matches the file being parsed this configuration is applied.
     * @see FileSystem#getPathMatcher(String)
     * @see Pattern
     */
    private final List<String> patterns;

    /**
     * List of {@link PathMatcher}s for all of the {@link #patterns}. Lazily populated via {@link FileSystem#getPathMatcher(String)}.
     */
    private List<PathMatcher> matchers;

    private boolean emitComments;

    private boolean emitAnchorsForIndexableEntries;

    public ParserConfiguration() {
        patterns = new LinkedList<>();
        matchers = null;
    }

    /**
     * Switches the feature {@link Parser#setEmitComments(boolean)} either on or off.
     * Default (for Doxia Sitetools) is off.
     *
     * @param emitComments {@code true} to switch it on, otherwise leave it off
     * @see Parser#setEmitComments(boolean)
     */
    public void setEmitComments(boolean emitComments) {
        this.emitComments = emitComments;
    }

    /**
     * Switches the feature {@link Parser#setEmitAnchorsForIndexableEntries(boolean)} either on or off.
     * Default (for Doxia Sitetools) is on.
     *
     * @param emitAnchorsForIndexableEntries {@code true} to switch it on, otherwise leave it off
     * @see Parser#setEmitAnchorsForIndexableEntries(boolean)
     */
    public void setEmitAnchorsForIndexableEntries(boolean emitAnchorsForIndexableEntries) {
        this.emitAnchorsForIndexableEntries = emitAnchorsForIndexableEntries;
    }

    /**
     * A pattern in the format described at {@link FileSystem#getPathMatcher(String)}, i.e. {@code <syntax>:<pattern>}
     * where {@code <syntax} is either {@code glob} or {@code regex}.
     * If one of the patterns matches the file being parsed this configuration is applied.
     * @see FileSystem#getPathMatcher(String)
     * @see Pattern
     */
    public void addPattern(String pattern) {
        patterns.add(pattern);
    }

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
    public boolean matches(Path filePath) {
        if (matchers == null) {
            // lazily populate all matchers
            matchers = patterns.stream()
                    .map(p -> filePath.getFileSystem().getPathMatcher(p))
                    .collect(Collectors.toList());
        }
        return matchers.stream().anyMatch(m -> m.matches(filePath));
    }

    @Override
    public void configure(Parser parser) {
        parser.setEmitComments(emitComments);
        parser.setEmitAnchorsForIndexableEntries(emitAnchorsForIndexableEntries);
    }
}
