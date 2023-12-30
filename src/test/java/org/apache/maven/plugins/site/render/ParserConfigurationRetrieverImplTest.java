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

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ParserConfigurationRetrieverImplTest {

    @Test
    public void testEmptyConfigurations() {
        ParserConfiguration config1 = new ParserConfiguration();
        ParserConfiguration config2 = new ParserConfiguration();
        assertEquals(
                Optional.empty(),
                new ParserConfigurationRetrieverImpl(Arrays.asList(config1, config2)).apply(Paths.get("some", "file")));
    }

    @Test
    public void testConfigurationWithInvalidPattern() {
        ParserConfiguration config1 = new ParserConfiguration();
        config1.addPattern("invalidprefix:*");
        ParserConfigurationRetrieverImpl parserConfigurationRetrieverImpl =
                new ParserConfigurationRetrieverImpl(Arrays.asList(config1));
        assertThrows(RuntimeException.class, () -> {
            parserConfigurationRetrieverImpl.apply(Paths.get("some", "file"));
        });
    }

    @Test
    public void testNonMatchingConfigurations() {
        ParserConfiguration config1 = new ParserConfiguration();
        config1.addPattern("glob:**/*.md");
        ParserConfiguration config2 = new ParserConfiguration();
        config2.addPattern("regex:.*\\.apt");
        assertEquals(
                Optional.empty(),
                new ParserConfigurationRetrieverImpl(Arrays.asList(config1, config2)).apply(Paths.get("some", "file")));
    }

    @Test
    public void testNonOverlappingConfigurations() {
        ParserConfiguration config1 = new ParserConfiguration();
        config1.addPattern("regex:.*\\.apt");
        ParserConfiguration config2 = new ParserConfiguration();
        config2.addPattern("glob:**/*");
        assertEquals(
                Optional.of(config2),
                new ParserConfigurationRetrieverImpl(Arrays.asList(config1, config2)).apply(Paths.get("some", "file")));
    }

    @Test
    public void testOverlappingConfigurations() {
        ParserConfiguration config1 = new ParserConfiguration();
        config1.addPattern("glob:**/*");
        ParserConfiguration config2 = new ParserConfiguration();
        config2.addPattern("regex:.*");
        assertEquals(
                Optional.of(config1),
                new ParserConfigurationRetrieverImpl(Arrays.asList(config1, config2)).apply(Paths.get("some", "file")));
    }
}
