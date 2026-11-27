
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

// The markdown configuration only matches files in the "commented" directory,
// the xdoc configuration matches all xdoc files.
def render = { String name ->
  def file = new File( basedir, "target/site/$name" )
  assert file.exists() : "$name must have been generated"
  def content = file.text
  assert content.contains( 'Content for verify.groovy' ) : "$name must contain the content of its source"
  return content
}

def commented = render( 'commented/commented.html' )
assert commented.contains( 'MARKER-COMMENT-COMMENTED' ) :
  'emitComments is configured for markdown files matching glob:**/markdown/commented/*.md'

def plain = render( 'plain.html' )
assert !plain.contains( 'MARKER-COMMENT-PLAIN' ) :
  'the markdown configuration must not apply to files outside of its patterns'

def xdoc = render( 'page.html' )
assert xdoc.contains( 'MARKER-COMMENT-XDOC' ) :
  'emitComments is configured for all xdoc files (no patterns)'
