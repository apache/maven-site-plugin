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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractSiteRenderingMojoTest {

    @TempDir
    Path tempDir;

    @Test
    void outputDirectoryBelowTheProjectIsAccepted() {
        File basedir = tempDir.toFile();
        File site = tempDir.resolve("src/site").toFile();

        assertDoesNotThrow(() -> AbstractSiteRenderingMojo.checkInheritedSiteDirectory(
                tempDir.resolve("target/inherited-site").toFile(), site, basedir, null));
    }

    @Test
    void siteDirectoryAsInheritedSiteDirectoryIsRefused() {
        File site = tempDir.resolve("src/site").toFile();

        assertThrows(IOException.class, () -> AbstractSiteRenderingMojo.checkInheritedSiteDirectory(site, site));
    }

    @Test
    void directoryHoldingTheSiteDirectoryOrTheBasedirIsRefused() {
        File basedir = tempDir.toFile();
        File site = tempDir.resolve("src/site").toFile();

        assertThrows(
                IOException.class,
                () -> AbstractSiteRenderingMojo.checkInheritedSiteDirectory(
                        tempDir.resolve("src").toFile(), site));
        assertThrows(
                IOException.class,
                () -> AbstractSiteRenderingMojo.checkInheritedSiteDirectory(
                        tempDir.resolve("src/../.").toFile(), site, basedir));
    }
}
