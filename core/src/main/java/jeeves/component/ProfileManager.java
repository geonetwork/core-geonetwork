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
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.component;

import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;

import javax.servlet.ServletContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

//=============================================================================

/**
 * This class is a container for the user-profiles.xml file
 */
public class ProfileManager {
    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    /**
     * Return the highest profile in the list by checking the number of extended profiles for each.
     *
     * @param profiles The list of profiles to analyze
     * @return The highest profile in the list
     */
    public static Profile getLowestProfile(String[] profiles) {
        Profile lowestProfile = null;
        int numberOfProfilesExtended = Profile.Administrator.getAll().size();

        for (String profileName : profiles) {
            Profile p = Profile.valueOf(profileName);
            Set<Profile> currentProfileSet = p.getAll();
            if (currentProfileSet.size() < numberOfProfilesExtended) {
                lowestProfile = p;
                numberOfProfilesExtended = currentProfileSet.size();
            }
        }
        return lowestProfile;
    }

    /**
     * Return the highest profile in the list by checking the number of extended profiles for each.
     *
     * @param profiles The list of profiles to analyze
     * @return The highest profile in the list
     */
    public static Profile getHighestProfile(Profile[] profiles) {
        Profile highestProfile = null;
        int numberOfProfilesExtended = 0;

        for (Profile profile : profiles) {
            Set<Profile> all = profile.getAll();
            if (all.size() > numberOfProfilesExtended) {
                highestProfile = profile;
                numberOfProfilesExtended = all.size();
            }
        }
        return highestProfile;
    }

    /**
     * Check if bean is defined in the context
     *
     * @param beanId id of the bean to look up
     */
    public static boolean existsBean(String beanId) {
        return ApplicationContextHolder.get().containsBean(beanId);
    }
    //--------------------------------------------------------------------------

    /**
     * Optimistically check if user can access a given url.  If not possible to determine then the
     * methods will return true.  So only use to show url links, not check if a user has access for
     * certain.  Spring security should ensure that users cannot access restricted urls though.
     *
     * @param serviceName the raw services name (main.home) or (admin)
     * @return true if accessible or system is unable to determine because the current thread does
     * not have a ServiceContext in its thread local store
     */
    public static boolean isAccessibleService(Object serviceName) {
        ServiceContext serviceContext = ServiceContext.get();
        if (serviceContext == null) return true;
        ServletContext servletContext = serviceContext.getServlet().getServletContext();
        ConfigurableApplicationContext springContext = JeevesDelegatingFilterProxy.getApplicationContextFromServletContext(servletContext);
        SecurityContext context = SecurityContextHolder.getContext();
        if (springContext == null || context == null) return true;

        Map<String, AbstractSecurityInterceptor> evals = springContext.getBeansOfType(AbstractSecurityInterceptor.class);
        Authentication authentication = context.getAuthentication();

        FilterInvocation fi = new FilterInvocation(null, "/srv/" + serviceContext.getLanguage() + "/" + serviceName, null);
        for (AbstractSecurityInterceptor securityInterceptor : evals.values()) {
            if (securityInterceptor == null) return true;


            Collection<ConfigAttribute> attrs = securityInterceptor.obtainSecurityMetadataSource().getAttributes(fi);

            if (attrs == null) {
                continue;
            }

            if (authentication == null) {
                continue;
            }

            try {
                securityInterceptor.getAccessDecisionManager().decide(authentication, fi, attrs);
                return true;
            } catch (AccessDeniedException unauthorized) {
                // ignore
            }
        }
        if (Log.isDebugEnabled(Log.REQUEST)) {
            Log.debug(Log.REQUEST, fi.toString() + " denied for " + authentication.toString());
        }

        return false;
    }

    //--------------------------------------------------------------------------

    public static boolean isCasEnabled() {
        return existsBean("casEntryPoint");
    }

    public boolean exists(String profile) {
        return Profile.exists(profile);
    }

    /**
     * Return the case corrected version of the supplied profile name, or empty string if none
     * match.
     *
     * @param profileName The profile name to be corrected.
     * @return The correct profile name
     */
    public Profile getCorrectCase(String profileName) {
        return Profile.findProfileIgnoreCase(profileName);
    }

    public Profile getProfile(String profileName) {
        return Profile.valueOf(profileName);
    }

    public Element getProfilesElement(Profile profile) {
        return profile.asElement();
    }
}

//=============================================================================


