
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

import java.util.jar.*

File target = new File( basedir, "target" )
assert target.exists() && target.isDirectory() : "target file is missing or not a directory."

File siteJar = new File( target, "site-jar-1.0-SNAPSHOT-site.jar" )
assert siteJar.exists() && !siteJar.isDirectory() : "siteJar file is missing or a directory."

String[] fileNames =  new String[] { "download.html", "index.html", "releases/release1.6.html" }

Set contents = new HashSet()

JarFile jar = new JarFile( siteJar )
Enumeration jarEntries = jar.entries()
long timestamp = -1
while ( jarEntries.hasMoreElements() )
{
    JarEntry entry = (JarEntry) jarEntries.nextElement()
    if ( timestamp == -1 )
    {
        timestamp = entry.getTime(); // reproducible timestamp in jar file cause local timestamp depending on timezone
    }
    assert entry.getTime() == timestamp : "wrong entry time for " + entry.getName() + ": " + entry.getTime() + " instead of " + timestamp
    if ( !entry.isDirectory() )
    {
        // Only compare files
        contents.add( entry.getName() )
    }
}

for ( int i = 0; i < fileNames.length; i++ )
{
    String fileName = fileNames[i]
    assert contents.contains( fileName ) : "File[" + fileName + "] not found in jar archive"
}
