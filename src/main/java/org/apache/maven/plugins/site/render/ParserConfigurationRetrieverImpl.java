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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import org.apache.maven.doxia.siterenderer.ParserConfigurationRetriever;

public class ParserConfigurationRetrieverImpl implements ParserConfigurationRetriever {

    private final Collection<ParserConfiguration> parserConfigurations;

    public ParserConfigurationRetrieverImpl(Collection<ParserConfiguration> parserConfigurations) {
        this.parserConfigurations = parserConfigurations;
    }

    @Override
    public Optional<ParserConfiguration> apply(Path filePath) {
        return parserConfigurations.stream().filter(c -> c.matches(filePath)).findFirst();
    }
}
