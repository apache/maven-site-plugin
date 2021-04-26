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
package org.apache.maven.plugins.site.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import junit.framework.Assert;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.siterenderer.DocumentContent;
import org.apache.maven.doxia.siterenderer.DocumentRenderer;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import static org.apache.maven.plugins.site.run.DoxiaFilter.I18N_DOXIA_CONTEXTS_KEY;
import static org.apache.maven.plugins.site.run.DoxiaFilter.LOCALES_LIST_KEY;
import static org.apache.maven.plugins.site.run.DoxiaFilter.SITE_RENDERER_KEY;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author D. CRUETTE / QualiteSys 2021 04 26
 * Check that the response contenttype is set according to the request ressource type
 *
 */
public class SiteRunMSITE872Test {
    
    public SiteRunMSITE872Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    final String MYLOCALE = "default";
    
    @Test
    public void testCall() 
    {

        SiteRenderingContext          leContext              = new SiteRenderingContext();
        Map<String, DocumentRenderer> leDocuments            = new HashMap<String, DocumentRenderer>();
        SiteRenderingContext          leGeneratedSiteContext = new SiteRenderingContext();
        DoxiaBean leDoxiaBean = new DoxiaBean ( leContext, leDocuments, leGeneratedSiteContext );
        
        Map<String, DoxiaBean> leI18nDoxiaContexts = new HashMap<String, DoxiaBean>();
        leI18nDoxiaContexts.put(MYLOCALE, leDoxiaBean);
        DoxiaFilter          leDoxiaFilter          = new DoxiaFilter();
        
        try {
            try {
                // Force ServletPath
                String[][] ltTests = new String[][] {
                    {"./rrr/bb/index.html" , "text/html"}
                    ,
                    {"./zz/toto.css"       , "text/css"}  
                    ,
                    {"./zz/toto.js"        , "application/javascript"}   
                    ,
                    {"./zz/toto.png"       , "image/png"}   
                    ,
                    {"./zz/toto.jpeg"      , "image/jpeg"}   
                };
                for ( String[] ltDet : ltTests) { 
                    String lsPath   = ltDet[0];
                    String lsResult = ltDet[1]; 
                    
                    InnerFilterConfig leFilterConfig = new InnerFilterConfig();
                    leDoxiaFilter.init( leFilterConfig ); 

                    InnerHttpServletRequest  leInnerHttpServletRequest  = new InnerHttpServletRequest();
                    InnerServletResponse     leServletResponse          = new InnerServletResponse();
                    InnerFilterChain         leFilterChain              = new InnerFilterChain();
                    
                    leInnerHttpServletRequest.setServletPath(lsPath);
                    leDoxiaFilter.doFilter( leInnerHttpServletRequest, leServletResponse, leFilterChain );
                    Assert.assertTrue( "ContentType is wrong, expected " + lsResult , 
                            leServletResponse.getContentType().contentEquals( lsResult )
                            ||
                            leServletResponse.getContentType().isEmpty()
                    );
                }
            } catch ( Exception ex )  
            {
                ex.printStackTrace();
            }
        } catch ( Exception ex )  
        {
            ex.printStackTrace();
        }
    }
        
    private class InnerHttpServletRequest implements HttpServletRequest 
    {
        @Override
        public String getAuthType() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest getAuthType Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Cookie[] getCookies() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest getCookies Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public long getDateHeader(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest getDateHeader Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getHeader(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest getHeader Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<String> getHeaders(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest getHeaders Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getIntHeader(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getMethod() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getPathInfo() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getPathTranslated() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getContextPath() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getQueryString() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRemoteUser() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isUserInRole(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRequestedSessionId() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRequestURI() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public StringBuffer getRequestURL() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
        private String path = "";
        public void setServletPath(String asPath) {
            path = asPath;
        }
        @Override
        public String getServletPath() { 
            return path;
        }

        @Override
        public HttpSession getSession(boolean bln) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public HttpSession getSession() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String changeSessionId() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean authenticate(HttpServletResponse hsr) throws IOException, ServletException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void login(String string, String string1) throws ServletException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void logout() throws ServletException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Part getPart(String string) throws IOException, ServletException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> type) throws IOException, ServletException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest upgrade Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object getAttribute(String string) {
            return "InnerHttpServletRequest getAttribute myAttribute";
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getContentLength() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public long getContentLengthLong() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest getContentType Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getParameter(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<String> getParameterNames() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String[] getParameterValues(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getScheme() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getServerName() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getServerPort() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public BufferedReader getReader() throws IOException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRemoteAddr() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRemoteHost() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setAttribute(String string, Object o) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void removeAttribute(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<Locale> getLocales() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRealPath(String string) {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getRemotePort() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getLocalName() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getLocalAddr() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getLocalPort() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletContext getServletContext() {
            InnerServletContext leServ = new InnerServletContext();
            return leServ;
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public AsyncContext startAsync(ServletRequest sr, ServletResponse sr1) throws IllegalStateException {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isAsyncStarted() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isAsyncSupported() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public AsyncContext getAsyncContext() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public DispatcherType getDispatcherType() {
            throw new UnsupportedOperationException( "InnerHttpServletRequest Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
    }
    private class InnerServletResponse implements ServletResponse
    {
        private String contentType = ""; 
        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setCharacterEncoding(String string) {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setContentLength(int i) {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override 
        public void setContentLengthLong(long l) {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setContentType(String string) { 
            contentType = string;
            //System.out.println( "InnerServletResponse setContentType is set to "+contentType);
        }

        @Override
        public void setBufferSize(int i) {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getBufferSize() {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void flushBuffer() throws IOException {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void resetBuffer() {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isCommitted() {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setLocale(Locale locale) {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException( "InnerServletResponse Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    private class InnerFilterChain implements FilterChain
    {

        @Override
        public void doFilter(ServletRequest sr, ServletResponse sr1) throws IOException, ServletException {
            //System.out.println( "InnerFilterChain doFilter done" );
        }

    }
    private class InnerFilterConfig implements FilterConfig
    {

        @Override
        public String getFilterName() {
            throw new UnsupportedOperationException( "InnerFilterConfig getFilterName Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletContext getServletContext() { 
            InnerServletContext leServ = new InnerServletContext();
            return leServ;
        }

        @Override
        public String getInitParameter(String string) {
            throw new UnsupportedOperationException( "InnerFilterConfig getInitParameter Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            throw new UnsupportedOperationException( "InnerFilterConfig getInitParameterNames Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

    }
    private class InnerServletContext implements ServletContext
    {

        @Override
        public String getContextPath() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletContext getContext(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getMajorVersion() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getMinorVersion() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getEffectiveMajorVersion() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getEffectiveMinorVersion() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getMimeType(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Set<String> getResourcePaths(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public URL getResource(String string) throws MalformedURLException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public InputStream getResourceAsStream(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public RequestDispatcher getNamedDispatcher(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Servlet getServlet(String string) throws ServletException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<Servlet> getServlets() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<String> getServletNames() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void log(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void log(Exception excptn, String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void log(String string, Throwable thrwbl) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRealPath(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getServerInfo() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getInitParameter(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override 
        public boolean setInitParameter(String string, String string1) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object getAttribute(String string) {
            Locale leLocale = new Locale(MYLOCALE); 
            switch (string) 
            {
                case SITE_RENDERER_KEY :
                    InnerRenderer leInnerRenderer = new InnerRenderer();
                    return leInnerRenderer;
                case I18N_DOXIA_CONTEXTS_KEY : 
                    Map<String, DoxiaBean> leMapLocale = new HashMap<String, DoxiaBean>();
                    SiteRenderingContext          leContext              = new SiteRenderingContext();
                    Map<String, DocumentRenderer> leDocuments            = new HashMap<String, DocumentRenderer>();
                    SiteRenderingContext          leGeneratedSiteContext = new SiteRenderingContext();
                    DoxiaBean leDoxiaBean = new DoxiaBean ( leContext, leDocuments, leGeneratedSiteContext );
                    leMapLocale.put(MYLOCALE,leDoxiaBean);
                    return leMapLocale;
                case LOCALES_LIST_KEY : 
                    List <Locale> leListLocale = new Vector<Locale>();
                    leListLocale.add(leLocale);
                    return leListLocale;
                default:
                    return null;
            }
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setAttribute(String string, Object o) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void removeAttribute(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getServletContextName() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String string, String string1) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String string, Servlet srvlt) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String string, Class<? extends Servlet> type) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> type) throws ServletException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletRegistration getServletRegistration(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String string, String string1) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String string, Filter filter) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String string, Class<? extends Filter> type) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> type) throws ServletException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public FilterRegistration getFilterRegistration(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public SessionCookieConfig getSessionCookieConfig() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> set) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addListener(String string) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T extends EventListener> void addListener(T t) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addListener(Class<? extends EventListener> type) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T extends EventListener> T createListener(Class<T> type) throws ServletException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ClassLoader getClassLoader() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void declareRoles(String... strings) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getVirtualServerName() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    private class InnerRenderer implements Renderer
    {

        @Override
        public void render(Collection<DocumentRenderer> clctn, SiteRenderingContext src, File file) throws RendererException, IOException {
            throw new UnsupportedOperationException( "InnerRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void generateDocument(Writer writer, SiteRendererSink srs, SiteRenderingContext src) throws RendererException {
            throw new UnsupportedOperationException( "InnerRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mergeDocumentIntoSite(Writer writer, DocumentContent dc, SiteRenderingContext src) throws RendererException {
            throw new UnsupportedOperationException( "InnerRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public SiteRenderingContext createContextForSkin(Artifact artfct, Map<String, ?> map, DecorationModel dm, String string, Locale locale) throws RendererException, IOException {
            throw new UnsupportedOperationException( "InnerRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public SiteRenderingContext createContextForTemplate(File file, Map<String, ?> map, DecorationModel dm, String string, Locale locale) throws MalformedURLException {
            throw new UnsupportedOperationException( "InnerRenderer createContextForTemplate Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void copyResources(SiteRenderingContext src, File file, File file1) throws IOException {
            throw new UnsupportedOperationException( "InnerRenderer copyResources Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void copyResources(SiteRenderingContext src, File file) throws IOException {
            throw new UnsupportedOperationException( "InnerRenderer copyResources Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        } 

        @Override
        public Map<String, DocumentRenderer> locateDocumentFiles(SiteRenderingContext src) throws IOException, RendererException {
            InnerDocumentRenderer leDoc = new InnerDocumentRenderer();
            Map<String, DocumentRenderer> leMap = new HashMap<String, DocumentRenderer>();
            leMap.put( "mydoc", leDoc);
            return leMap; 
        }

        @Override
        public Map<String, DocumentRenderer> locateDocumentFiles(SiteRenderingContext src, boolean bln) throws IOException, RendererException {
            throw new UnsupportedOperationException( "InnerRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void renderDocument(Writer writer, RenderingContext rc, SiteRenderingContext src) throws RendererException, FileNotFoundException, UnsupportedEncodingException {
            throw new UnsupportedOperationException( "InnerRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    private class InnerDocumentRenderer implements DocumentRenderer
    {

        @Override
        public void renderDocument(Writer writer, Renderer rndr, SiteRenderingContext src) throws RendererException, FileNotFoundException, UnsupportedEncodingException {
            throw new UnsupportedOperationException( "InnerDocumentRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getOutputName() {
            throw new UnsupportedOperationException( "InnerDocumentRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public RenderingContext getRenderingContext() {
            throw new UnsupportedOperationException( "InnerDocumentRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isOverwrite() {
            throw new UnsupportedOperationException( "InnerDocumentRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isExternalReport() {
            throw new UnsupportedOperationException( "InnerDocumentRenderer Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
