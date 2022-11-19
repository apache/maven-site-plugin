
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

boolean result = true;

// TODO: also verify module links in generated files!

try
{
    // SITE

    File topLevelDirectory = new File( basedir, "target/site" );
    if ( !topLevelDirectory.exists() || !topLevelDirectory.isDirectory() )
    {
        System.err.println( "Site directory '" + topLevelDirectory + "' is missing or not a directory." );
        return false;
    }

    File moduleDirectory = new File( basedir, "mymodule/target/site" );
    if ( !moduleDirectory.exists() || !moduleDirectory.isDirectory() )
    {
        System.err.println( "Site module directory '" + moduleDirectory + "' is missing or not a directory." );
        return false;
    }

    File indexFile = new File( topLevelDirectory, "index.html" );
    if ( !indexFile.exists() || !indexFile.isFile() )
    {
        System.err.println( "Site index.html '" + indexFile + "' is missing or not a file." );
        return false;
    }
    String content = FileUtils.fileRead( indexFile, "UTF-8" );
    if ( !content.contains( "the following locales are supported: [, en, fr, de, de_DE, de_AT, zh_TW]" ) )
    {
        System.err.println( "index.html does not contain supported locales" );
        return false;
    }

    File projectInfoFile = new File( topLevelDirectory, "project-info.html" );
    if ( !projectInfoFile.exists() || !projectInfoFile.isFile() )
    {
        System.err.println( "Site project-info.html '" + projectInfoFile + "' is missing or not a file." );
        return false;
    }

    File frDirectory = new File( topLevelDirectory, "fr" );
    if ( !frDirectory.exists() || !frDirectory.isDirectory() )
    {
        System.err.println( "Site fr directory '" + frDirectory + "' is missing or not a directory." );
        return false;
    }

    File frProjectInfoFile = new File( frDirectory, "project-info.html" );
    if ( !frProjectInfoFile.exists() || !frProjectInfoFile.isFile() )
    {
        System.err.println( "Site fr project-info.html '" + frProjectInfoFile + "' is missing or not a file." );
        return false;
    }

    File frModuleDirectory = new File( moduleDirectory, "fr" );
    if ( !frModuleDirectory.exists() || !frModuleDirectory.isDirectory() )
    {
        System.err.println( "Site fr module directory '" + frModuleDirectory + "' is missing or not a directory." );
        return false;
    }

    File moduleProjectInfoFile = new File( moduleDirectory, "project-info.html" );
    if ( !moduleProjectInfoFile.exists() || !moduleProjectInfoFile.isFile() )
    {
        System.err.println( "Site module project-info.html '" + moduleProjectInfoFile + "' is missing or not a file." );
        return false;
    }

    File frModuleProjectInfoFile = new File( frModuleDirectory, "project-info.html" );
    if ( !frModuleProjectInfoFile.exists() || !frModuleProjectInfoFile.isFile() )
    {
        System.err.println( "Site fr module project-info.html '" + frModuleProjectInfoFile + "' is missing or not a file." );
        return false;
    }

    // STAGING

    File topLevelDirectory = new File( basedir, "target/staging" );
    if ( !topLevelDirectory.exists() || !topLevelDirectory.isDirectory() )
    {
        System.err.println( "Staging directory '" + topLevelDirectory + "' is missing or not a directory." );
        return false;
    }

    File moduleDirectory = new File( topLevelDirectory, "mymodule" );
    if ( !moduleDirectory.exists() || !moduleDirectory.isDirectory() )
    {
        System.err.println( "Staging module directory '" + moduleDirectory + "' is missing or not a directory." );
        return false;
    }

    File frDirectory = new File( topLevelDirectory, "fr" );
    if ( !frDirectory.exists() || !frDirectory.isDirectory() )
    {
        System.err.println( "Staging fr directory '" + frDirectory + "' is missing or not a directory." );
        return false;
    }

    File frModuleDirectory = new File( frDirectory, "mymodule" );
    if ( !frModuleDirectory.exists() || !frModuleDirectory.isDirectory() )
    {
        System.err.println( "Staging fr module directory '" + frModuleDirectory + "' is missing or not a directory." );
        return false;
    }

    // DEPLOY

    topLevelDirectory = new File( basedir, "deploy" );
    if ( !topLevelDirectory.exists() || !topLevelDirectory.isDirectory() )
    {
        System.err.println( "Deploy directory '" + topLevelDirectory + "' is missing or not a directory." );
        return false;
    }

    moduleDirectory = new File( topLevelDirectory, "mymodule" );
    if ( !moduleDirectory.exists() || !moduleDirectory.isDirectory() )
    {
        System.err.println( "Staging module directory '" + moduleDirectory + "' is missing or not a directory." );
        return false;
    }

    frDirectory = new File( topLevelDirectory, "fr" );
    if ( !frDirectory.exists() || !frDirectory.isDirectory() )
    {
        System.err.println( "Staging fr directory '" + frDirectory + "' is missing or not a directory." );
        return false;
    }

    frModuleDirectory = new File( frDirectory, "mymodule" );
    if ( !frModuleDirectory.exists() || !frModuleDirectory.isDirectory() )
    {
        System.err.println( "Staging fr module directory '" + frModuleDirectory + "' is missing or not a directory." );
        return false;
    }

    // STAGE DEPLOY

    topLevelDirectory = new File( topLevelDirectory, "staging" );
    if ( !topLevelDirectory.exists() || !topLevelDirectory.isDirectory() )
    {
        System.err.println( "Stage deploy directory '" + topLevelDirectory + "' is missing or not a directory." );
        return false;
    }

    moduleDirectory = new File( topLevelDirectory, "mymodule" );
    if ( !moduleDirectory.exists() || !moduleDirectory.isDirectory() )
    {
        System.err.println( "Staging module directory '" + moduleDirectory + "' is missing or not a directory." );
        return false;
    }

    frDirectory = new File( topLevelDirectory, "fr" );
    if ( !frDirectory.exists() || !frDirectory.isDirectory() )
    {
        System.err.println( "Staging fr directory '" + frDirectory + "' is missing or not a directory." );
        return false;
    }

    frModuleDirectory = new File( frDirectory, "mymodule" );
    if ( !frModuleDirectory.exists() || !frModuleDirectory.isDirectory() )
    {
        System.err.println( "Staging fr module directory '" + frModuleDirectory + "' is missing or not a directory." );
        return false;
    }
}
catch ( IOException e )
{
    e.printStackTrace();
    result = false;
}

return result;
