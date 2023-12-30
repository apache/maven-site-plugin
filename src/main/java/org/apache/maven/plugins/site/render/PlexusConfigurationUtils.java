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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.configuration.PlexusConfiguration;

public class PlexusConfigurationUtils {

    private PlexusConfigurationUtils() {
        // not supposed to be instantiated
    }

    /**
     * Retrieves all string values from the children of the given {@link PlexusConfiguration}
     * @param arrayContainer the configuration containing the array container (may be {@code null})
     * @return the list of string values
     */
    static List<String> getStringArrayValues(PlexusConfiguration arrayContainer) {
        if (arrayContainer == null) {
            return Collections.emptyList();
        }
        List<String> stringValues = new ArrayList<>();
        for (PlexusConfiguration item : arrayContainer.getChildren()) {
            stringValues.add(item.getValue());
        }
        return stringValues;
    }
}
