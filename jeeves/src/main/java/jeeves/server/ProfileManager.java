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

package jeeves.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import jeeves.constants.Jeeves;
import jeeves.constants.Profiles;
import jeeves.server.context.ServiceContext;
import jeeves.server.overrides.ConfigurationOverrides;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import net.sf.saxon.value.TextFragmentValue;

import org.jdom.Element;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

//=============================================================================

/** This class is a container for the user-profiles.xml file
  */

public class ProfileManager
{
	public static final String GUEST = "Guest";
	public static final String ADMIN = "Administrator";

	private Hashtable<String, Element> htProfiles;

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	/** Given the user-profiles.xml file name, loads it abd inits its internal data
	  * putting data in memory into a convenient way
	 * @param appPath 
	  */

	@SuppressWarnings("unchecked")
	public ProfileManager(ServletContext servletContext, String appPath, String profilesFile) throws Exception
	{
		Element elProfiles = Xml.loadFile(profilesFile);
		if (servletContext != null) {
		      ConfigurationOverrides.DEFAULT.updateWithOverrides(profilesFile, servletContext, appPath, elProfiles);
		}
		htProfiles  = new Hashtable<String, Element>(50);

		List<Element> profList = elProfiles.getChildren(Profiles.Elem.PROFILE);

		for(int i=0; i<profList.size(); i++)
		{
			Element elProfile = (Element) profList.get(i);
			String  sName     = elProfile.getAttributeValue(Profiles.Attr.NAME);

			htProfiles.put(sName, elProfile);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public boolean exists(String profile)
	{
		return htProfiles.containsKey(profile);
	}

	/**
	 * Return the case corrected version of the supplied 
	 * profile name, or empty string if none match.
	 * 
	 * @param profile The profile name to be corrected.
	 * @return The correct profile name
	 */
	public String getCorrectCase(String profile)
	{
		Set<String> keys = htProfiles.keySet();
		for (String key : keys) {
			if (key.equalsIgnoreCase(profile)) {
				return key;
			}
		}
		
		return "";
	}

	//--------------------------------------------------------------------------

	public Element getProfilesElement(String profile)
	{
		Set<String> set = getProfilesSet(profile);

		//--- build proper result

		Element elResult = new Element(Jeeves.Elem.PROFILES);

		for (String p : set) {
			if (p.equals(GUEST))
				continue;

			elResult.addContent(new Element(p));
		}

		return elResult;
	}

	/**
	 * Return the highest profile in the list by checking the number
	 * of extended profiles for each.
	 * 
	 * @param profiles The list of profiles to analyze
	 * @return The highest profile in the list
	 */
	public String getLowestProfile(String[] profiles) {
		String lowestProfile = null;
		int numberOfProfilesExtended = getProfilesSet(ADMIN).size();
		
		for (String p : profiles) {
			Set<String> currentProfileSet = getProfilesSet(p);
			if (currentProfileSet.size() < numberOfProfilesExtended) {
				lowestProfile = p;
				numberOfProfilesExtended = currentProfileSet.size();
			}
		}
		return lowestProfile;
	}
	
	/**
	 * Return the highest profile in the list by checking the number
	 * of extended profiles for each.
	 * 
	 * @param profiles The list of profiles to analyze
	 * @return The highest profile in the list
	 */
	public String getHighestProfile(String[] profiles) {
		String highestProfile = null;
		int numberOfProfilesExtended = 0;
		
		for (String p : profiles) {
			Set<String> currentProfileSet = getProfilesSet(p);
			if (currentProfileSet.size() > numberOfProfilesExtended) {
				highestProfile = p;
				numberOfProfilesExtended = currentProfileSet.size();
			}
		}
		return highestProfile;
	}
	public Set<String> getProfilesSet(String profile)
	{
		HashSet<String>   hs = new HashSet<String>();
		ArrayList<String> al = new ArrayList<String>();

		al.add(profile);

		while(!al.isEmpty())
		{
			profile = (String) al.get(0);
			al.remove(0);

			hs.add(profile);

			Element elProfile = htProfiles.get(profile);
			if (elProfile == null) {
				return null;
			}

			String extend = elProfile.getAttributeValue(Profiles.Attr.EXTENDS);

			if (extend != null)
			{
				StringTokenizer st = new StringTokenizer(extend, ",");

				while(st.hasMoreTokens())
					al.add(st.nextToken().trim());
			}
		}

		return hs;
	}

	//--------------------------------------------------------------------------

	/** Returns true if the service is accessible from the given profile, resolving
	  * any inheritance
	  */

	@SuppressWarnings("unchecked")
	public boolean hasAccessTo(String profile, String service)
	{
		ArrayList<String> al = new ArrayList<String>();
		al.add(profile);

		while(!al.isEmpty())
		{
			profile = (String) al.get(0);
			al.remove(0);

			Element elProfile = (Element) htProfiles.get(profile);

			//--- scan allow list

			List<Element> allowList = elProfile.getChildren(Profiles.Elem.ALLOW);

			for (Element elAllow : allowList) {
				String  sService = elAllow.getAttributeValue(Profiles.Attr.SERVICE);

				if (service.equals(sService))
					return true;
			}

			//--- ops, no allow found. Try an ancestor (if any)

			String extend = profile = elProfile.getAttributeValue(Profiles.Attr.EXTENDS);

			if (extend != null)
			{
				StringTokenizer st = new StringTokenizer(extend, ",");

				while(st.hasMoreTokens())
					al.add(st.nextToken().trim());
			}
		}

		return false;
	}


	/** 
	 * Check if bean is defined in the context
	 * 
	 * @param beanId id of the bean to look up
	 */
	public static boolean existsBean(String beanId) {
		ServiceContext serviceContext = ServiceContext.get();
		if(serviceContext == null) return true;
		ServletContext servletContext = serviceContext.getServlet().getServletContext();
		WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		if(springContext == null) return true;
		return springContext.containsBean(beanId);
	}

	/**
	 * Optimistically check if user can access a given url.  If not possible to determine then
	 * the methods will return true.  So only use to show url links, not check if a user has access
	 * for certain.  Spring security should ensure that users cannot access restricted urls though.
	 *  
	 * @param serviceName the raw services name (main.home) or (admin) 
	 * 
	 * @return true if accessible or system is unable to determine because the current
	 * 				thread does not have a ServiceContext in its thread local store
	 */
	public static boolean isAccessibleService(Object serviceObj) {
		ServiceContext serviceContext = ServiceContext.get();
		if(serviceContext == null) return true;
		ServletContext servletContext = serviceContext.getServlet().getServletContext();
		WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		SecurityContext context = SecurityContextHolder.getContext();
		if(springContext == null || context == null) return true;
		
		Map<String, AbstractSecurityInterceptor> evals = springContext.getBeansOfType(AbstractSecurityInterceptor.class);
		Authentication authentication = context.getAuthentication();

 		String serviceName = resolveXslString(serviceObj);
        
		FilterInvocation fi = new FilterInvocation(null, "/srv/"+serviceContext.getLanguage()+"/"+serviceName, null);
		for(AbstractSecurityInterceptor securityInterceptor: evals.values()) {
	    	if(securityInterceptor == null) return true;
	    	

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

    /**
     * Transform a XSL object into a String.
     * <p/>
     * Sometimes XSL sends a node collection instead of a String; 
     * here we try to squash the complex object into a String.
     * <p/>
     * <i>At the moment only TextFragmentValue</i>s<i> are handled.</i>
     *
     * <p/>E.g.:
     * This will set a complex object:
     * <pre> {@code
     * 			<xsl:variable name="name" select="@name"/>
     * }</pre>
     * while this code will assign a string:
     * <pre> {@code
     * 			<xsl:variable name="name" select="concat('',@name)"/>
     * }</pre>
     *
     */
    protected static String resolveXslString(Object serviceObj) {
        String serviceName;
        if(serviceObj instanceof TextFragmentValue) {
            serviceName = ((TextFragmentValue)serviceObj).getStringValue();
        } else {
            serviceName = String.valueOf(serviceObj);
        }

        return serviceName;
    }

	public static boolean isCasEnabled() {
		return existsBean("casEntryPoint");
	}
}

//=============================================================================


