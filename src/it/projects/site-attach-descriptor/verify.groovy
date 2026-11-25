
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

File target = new File(basedir, "target")
assert target.exists() && target.isDirectory() : "target file is missing or not a directory."

File siteDescriptor = new File( target, "site-attach-descriptor-1.0-SNAPSHOT-site.xml" )
assert siteDescriptor.exists() && !siteDescriptor.isDirectory() : "siteDescriptor file is missing from target or is a directory."

File siteDescriptorSwedish = new File( target, "site-attach-descriptor-1.0-SNAPSHOT-site_sv.xml" )
assert siteDescriptorSwedish.exists() && !siteDescriptorSwedish.isDirectory() : "siteDescriptorSwedish file is missing from target or is a directory."

File artifactsDirectory = new File( target, "snapshot-repo/org/apache/maven/plugins/site/its/site-attach-descriptor/1.0-SNAPSHOT" )
boolean pomSiteDescriptor = false
File[] files = artifactsDirectory.listFiles()
for ( int i = 0; i < files.length; i++ ) {
    String name = files[i].getName()
    // site-attach-descriptor-1.0-20090823.204852-1-site.xml
    if ( name.endsWith( "-site.xml" ) ) {
        pomSiteDescriptor = pomSiteDescriptor || name.startsWith( "site-attach-descriptor-1.0" )
    }
}
assert pomSiteDescriptor : "site descriptor not deployed for pom packaging."

artifactsDirectory = new File( target, "snapshot-repo/org/apache/maven/plugins/site/its/jar-site-attach-descriptor/1.0-SNAPSHOT" )
boolean jarSiteDescriptor = false
files = artifactsDirectory.listFiles()
for ( int i = 0; i < files.length; i++ ) {
    String name = files[i].getName()
    // jar-site-attach-descriptor-1.0-20090823.204852-1-site.xml
    if ( name.endsWith( "-site.xml" ) ) {
        jarSiteDescriptor = jarSiteDescriptor || name.startsWith( "jar-site-attach-descriptor-1.0" )
    }
}
assert !jarSiteDescriptor : "site descriptor deployed for jar packaging."