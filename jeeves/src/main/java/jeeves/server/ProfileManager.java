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

import javax.servlet.Filter;
import javax.servlet.ServletContext;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.constants.Jeeves;
import jeeves.constants.Profiles;
import jeeves.utils.Xml;

import org.jdom.Element;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

//=============================================================================

/** This class is a container for the user-profiles.xml file
  */

public class ProfileManager
{
	public static final String GUEST = "Guest";
	public static final String ADMIN = "Administrator";

	private Hashtable<String, Element> htProfiles;
	private JeevesApplicationContext applicationContext;

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
		      ConfigurationOverrides.updateWithOverrides(profilesFile, servletContext, appPath, elProfiles);
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
	/** Returns all services accessible by the given profile
	  */

	public Element getAccessibleServices()
	{
		Collection<FilterSecurityInterceptor> chains = applicationContext.getBeansOfType(FilterSecurityInterceptor.class).values();
		
		for (FilterSecurityInterceptor chain : chains) {
			System.out.println(chain);
			Collection<ConfigAttribute> allConfigAttributes = chain.getSecurityMetadataSource().getAllConfigAttributes();
			for (ConfigAttribute configAttribute : allConfigAttributes) {
				System.out.println(configAttribute);
			}
//			List<Filter> filters = chain.getFilters();
//			for (Filter filter : filters) {
//				System.out.println(filter);
//			}
		}
		//--- build proper result

		Element elRes = new Element(Jeeves.Elem.SERVICES);
//
//		for (String service : hs) {
//			elRes.addContent(new Element(Jeeves.Elem.SERVICE)
//					.setAttribute(new Attribute(Jeeves.Attr.NAME, service)));
//		}

		return elRes;
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

	public void setApplicationContext(JeevesApplicationContext jeevesAppContext) {
		this.applicationContext = jeevesAppContext;
	}
}

//=============================================================================


