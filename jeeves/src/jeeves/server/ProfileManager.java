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

import java.util.*;
import org.jdom.*;

import jeeves.utils.*;
import jeeves.constants.*;

//=============================================================================

/** This class is a container for the user-profiles.xml file
  */

public class ProfileManager
{
	public static final String GUEST = "Guest";
	public static final String ADMIN = "Administrator";

	private Hashtable htProfiles;

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	/** Given the user-profiles.xml file name, loads it abd inits its internal data
	  * putting data in memory into a convenient way
	  */

	public ProfileManager(String profilesFile) throws Exception
	{
		Element elProfiles = Xml.loadFile(profilesFile);

		htProfiles  = new Hashtable(50);

		List profList = elProfiles.getChildren(Profiles.Elem.PROFILE);

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
		Set keys = htProfiles.keySet();
		for (Object key : keys)
		{
			String keyStr = (String) key;
			if (keyStr.equalsIgnoreCase(profile))
			{
				return keyStr;
			}
		}
		
		return "";
	}

	//--------------------------------------------------------------------------

	public Element getProfilesElement(String profile)
	{
		Set set = getProfilesSet(profile);

		//--- build proper result

		Element elResult = new Element(Jeeves.Elem.PROFILES);

		for(Iterator i=set.iterator(); i.hasNext();)
		{
			profile = (String) i.next();

			if (profile.equals(GUEST))
				continue;

			elResult.addContent(new Element(profile));
		}

		return elResult;
	}

	//--------------------------------------------------------------------------

	public Set getProfilesSet(String profile)
	{
		HashSet   hs = new HashSet();
		ArrayList al = new ArrayList();

		al.add(profile);

		while(!al.isEmpty())
		{
			profile = (String) al.get(0);
			al.remove(0);

			hs.add(profile);

			Element elProfile = (Element) htProfiles.get(profile);

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

	public Element getAccessibleServices(String profile)
	{
		HashSet   hs = new HashSet();
		ArrayList al = new ArrayList();

		al.add(profile);

		while(!al.isEmpty())
		{
			profile = (String) al.get(0);
			al.remove(0);

			Element elProfile = (Element) htProfiles.get(profile);

			//--- scan allow list

			List allowList = elProfile.getChildren(Profiles.Elem.ALLOW);

			for(int i=0; i<allowList.size(); i++)
			{
				Element elAllow  = (Element) allowList.get(i);
				String  sService = elAllow.getAttributeValue(Profiles.Attr.SERVICE);

				hs.add(sService);
			}

			//--- ops, no allow found. Try an ancestor (if any)

			String extend = elProfile.getAttributeValue(Profiles.Attr.EXTENDS);

			if (extend != null)
			{
				StringTokenizer st = new StringTokenizer(extend, ",");

				while(st.hasMoreTokens())
					al.add(st.nextToken().trim());
			}
		}

		//--- build proper result

		Element elRes = new Element(Jeeves.Elem.SERVICES);

		for(Iterator i=hs.iterator(); i.hasNext();)
		{
			String service = (String) i.next();
			elRes.addContent(new Element(Jeeves.Elem.SERVICE).setAttribute(new Attribute(Jeeves.Attr.NAME, service)));
		}

		return elRes;
	}

	//--------------------------------------------------------------------------
	/** Returns true if the service is accessible from the given profile, resolving
	  * any inheritance
	  */

	public boolean hasAccessTo(String profile, String service)
	{
		ArrayList al = new ArrayList();
		al.add(profile);

		while(!al.isEmpty())
		{
			profile = (String) al.get(0);
			al.remove(0);

			Element elProfile = (Element) htProfiles.get(profile);

			//--- scan allow list

			List allowList = elProfile.getChildren(Profiles.Elem.ALLOW);

			for(int i=0; i<allowList.size(); i++)
			{
				Element elAllow  = (Element) allowList.get(i);
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
}

//=============================================================================


