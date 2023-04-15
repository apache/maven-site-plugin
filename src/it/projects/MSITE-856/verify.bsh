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

import java.io.*;
import org.codehaus.plexus.util.*;

try
{
    final File siteDirectory = new File( basedir, "target/site" );
    if ( !siteDirectory.isDirectory() )
    {
        System.err.println( "site is missing or not a directory." );
        return false;
    }

    File sitemapHtmlFile = new File( siteDirectory, "sitemap.html" );
    if ( !sitemapHtmlFile.isFile() )
    {
        System.err.println( "no sitemap.html!" );
        return false;
    }

    content = FileUtils.fileRead( sitemapHtmlFile, "UTF-8" );
    if ( !content.contains( "<li>My first item</li>" ) )
    {
        System.err.println( "\"<li>My first item</li>\" not found in sitemap.html!" );
        return false;
    }

    File sitemapGermanHtmlFile = new File( siteDirectory, "de/sitemap.html" );
    if ( !sitemapGermanHtmlFile.isFile() )
    {
        System.err.println( "no German sitemap.html!" );
        return false;
    }

    content = FileUtils.fileRead( sitemapGermanHtmlFile, "UTF-8" );
    if ( !content.contains( "<li>Mein erstes Element</li>" ) )
    {
        System.err.println( "\"<li>Mein erstes Element</li>\" not found in German sitemap.html!" );
        return false;
    }
}
catch ( IOException e )
{
    e.printStackTrace();
    return false;
}

return true;
