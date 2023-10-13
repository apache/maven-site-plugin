
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

try
{
    File parentDirectory = new File( basedir, "parentNotAsRef" );
    if ( !parentDirectory.exists() || !parentDirectory.isDirectory() )
    {
        System.err.println( "parent is missing or not a directory." );
        result = false;
    }

    File parentSiteDirectory = new File( parentDirectory, "target/site" );
    if ( !parentSiteDirectory.exists() || !parentSiteDirectory.isDirectory() )
    {
        System.err.println( "parent site is missing or not a directory." );
        result = false;
    }

    File parentIndex = new File( parentSiteDirectory, "index.html" );
    if ( !parentIndex.exists() || parentIndex.isDirectory() )
    {
        System.err.println( "no index file in parent or is a directory." );
        result = false;
    }

    String content = FileUtils.fileRead( parentIndex, "UTF-8" );
    int index1 = content.indexOf( "<li class=\"active\"><a>Parent Relative Home Inherited</a></li>" );
    int index2 = content.indexOf( "<li class=\"active\"><a>Parent Relative Home Inherited with dot</a></li>" );
    int index3 = content.indexOf( "<li><a href=\"./\">Parent Absolute Home Inherited</a></li>" );
    int index4 = content.indexOf( "<li class=\"active\"><a>Parent Absolute Home Inherited with index</a></li>" );

    if ( index1 < 0 || index2 < 0 || index3 < 0 || index4 < 0 )
    {
        System.err.println( "parent index.html has wrong inherited menu items!" );
        result = false;
    }

    index1 = content.indexOf( "<li class=\"active\"><a>Parent Relative Home Local</a></li>" );
    index2 = content.indexOf( "<li class=\"active\"><a>Parent Relative Home Local with dot</a></li>" );
    index3 = content.indexOf( "<li><a href=\"./\">Parent Absolute Home Local</a></li>" );
    index4 = content.indexOf( "<li class=\"active\"><a>Parent Absolute Home Local with index</a></li>" );

    if ( index1 < 0 || index2 < 0 || index4 < 0 )
    {
        System.err.println( "parent index.html has wrong local menu items!" );
        result = false;
    }

    index1 = content.indexOf( "<li><a href=\"childNotAsRef/index.html\">Child Not As Ref</a></li>" );
    index2 = content.indexOf( "<li><a href=\"project-info.html\"><span class=\"icon-chevron-down\"></span>Project Information</a" );
    index3 = content.indexOf( "<li class=\"nav-header\">Parent Project</li>" );

    if ( index1 < 0 || index2 < 0 || index3 >= 0 )
    {
        System.err.println( "parent index.html has wrong reference menu items!" );
        result = false;
    }


    // CHILD


    File childDirectory = new File( parentDirectory, "childNotAsRef" );
    if ( !childDirectory.exists() || !childDirectory.isDirectory() )
    {
        System.err.println( "child is missing or not a directory." );
        result = false;
    }

    File childSiteDirectory = new File( childDirectory, "target/site" );
    if ( !childSiteDirectory.exists() || !childSiteDirectory.isDirectory() )
    {
        System.err.println( "child site is missing or not a directory." );
        result = false;
    }

    File childIndex = new File( childSiteDirectory, "index.html" );
    if ( !childIndex.exists() || childIndex.isDirectory() )
    {
        System.err.println( "no index file in child or is a directory." );
        result = false;
    }

    content = FileUtils.fileRead( childIndex, "UTF-8" );
    index1 = content.indexOf( "<a href=\"../index.html\">Parent Relative Home Inherited</a>" );
    index2 = content.indexOf( "<a href=\"../index.html\">Parent Relative Home Inherited with dot</a>" );
    index3 = content.indexOf( "<a href=\"../\">Parent Absolute Home Inherited</a>" );
    index4 = content.indexOf( "<a href=\"../index.html\">Parent Absolute Home Inherited with index</a>" );

    if ( index1 < 0 || index2 < 0 || index3 < 0 || index4 < 0 )
    {
        System.err.println( "child index.html has wrong menu items in inherited menu!" );
        result = false;
    }

    index1 = content.indexOf( "<li class=\"active\"><a>Child Relative Home Local</a></li>" );
    index2 = content.indexOf( "<li class=\"active\"><a>Child Relative Home Local with dot</a></li>" );
    index3 = content.indexOf( "<li><a href=\"./\">Child Absolute Home Local</a></li>" );
    index4 = content.indexOf( "<li class=\"active\"><a>Child Absolute Home Local with index</a></li>" );

    if ( index1 < 0 || index2 < 0 || index3 < 0 || index4 < 0 )
    {
        System.err.println( "child index.html has wrong menu items in local menu!" );
        result = false;
    }

    index1 = content.indexOf( "<a href=\"grandChildNotAsRef/index.html\">GrandChild Not As Ref</a>" );
    index2 = content.indexOf( "<a href=\"../index.html\">Parent Not As Ref</a>" );

    if ( index1 < 0 || index2 < 0 )
    {
        System.err.println( "child index.html has wrong reference menu items!" );
        result = false;
    }


    // GRANDCHILD


    File grandChildDirectory = new File( childDirectory, "grandChildNotAsRef" );
    if ( !grandChildDirectory.exists() || !grandChildDirectory.isDirectory() )
    {
        System.err.println( "grandchild is missing or not a directory." );
        result = false;
    }

    File grandChildSiteDirectory = new File( grandChildDirectory, "target/site" );
    if ( !grandChildSiteDirectory.exists() || !grandChildSiteDirectory.isDirectory() )
    {
        System.err.println( "grandchild site is missing or not a directory." );
        result = false;
    }

    File grandChildIndex = new File( grandChildSiteDirectory, "index.html" );
    if ( !grandChildIndex.exists() || grandChildIndex.isDirectory() )
    {
        System.err.println( "no index file in grandchild or is a directory." );
        result = false;
    }

    content = FileUtils.fileRead( grandChildIndex, "UTF-8" );
    index1 = content.indexOf( "a href=\"../../index.html\">Parent Relative Home Inherited</a>" );
    index2 = content.indexOf( "<a href=\"../../index.html\">Parent Relative Home Inherited with dot</a>" );
    index3 = content.indexOf( "<a href=\"../../\">Parent Absolute Home Inherited</a>" );
    index4 = content.indexOf( "<a href=\"../../index.html\">Parent Absolute Home Inherited with index</a>" );

    if ( index1 < 0 || index2 < 0 || index3 < 0 || index4 < 0 )
    {
        System.err.println( "grandchild index.html has wrong menu items in inherited menu!" );
        result = false;
    }

    index1 = content.indexOf( "<li class=\"active\"><a>Grand Child Relative Home Local</a></li>" );
    index2 = content.indexOf( "<li class=\"active\"><a>Grand Child Relative Home Local with dot</a></li>" );
    index3 = content.indexOf( "<li><a href=\"./\">Grand Child Absolute Home Local</a></li>" );
    index4 = content.indexOf( "<li class=\"active\"><a>Grand Child Absolute Home Local with index</a></li>" );

    if ( index1 < 0 || index2 < 0 || index3 < 0 || index4 < 0 )
    {
        System.err.println( "grandchild index.html has wrong menu items in local menu!" );
        result = false;
    }

    index1 = content.indexOf( "<a href=\"../index.html\">Child Not As Ref</a>" );

    if ( index1 < 0 )
    {
        System.err.println( "grandchild index.html has wrong reference menu items!" );
        result = false;
    }

}
catch ( IOException e )
{
    e.printStackTrace();
    result = false;
}

return result;
