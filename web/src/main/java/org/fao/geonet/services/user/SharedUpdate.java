//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.user;

import com.google.common.base.Functions;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geocat;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.PasswordUtil;
import jeeves.utils.Util;
import jeeves.xlink.Processor;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.reusable.ContactsStrategy;
import org.fao.geonet.kernel.reusable.MetadataRecord;
import org.fao.geonet.kernel.reusable.Utils;
import org.fao.geonet.util.LangUtils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

//=============================================================================

/** Update the information of a user
  */

public class SharedUpdate implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String operation = Util.getParam(params, Params.OPERATION);
		String id       = params.getChildText(Params.ID);
		String username = Util.getParam(params, Params.USERNAME);
		String password = UUID.randomUUID().toString();
		String surname  = Util.getParam(params, Params.SURNAME, "");
		String name     = Util.getParam(params, Params.NAME,    "");
		String profile  = Geocat.Profile.SHARED;
		String address  = Util.getParam(params, Params.ADDRESS, "");
		String city     = Util.getParam(params, Params.CITY,    "");
		String state    = Util.getParam(params, Params.STATE,   "");
		String zip      = Util.getParam(params, Params.ZIP,     "");
		String country  = Util.getParam(params, Params.COUNTRY, "");
		String email    = Util.getParam(params, Params.EMAIL,   "");
		String organ    = LangUtils.createDescFromParams(params, Params.ORG);
		String kind     = Util.getParam(params, Params.KIND,    "");
		
        String phone    = Util.getParam(params, Geocat.Params.PHONE, "");
        String fac      = Util.getParam(params, Geocat.Params.FAC, "");
        String email1    = Util.getParam(params, Params.EMAIL+1,   "");
        String phone1    = Util.getParam(params, Geocat.Params.PHONE+1, "");
        String fac1      = Util.getParam(params, Geocat.Params.FAC+1, "");
        String email2    = Util.getParam(params, Params.EMAIL+2,   "");
        String phone2    = Util.getParam(params, Geocat.Params.PHONE+2, "");
        String fac2      = Util.getParam(params, Geocat.Params.FAC+2, "");

		String streetnb = Util.getParam(params, Geocat.Params.STREETNUMBER, "");
		String street   = Util.getParam(params, Geocat.Params.STREETNAME, "");
		String postbox  = Util.getParam(params, Geocat.Params.POSTBOX, "");
		String position = LangUtils.createDescFromParams(params, Geocat.Params.POSITIONNAME);

		String online      = LangUtils.createDescFromParams(params, Geocat.Params.ONLINE);
        String onlinename  = LangUtils.createDescFromParams(params, "onlinename");
        String onlinedesc  = LangUtils.createDescFromParams(params, "onlinedescription");

        String hours    = Util.getParam(params, Geocat.Params.HOURSOFSERV, "");
		String instruct = LangUtils.createDescFromParams(params, Geocat.Params.CONTACTINST);
		String orgacronym = LangUtils.createDescFromParams(params, Geocat.Params.ORGACRONYM);
		String directnumber = Util.getParam(params, Geocat.Params.DIRECTNUMBER, "");
		String mobile = Util.getParam(params, Geocat.Params.MOBILE, "");

        String validated = Util.getParam(params, Geocat.Params.VALIDATED, "y");


		Processor.uncacheXLinkUri(ContactsStrategy.baseHref(id));
		
		UserSession usrSess = context.getUserSession();
		String      myProfile = usrSess.getProfile();
		String      myUserId  = usrSess.getUserId();

		java.util.List listGroups = params.getChildren(Params.GROUPS);


			Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

			// Before we do anything check (for UserAdmin) that they are not trying
			// to add a user to any group outside of their own - if they are then
			// raise an exception - this shouldn't happen unless someone has
			// constructed their own malicious URL!
			//
			if (operation.equals("newuser") || operation.equals("editinfo")) {
				if (!(myUserId.equals(id)) && myProfile.equals("UserAdmin")) {
					Element bull = dbms.select("SELECT groupId from UserGroups WHERE userId="+myUserId);
					java.util.List adminlist = bull.getChildren();
					for(int i=0; i<listGroups.size(); i++) {
						String group = ((Element) listGroups.get(i)).getText();
						Boolean found = false;
						for (int j=0;j<adminlist.size();j++) {
							String testGroup = ((Element) adminlist.get(j)).getChild("groupid").getText();
							System.out.println("Testing group "+group+" against "+testGroup);
							if (group.equals(testGroup)) {
								found = true;
							}
						}
						if (!found) {
							throw new IllegalArgumentException("tried to add group id "+group+" to user "+username+" - not allowed because you are not a member of that group!");	
						}
					}
				}
			}

		// -- For Adding new user
			if (operation.equals(Params.Operation.NEWUSER)) {
				id = context.getSerialFactory().getSerial(dbms, "Users") +"";

				String query = "INSERT INTO Users (id, username, password, surname, name, profile, "+
						"address, state, zip, country, email, organisation, kind, streetnumber, "+
						"streetname, postbox, city, phone, facsimile, positionname, onlineresource, "+
						"hoursofservice, contactinstructions, publicaccess, orgacronym, directnumber, mobile, " +
						"email1, phone1, facsimile1, email2, phone2, facsimile2, onlinename, onlinedescription, validated) "+
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				dbms.execute(query, new Integer(id), username, PasswordUtil.encode(context, password), surname,
					 name, profile, address, state, zip, country, email, organ, kind,
					 streetnb, street, postbox, city, phone, fac, position, online, hours,
					 instruct, "y", orgacronym, directnumber, mobile, email1, phone1, fac1, email2, phone2, fac2, onlinename, onlinedesc, validated);


			//--- add groups

				for(int i=0; i<listGroups.size(); i++) {
					String group = ((Element) listGroups.get(i)).getText();
					addGroup(dbms, id, group);
				}
			}

			else {

			// -- full update
				if (operation.equals(Params.Operation.FULLUPDATE)) {
					String query = "UPDATE Users SET username=?, password=?, surname=?, name=?, "
							+ "address=?, state=?, zip=?, country=?, email=?,"
							+ "organisation=?, kind=?, "
							+ "profile=?, streetnumber=?, streetname=?, postbox=?, city=?, "
							+ "phone=?, facsimile=?, positionname=?, onlineresource=?, "
							+ "hoursofservice=?, contactinstructions=?, publicaccess=?, "
							+ "orgacronym=?, directnumber=?, mobile=?, email1=?, phone1=?, facsimile1=?, "
							+ "email2=?, phone2=?, facsimile2=?, onlinename=?, onlinedescription=? "
							+ "WHERE id=?";
					dbms.execute(query, username, PasswordUtil.encode(context, password),
							surname, name, address, state, zip, country, email,
							organ, kind, profile, streetnb, street, postbox,
							city, phone, fac, position, online, hours,
							instruct, "y",
							orgacronym, directnumber, mobile, email1, phone1,
							fac1, email2, phone2, fac2, onlinename, onlinedesc,
							new Integer(id));

					// --- add groups

					dbms.execute("DELETE FROM UserGroups WHERE userId=?", new Integer(id));

					for(int i=0; i<listGroups.size(); i++) {
						String group = ((Element) listGroups.get(i)).getText();
						addGroup(dbms, id, group);
					}

			// -- edit user info
				} else if (operation.equals(Params.Operation.EDITINFO)) {
					String query = "UPDATE Users SET username=?, surname=?, name=?, profile=?, address=?, city=?, state=?, zip=?, country=?, email=?, organisation=?, kind=? WHERE id=?";
					dbms.execute (query, username, surname, name, profile, address, city, state, zip, country, email, organ, kind, new Integer(id));
					//--- add groups
				
					dbms.execute ("DELETE FROM UserGroups WHERE userId=" + id);
					for(int i=0; i<listGroups.size(); i++) {
						String group = ((Element) listGroups.get(i)).getText();
						addGroup(dbms, id, group);
					}

			// -- reset password
				} else if (operation.equals(Params.Operation.RESETPW)) {
					String query = "UPDATE Users SET password=? WHERE id=?";
					dbms.execute (query, PasswordUtil.encode(context, password),new Integer(id));
				} else {
					throw new IllegalArgumentException("unknown user update operation "+operation);
				}
			}

        final ContactsStrategy strategy = new ContactsStrategy(dbms, context.getAppPath(), context.getBaseUrl(),
                context.getLanguage(), null);
        ArrayList<String> fields = new ArrayList<String>();

        fields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));
        fields.addAll(Arrays.asList(strategy.getValidXlinkLuceneField()));
        final Set<MetadataRecord> referencingMetadata = Utils.getReferencingMetadata(context, strategy, fields, id, null, false,
                Functions.<String>identity());

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getDataManager();
        for (MetadataRecord metadataRecord : referencingMetadata) {
            dm.indexMetadata(dbms, metadataRecord.id, true, context, false, false, true);
        }

        return new Element(Jeeves.Elem.RESPONSE);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	/** Adds a user to a group
	  */

	private void addGroup(Dbms dbms, String user, String group) throws Exception
	{
		dbms.execute("INSERT INTO UserGroups(userId, groupId) VALUES (?, ?)",
						 new Integer(user), new Integer(group));
	}
}

//=============================================================================

