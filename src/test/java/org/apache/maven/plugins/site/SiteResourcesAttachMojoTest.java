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
package org.apache.maven.plugins.site;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.archiver.dir.DirectoryArchiver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SiteResourcesAttachMojo#createArchive}, which {@code SiteResourcesResolver} also uses to copy
 * the site resources of a parent in the same reactor.
 */
class SiteResourcesAttachMojoTest {

    @TempDir
    Path tempDir;

    private SiteResourcesAttachMojo createMojo(Set<String> includes) throws Exception {
        Path site = tempDir.resolve("src/site");
        Files.createDirectories(site.resolve("resources/images"));
        Files.createDirectories(site.resolve("markdown"));
        Files.write(site.resolve("site.xml"), Collections.singletonList("<site/>"));
        Files.write(site.resolve("site_sv.xml"), Collections.singletonList("<site/>"));
        Files.write(site.resolve("resources/images/logo.png"), new byte[] {1});
        Files.write(site.resolve("resources/.gitattributes"), Collections.singletonList("*"));
        Files.write(site.resolve("markdown/index.md"), Collections.singletonList("# Index"));

        SiteResourcesAttachMojo mojo = new SiteResourcesAttachMojo(null, null);
        mojo.siteDirectory = site.toFile();
        mojo.excludes = new HashSet<>(Collections.singleton("**/.gitattributes"));
        mojo.includes = includes;
        mojo.addDefaultExcludes = true;
        return mojo;
    }

    @Test
    void copiesResourcesButNoSiteDescriptors() throws Exception {
        SiteResourcesAttachMojo mojo = createMojo(null);
        File dest = tempDir.resolve("inherited").toFile();

        mojo.createArchive(new DirectoryArchiver(), dest);

        assertTrue(new File(dest, "resources/images/logo.png").isFile());
        assertTrue(new File(dest, "markdown/index.md").isFile());
        assertFalse(new File(dest, "site.xml").exists());
        assertFalse(new File(dest, "site_sv.xml").exists());
        assertFalse(new File(dest, "resources/.gitattributes").exists());
    }

    @Test
    void honorsIncludes() throws Exception {
        SiteResourcesAttachMojo mojo = createMojo(new HashSet<>(Collections.singleton("resources/**")));
        File dest = tempDir.resolve("inherited").toFile();

        mojo.createArchive(new DirectoryArchiver(), dest);

        assertTrue(new File(dest, "resources/images/logo.png").isFile());
        assertFalse(new File(dest, "markdown/index.md").exists());
    }
}
