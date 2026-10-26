package org.apache.maven.plugins.site.it;

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

import org.apache.velocity.context.Context;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;

/**
 * A tool shaped like the Reflow skin's: configured through {@link SafeConfig}, reading its property and the
 * Velocity context.
 */
@DefaultKey( "configured" )
public class ConfiguredVelocityTool
    extends SafeConfig
{
    private Context context;

    private String greeting;

    @Override
    protected void configure( ValueParser values )
    {
        context = (Context) values.get( "velocityContext" );
        greeting = values.getString( "greeting" );
    }

    public String hello()
    {
        return greeting + " " + context.get( "currentFileName" );
    }
}
