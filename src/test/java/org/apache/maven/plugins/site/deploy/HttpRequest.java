package org.apache.maven.plugins.site.deploy;

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

import org.apache.commons.lang3.SystemUtils;

import java.util.HashMap;
import java.util.Map;

class HttpRequest
{
    Map<String, String> headers = new HashMap<String,String>();

    String method;

    String path;

    HttpRequest()
    {
        // nop
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( method ).append( " path " ).append( path )
                .append( SystemUtils.LINE_SEPARATOR );
        for ( Map.Entry<String, String> entry : headers.entrySet() )
        {
            sb.append( entry.getKey() ).append( " : " ).append( entry.getValue() )
                    .append( SystemUtils.LINE_SEPARATOR );
        }
        return sb.toString();
    }
}