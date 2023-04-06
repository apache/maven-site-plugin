
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

// These files must have been generated
[
  'apt-macro.html',
  'index.html',
  'velocity-context.html',
  'velocity-include-parse.html',
  'faq-macro.html',
  'faq.html',
  'markdown-macro.html',
  'markdown.html',
  'markdown2.html',
  'xdoc.html',
  'xdoc-macro.html',
  'xhtml.html',
  'xhtml-macro.html'
].each {

  // File exists
  def verifiedFile = new File( basedir, "target/site/$it" )
  assert verifiedFile.exists() : "$it must have been generated"

  // File contains the content marker 'Content for verify.groovy'
  def verifiedContent = verifiedFile.text
  assert verifiedContent.contains( 'Content for verify.groovy' ) :
    "$it must have content from the source ('Content for verify.groovy')"

  // File does NOT contain the path to the source file referenced by the SNIPPET macro.
  // This means that the SNIPPET macro has been properly processed.
  // This test can be run in all files produced, even those with no macros.
  assert !verifiedContent.contains( 'src/main/java/org/apache/maven/plugins/site/it/CustomVelocityTool.java' ) :
    "Macros must have been processed in $it and SNIPPET source file path removed"

}

// ignore.txt must NOT have been generated
assert !( new File( basedir, 'target/site/ignore.txt' ).exists() ) :
  "ignore.txt does not end up in the generated content"
assert !( new File( basedir, 'target/site/ignore.html' ).exists() ) :
  "ignore.txt does not produce ignore.html"

// .vm files must have been processed with Velocity
def velocity = new File( basedir, 'target/generated-site/processed/velocity-context.apt' )
assert velocity.exists() : "*.vm files must be processed with Velocity and stored in target/generated-site"

def content = velocity.text
assert content.contains( '= <<<val1>>>' ) : "Velocity-processed content must conform to MSITE-550"

assert !( content.replace('<<<$value', '').contains( '<<<$' ) ) :
  'Velocity-processed content must not contain any veloci-macro reference'

assert content.contains( "Content for verify.groovy" ) : "Velocity-processed content must contain English text for default locale"

// .vm files must have been processed with Velocity
velocity = new File( basedir, 'target/generated-site/processed/de/velocity-context.apt' )
assert velocity.exists() : "*.vm files must be processed with Velocity and stored in target/generated-site"

content = velocity.text
assert content.contains( "Inhalt für verify.groovy" ) : "Velocity-processed content must contain German text for locale 'de'"
