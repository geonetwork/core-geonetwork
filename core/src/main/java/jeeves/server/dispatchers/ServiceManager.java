//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package jeeves.server.dispatchers;

import jeeves.component.ProfileManager;
import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.sources.http.JeevesServlet;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates the service contexts used by the application and keeps the startup errors.
 */
public class ServiceManager {
    private Map<String, Object> htContexts = new HashMap<String, Object>();
    private String baseUrl;
    private long maxUploadSize;
    private JeevesServlet servlet;
    private boolean startupError = false;
    private Map<String, String> startupErrors;


    @PersistenceContext
    private EntityManager entityManager;

    public void setMaxUploadSize(long size) {
        maxUploadSize = size;
    }

    public void setServlet(JeevesServlet serv) {
        servlet = serv;
    }

    //---------------------------------------------------------------------------

    public void setStartupErrors(Map<String, String> errors) {
        startupErrors = errors;
        startupError = true;
    }

    //---------------------------------------------------------------------------

    public boolean isStartupError() {
        return startupError;
    }

    public Map<String, String> getStartupErrors() {
        return startupErrors;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
    //---------------------------------------------------------------------------

    public void setBaseUrl(String name) {
        baseUrl = name;

        if (!baseUrl.startsWith("/") && baseUrl.length() != 0)
            baseUrl = "/" + baseUrl;
    }

    public void registerContext(String name, Object context) {
        htContexts.put(name, context);
    }

    public ServiceContext createServiceContext(String name, ConfigurableApplicationContext appContext) {
        ServiceContext context = new ServiceContext(name, appContext, htContexts,
            entityManager);

        context.setBaseUrl(baseUrl);
        context.setLanguage("?");
        context.setUserSession(null);
        context.setIpAddress("?");
        context.setMaxUploadSize(maxUploadSize);
        context.setServlet(servlet);

        return context;
    }

    /**
     * If we do have the optional x-forwarded-for request header then
     * use whatever is in it to record ip address of client
     */
    public static String getRequestIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("x-forwarded-for");
        return StringUtils.isNotEmpty(xForwardedForHeader)
            ? xForwardedForHeader : request.getRemoteAddr();
    }


    public ServiceContext createServiceContext(String name, String lang, HttpServletRequest request) {
        ServiceContext context = new ServiceContext(name, ApplicationContextHolder.get(), htContexts, entityManager);

        context.setBaseUrl(baseUrl);
        context.setLanguage(lang);
        context.setIpAddress(getRequestIpAddress(request));
        context.setMaxUploadSize(maxUploadSize);
        context.setServlet(servlet);

        // Session is created by ApiInterceptor when needed
        // Save the session here in the ServiceContext (not used in the API package).
        final HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            UserSession session = (UserSession) httpSession.getAttribute(Jeeves.Elem.SESSION);
            if (session != null) {

                context.setUserSession(session);
            }
        }

        return context;
    }

    public ProfileManager getProfileManager() {
        return ApplicationContextHolder.get().getBean(ProfileManager.class);
    }

}
