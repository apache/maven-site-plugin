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
import org.junit.Test;

/**
 * Bug fix for MSITE-872
 * Check that the response contenttype is set according to the request ressource type
 *
 */
public class SiteRunMSITE872Test {
    
    final String MYLOCALE = "default";
    
    @Test
    public void testCall () throws Exception 
    {

        SiteRenderingContext          leContext              = new SiteRenderingContext();
        Map<String, DocumentRenderer> leDocuments            = new HashMap<String, DocumentRenderer>();
        SiteRenderingContext          leGeneratedSiteContext = new SiteRenderingContext();
        DoxiaBean leDoxiaBean = new DoxiaBean ( leContext, leDocuments, leGeneratedSiteContext );
        
        Map<String, DoxiaBean> leI18nDoxiaContexts = new HashMap<String, DoxiaBean>();
        leI18nDoxiaContexts.put(MYLOCALE, leDoxiaBean);
        DoxiaFilter          leDoxiaFilter          = new DoxiaFilter();
        
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
            String lsFct = leServletResponse.getContentType();
            Assert.assertTrue( "ContentType is wrong, expected " + lsResult + " but result is " + lsFct, 
                    lsFct.contentEquals( lsResult )
                    ||
                    lsFct.isEmpty()
            );
        }
    }
        
    private class InnerHttpServletRequest implements HttpServletRequest 
    {
        @Override
        public String getAuthType() 
        {
            return "";
        }

        @Override
        public Cookie[] getCookies() 
        {
            return ( new Cookie[0] );
        }

        @Override
        public long getDateHeader(String string) 
        {
            return 0;
        }

        @Override
        public String getHeader(String string) 
        {
            return "";
        }

        @Override
        public Enumeration<String> getHeaders(String string) 
        {
            return null;
        }

        @Override
        public Enumeration<String> getHeaderNames() 
        {
            return null;
        }

        @Override
        public int getIntHeader(String string) {
            return 0;
        }

        @Override
        public String getMethod() {
            return "";
        }

        @Override
        public String getPathInfo() {
            return "";
        }

        @Override
        public String getPathTranslated() {
            return "";
        }

        @Override
        public String getContextPath() {
            return "";
        }

        @Override
        public String getQueryString() {
            return "";
        }

        @Override
        public String getRemoteUser() {
            return "";
        }

        @Override
        public boolean isUserInRole(String string) {
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getRequestedSessionId() {
            return "";
        }

        @Override
        public String getRequestURI() {
            return "";
        }

        @Override
        public StringBuffer getRequestURL() {
            return null;
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
            return null;
        }

        @Override
        public HttpSession getSession() {
            return null;
        }

        @Override
        public String changeSessionId() {
            return "";
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            return false;
        }

        @Override
        public boolean authenticate(HttpServletResponse hsr) throws IOException, ServletException {
            return false;
        }

        @Override
        public void login(String string, String string1) throws ServletException {
        }

        @Override
        public void logout() throws ServletException {
        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            return null;
        }

        @Override
        public Part getPart(String string) throws IOException, ServletException {
            return null;
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> type) throws IOException, ServletException {
            return null;
        }

        @Override
        public Object getAttribute(String string) {
            return "InnerHttpServletRequest getAttribute myAttribute";
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return null;
        }

        @Override
        public String getCharacterEncoding() {
            return "";
        }

        @Override
        public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
        }

        @Override
        public int getContentLength() {
            return 0;
        }

        @Override
        public long getContentLengthLong() {
            return 0;
        }

        @Override
        public String getContentType() {
            return "";
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return null;
        }

        @Override
        public String getParameter(String string) {
            return "";
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return null;
        }

        @Override
        public String[] getParameterValues(String string) {
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return null;
        }

        @Override
        public String getProtocol() {
            return "";
        }

        @Override
        public String getScheme() {
            return "";
        }

        @Override
        public String getServerName() {
            return "";
        }

        @Override
        public int getServerPort() {
            return 0;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return "";
        }

        @Override
        public String getRemoteHost() {
            return "";
        }

        @Override
        public void setAttribute(String string, Object o) {
        }

        @Override
        public void removeAttribute(String string) {
        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public Enumeration<Locale> getLocales() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String string) {
            return null;
        }

        @Override
        public String getRealPath(String string) {
            return "";
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public String getLocalName() {
            return "";
        }

        @Override
        public String getLocalAddr() {
            return "";
        }

        @Override
        public int getLocalPort() {
            return 0;
        }

        @Override
        public ServletContext getServletContext() {
            InnerServletContext leServ = new InnerServletContext();
            return leServ;
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            return null;
        }

        @Override
        public AsyncContext startAsync(ServletRequest sr, ServletResponse sr1) throws IllegalStateException {
            return null;
        }

        @Override
        public boolean isAsyncStarted() {
            return false;
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }

        @Override
        public AsyncContext getAsyncContext() {
            return null;
        }

        @Override
        public DispatcherType getDispatcherType() {
            return null;
        }
    }
    private class InnerServletResponse implements ServletResponse
    {
        private String contentType = ""; 
        @Override
        public String getCharacterEncoding() {
            return "";
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return null;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return null;
        }

        @Override
        public void setCharacterEncoding(String string) {
        }

        @Override
        public void setContentLength(int i) {
        }

        @Override 
        public void setContentLengthLong(long l) {
        }

        @Override
        public void setContentType(String string) { 
            contentType = string;
        }

        @Override
        public void setBufferSize(int i) {
        }

        @Override
        public int getBufferSize() {
            return 0;
        }

        @Override
        public void flushBuffer() throws IOException {
        }

        @Override
        public void resetBuffer() {
        }

        @Override
        public boolean isCommitted() {
            return false;
        }

        @Override
        public void reset() {
        }

        @Override
        public void setLocale(Locale locale) {
        }

        @Override
        public Locale getLocale() {
            return null;
        }
        
    }
    private class InnerFilterChain implements FilterChain
    {

        @Override
        public void doFilter(ServletRequest sr, ServletResponse sr1) throws IOException, ServletException {
        }

    }
    private class InnerFilterConfig implements FilterConfig
    {

        @Override
        public String getFilterName() {
            return "";
        }

        @Override
        public ServletContext getServletContext() { 
            InnerServletContext leServ = new InnerServletContext();
            return leServ;
        }

        @Override
        public String getInitParameter(String string) {
            return "";
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return null;
        }

    }
    private class InnerServletContext implements ServletContext
    {

        @Override
        public String getContextPath() {
            return "";
        }

        @Override
        public ServletContext getContext(String string) {
            return null;
        }

        @Override
        public int getMajorVersion() {
            return 0;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public int getEffectiveMajorVersion() {
            return 0;
        }

        @Override
        public int getEffectiveMinorVersion() {
            return 0;
        }

        @Override
        public String getMimeType(String string) {
            return "";
        }

        @Override
        public Set<String> getResourcePaths(String string) {
            return null;
        }

        @Override
        public URL getResource(String string) throws MalformedURLException {
            return null;
        }

        @Override
        public InputStream getResourceAsStream(String string) {
            return null;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String string) {
            return null;
        }

        @Override
        public RequestDispatcher getNamedDispatcher(String string) {
            return null;
        }

        @Override
        public Servlet getServlet(String string) throws ServletException {
            return null;
        }

        @Override
        public Enumeration<Servlet> getServlets() {
            return null;
        }

        @Override
        public Enumeration<String> getServletNames() {
            return null;
        }

        @Override
        public void log(String string) {
        }

        @Override
        public void log(Exception excptn, String string) {
        }

        @Override
        public void log(String string, Throwable thrwbl) {
        }

        @Override
        public String getRealPath(String string) {
            return "";
        }

        @Override
        public String getServerInfo() {
            return "";
        }

        @Override
        public String getInitParameter(String string) {
            return "";
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return null;
        }

        @Override 
        public boolean setInitParameter(String string, String string1) {
            return false;
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
            return null;
        }

        @Override
        public void setAttribute(String string, Object o) {
        }

        @Override
        public void removeAttribute(String string) {
        }

        @Override
        public String getServletContextName() {
            return "";
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String string, String string1) {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String string, Servlet srvlt) {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String string, Class<? extends Servlet> type) {
            return null;
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> type) throws ServletException {
            return null;
        }

        @Override
        public ServletRegistration getServletRegistration(String string) {
            return null;
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String string, String string1) {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String string, Filter filter) {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String string, Class<? extends Filter> type) {
            return null;
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> type) throws ServletException {
            return null;
        }

        @Override
        public FilterRegistration getFilterRegistration(String string) {
            return null;
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            return null;
        }

        @Override
        public SessionCookieConfig getSessionCookieConfig() {
            return null;
        }

        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> set) {
        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return null;
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return null;
        }

        @Override
        public void addListener(String string) {
        }

        @Override
        public <T extends EventListener> void addListener(T t) {
            return;
        }

        @Override
        public void addListener(Class<? extends EventListener> type) {
        }

        @Override
        public <T extends EventListener> T createListener(Class<T> type) throws ServletException {
            return null;
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public void declareRoles(String... strings) {
        }

        @Override
        public String getVirtualServerName() {
            return "";
        }
        
    }
    private class InnerRenderer implements Renderer
    {

        @Override
        public void render(Collection<DocumentRenderer> clctn, SiteRenderingContext src, File file) throws RendererException, IOException {
        }

        @Override
        public void generateDocument(Writer writer, SiteRendererSink srs, SiteRenderingContext src) throws RendererException {
        }

        @Override
        public void mergeDocumentIntoSite(Writer writer, DocumentContent dc, SiteRenderingContext src) throws RendererException {
        }

        @Override
        public SiteRenderingContext createContextForSkin(Artifact artfct, Map<String, ?> map, DecorationModel dm, String string, Locale locale) throws RendererException, IOException {
            return null;
        }

        @Override
        public SiteRenderingContext createContextForTemplate(File file, Map<String, ?> map, DecorationModel dm, String string, Locale locale) throws MalformedURLException {
            return null;
        }

        @Override
        public void copyResources(SiteRenderingContext src, File file, File file1) throws IOException {
        }

        @Override
        public void copyResources(SiteRenderingContext src, File file) throws IOException {
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
            return null;
        }

        @Override
        public void renderDocument(Writer writer, RenderingContext rc, SiteRenderingContext src) throws RendererException, FileNotFoundException, UnsupportedEncodingException {
        }
        
    }
    private class InnerDocumentRenderer implements DocumentRenderer
    {

        @Override
        public void renderDocument(Writer writer, Renderer rndr, SiteRenderingContext src) throws RendererException, FileNotFoundException, UnsupportedEncodingException {
        }

        @Override
        public String getOutputName() {
            return "";
        }

        @Override
        public RenderingContext getRenderingContext() {
            return null;
        }

        @Override
        public boolean isOverwrite() {
            return false;
        }

        @Override
        public boolean isExternalReport() {
            return false;
        }

    }
}
