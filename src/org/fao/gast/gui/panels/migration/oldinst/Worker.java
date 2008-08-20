//==============================================================================
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

package org.fao.gast.gui.panels.migration.oldinst;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.dlib.gui.ProgressDialog;
import org.fao.gast.app.App;
import org.fao.gast.lib.DatabaseLib;
import org.fao.gast.lib.Lib;
import org.fao.gast.lib.Resource;
import org.fao.gast.lib.druid.Codec;
import org.fao.gast.lib.druid.DdfLoader;
import org.fao.gast.lib.druid.ImportField;
import org.fao.geonet.kernel.XmlSerializer;
import org.jdom.Element;

//==============================================================================

public class Worker implements Runnable
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Worker(ProgressDialog d)
	{
		dlg = d;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void setOldDir(String dir)
	{
		appPath = dir;
	}

	//---------------------------------------------------------------------------

	public void setOldUser(String user)
	{
		oldUserName = user;
	}

	//---------------------------------------------------------------------------

	public void setOldGroup(String group)
	{
		oldGroupName = group;
	}

	//---------------------------------------------------------------------------

	public void setUserDialog(boolean yesno)
	{
		showDialog = yesno;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Migration process
	//---
	//---------------------------------------------------------------------------

	public void run()
	{
		if (!openSource())
		{
			dlg.stop();
			return;
		}

		Resource oldRes = null;
		Resource newRes = null;

		try
		{
			oldRes = source.config.createResource();
			newRes = Lib.config.createResource();

			executeJob((Dbms) oldRes.open(), (Dbms) newRes.open());
		}
		catch(Throwable t)
		{
			Lib.gui.showError(dlg, t);
		}
		finally
		{
			if (oldRes != null)
				oldRes.close();

			if (newRes != null)
				newRes.close();

			dlg.stop();
		}
	}

	//---------------------------------------------------------------------------

	private boolean openSource()
	{
		try
		{
			source = new GNSource(appPath);

			return true;
		}
		catch (Exception e)
		{
			Lib.gui.showError(dlg, 	"It seems that the specified folder does not \n"+
											"contain an old GeoNetwork installation");

			return false;
		}
	}

	//---------------------------------------------------------------------------

	private void checkOldUser(Dbms oldDbms) throws Exception
	{
		String query = "SELECT id, profile FROM users WHERE username = ?";
		List oldUserIds = oldDbms.select(query, oldUserName).getChildren();
		oldDbms.commit();

		// Get the user id and profile from the db
		if (oldUserIds.size() == 0)
			throw new Exception("Can't find user \"" + oldUserName + "\" in the old GeoNetwork");

		// Check if the user is an Editor
		String profile = ((Element)oldUserIds.get(0)).getChildText("profile");
		if (!"Editor".equals(profile))
			throw new Exception("User \"" + oldUserName + "\" is not an Editor");

		// Check if the group exists
		oldUserId = Integer.parseInt(((Element)oldUserIds.get(0)).getChildText("id"));

		query = "SELECT id FROM groups WHERE name = ?";
		List oldGroupIds = oldDbms.select(query, oldGroupName).getChildren();
		oldDbms.commit();
		if (oldGroupIds.size() == 0)
			throw new Exception("Can't find group \"" + oldGroupName + "\" in the old GeoNetwork");

		// Check if the user belongs to the given group
		oldGroupId = Integer.parseInt(((Element)oldGroupIds.get(0)).getChildText("id"));

		query = "SELECT groupId FROM userGroups WHERE groupId = ? AND userId = ?";
		List userGroups = oldDbms.select(query, new Integer(oldGroupId), new Integer(oldUserId)).getChildren();
		oldDbms.commit();
		if (userGroups.size() == 0)
			throw new Exception("User \"" + oldUserName + "\" doesn't belong to group \"" + oldGroupName + "\"");
	}

	//---------------------------------------------------------------------------

	private void executeJob(Dbms oldDbms, Dbms newDbms) throws Exception
	{
		if (oldUserName == null)
			dlg.reset(9);
		else
		{
			dlg.reset(10);
			dlg.advance("Checking old user and group");
			checkOldUser(oldDbms);
		}

		dlg.advance("Removing data in new installation");
		removeAll(newDbms);

		Set<String> langs = getLanguages(newDbms);

		dlg.advance("Migrating users");
		migrateUsers(oldDbms, newDbms);

		dlg.advance("Migrating groups");
		migrateGroups(oldDbms, newDbms, langs);

		dlg.advance("Migrating categories");
		migrateCategories(oldDbms, newDbms, langs);

		dlg.advance("Migrating metadata");
		migrateMetadata(oldDbms, newDbms);

		dlg.advance("Migrating privileges");
		migrateOperationAllowed(oldDbms, newDbms);

		dlg.advance("Migrating metadata categories");
		migrateMetadataCateg(oldDbms, newDbms);

		dlg.advance("Migrating settings");
		migrateSettings(newDbms);

		dlg.advance("Restoring localized labels");
		restoreLocalizedRecords(newDbms);

		Lib.metadata.clearIndexes();

		String message = "Migrated metadata : "+ mdIds.size() + "\n"+
								nNoEditor + " metadata had no editor and were assigned to " + oldUserName + ":" + oldGroupName + "\n"+
								nNoPriv + " metadata had no admin privileges and were assigned to " + oldUserName + ":" + oldGroupName + "\n"+
								nMatchedUsers + " users were successfully assigned from their surname\n"+
								nMultiUser + " metadata were assigned to first candidate\n";

		Lib.gui.showInfo(dlg, message);
		Lib.log.debug(message);
	}

	//---------------------------------------------------------------------------

	private void removeAll(Dbms newDbms) throws SQLException
	{
		//--- we commit at each step because an old migration could have failed and
		//--- we could have plenty of records

		newDbms.execute("DELETE FROM OperationAllowed");
		newDbms.commit();
		newDbms.execute("DELETE FROM MetadataCateg");
		newDbms.commit();
		newDbms.execute("DELETE FROM UserGroups");
		newDbms.commit();
		newDbms.execute("DELETE FROM Metadata");
		newDbms.commit();
		newDbms.execute("DELETE FROM GroupsDes");
		newDbms.execute("DELETE FROM Groups");
		newDbms.commit();
		newDbms.execute("DELETE FROM Users");
		newDbms.commit();
		newDbms.execute("DELETE FROM CategoriesDes");
		newDbms.execute("DELETE FROM Categories");
		newDbms.commit();
	}

	//---------------------------------------------------------------------------

	private Set<String> getLanguages(Dbms newDbms) throws SQLException
	{
		List langs = newDbms.select("SELECT * FROM Languages").getChildren();

		Set<String> set = new HashSet<String>();

		for (Object l : langs)
		{
			Element lang = (Element) l;
			set.add(lang.getChildText("id"));
		}

		newDbms.commit();

		return set;
	}

	//---------------------------------------------------------------------------

	private void migrateUsers(Dbms oldDbms, Dbms newDbms) throws SQLException
	{
		String query = "SELECT * FROM Users";

		List oldUsers = oldDbms.select(query).getChildren();

		//--- copy users

		for (Object user : oldUsers)
			Lib.database.insert(newDbms, "Users", (Element) user, userMapper);

		oldDbms.commit();
		newDbms.commit();
	}

	//---------------------------------------------------------------------------
	/** Migrates Groups, UserGroups */

	private void migrateGroups(Dbms oldDbms, Dbms newDbms, Set<String> langs) throws SQLException
	{
		String query1 = "SELECT * FROM Groups";
		String query2 = "SELECT * FROM UserGroups";

		List oldGroups = oldDbms.select(query1).getChildren();

		//--- copy groups

		for (Object g : oldGroups)
		{
			Element group = (Element) g;
			String  id    = group.getChildText("id");
			String  label = group.getChildText("name");

			Lib.database.insert(newDbms, "Groups", group, groupsMapper);
			Lib.database.insert(newDbms, "GroupsDes", langs, id, label);
		}

		//--- copy user groups

		List oldUsrGrp = oldDbms.select(query2).getChildren();

		for (Object usrGrp : oldUsrGrp)
			Lib.database.insert(newDbms, "UserGroups", (Element) usrGrp, relationMapper);

		oldDbms.commit();
		newDbms.commit();
	}

	//---------------------------------------------------------------------------
	/** Migrates Categories */

	private void migrateCategories(Dbms oldDbms, Dbms newDbms, Set<String> langs) throws SQLException
	{
		String query = "SELECT * FROM Categories";

		List categs = oldDbms.select(query).getChildren();

		//--- copy categories

		for (Object c : categs)
		{
			Element categ = (Element) c;
			String  id    = categ.getChildText("id");
			String  label = categ.getChildText("name");

			Lib.database.insert(newDbms, "Categories", categ, idMapper);
			Lib.database.insert(newDbms, "CategoriesDes", langs, id, label);
		}

		oldDbms.commit();
		newDbms.commit();
	}

	//---------------------------------------------------------------------------

	private void migrateMetadata(Dbms oldDbms, Dbms newDbms) throws Exception
	{
		nNoPriv       = 0;
		nNoEditor     = 0;
		nMatchedUsers = 0;
		nMultiUser    = 0;

		String query = "SELECT id, uuid, schemaId, isTemplate, createDate, "+
							"       lastChangeDate as changeDate, source "+
							"FROM   Metadata ORDER BY id";

		List metadata = oldDbms.select(query).getChildren();

		String oldSiteId = source.getSiteId();
		String newSiteId = Lib.site.getSiteId(newDbms);

		oldDbms.commit();
		newDbms.commit();

		mdIds.clear();

		//--- copy metadata

		for (Object m : metadata)
		{
			Element md     = (Element) m;
			String  id     = md.getChildText("id");
			String  source = md.getChildText("source");

			//--- has the old metadata been harvested ?

			if (!oldSiteId.equals(source))
				continue;

			//--- retrieve metadata

			md.getChild("source").setText(newSiteId);

			Element xml = XmlSerializer.select(oldDbms, "Metadata", id);
			oldDbms.commit();

			//--- Identify the best owner

			List<Owner> owners = getPossibleOwners(oldDbms, id, xml);

			Owner owner = null;

			//--- only 1 possible owner -> assign it

			if (owners.size() == 1)
				owner = owners.get(0);

			//--- more possible owners -> try to match the best one

			else if (owners.size() > 1)
			{
				if (showDialog)
					owner = promptUser(oldDbms, id, owners);
				else
				{
					Lib.log.debug("Assigning metadata id " + id + " to first candidate found: "+ owners.get(0) 
				        + " surname: " + owners.get(0).surname);
					owner = owners.get(0);
					nMultiUser++;
				}
			}

            if (owner.group==null) {
                Lib.log.info("No group found. Using "+ oldGroupName +" (" + oldGroupId + ")");
                owner.group = "" + oldGroupId;
                owner.groupName = oldGroupName;
            }

			//--- no, ok we can store it

			md.addContent(new Element("root")       .setText(xml.getName()));
			md.addContent(new Element("isHarvested").setText("n"));
			md.addContent(new Element("data")       .setText(Xml.getString(xml)));
			md.addContent(new Element("owner")      .setText(owner.user));
			md.addContent(new Element("groupOwner") .setText(owner.group));

			Lib.database.insert(newDbms, "Metadata", md, idMapper);

            //--- we can have even millions of records so it is convenient to commit
			//--- at each insert

			newDbms.commit();

			Lib.log.info("Metadata (id: " + id + ") successfully assigned to "+ owner.toString());

			mdIds.add(id);
		}
	}

	//---------------------------------------------------------------------------

	private List<Owner> getPossibleOwners(Dbms dbms, String id, Element md) throws Exception
	{
		List<Owner> owners = new ArrayList<Owner>(); // The candidate owners that should be returned
        List<Owner> ownersPriv = new ArrayList<Owner>(); // Editors and above in groups that have edit or admin privileges on the record 
		String group = null;
		
		/*
		 * Select the best new owner of the metadata record
		 * 
		 * Select the metadata author based on the metadata
		 * Select the group(s) that has/have edit or admin privileges to it
         * Select all users from the above selected group(s) that are not RegisteredUsers
         * 
		 * Is the identified author part of the group that has edit or admin privileges to the record?
		 *   Yes: assign that author as the new owner
		 *   No:
		 *     - allow the user to select the new owner, or
		 *     - automatically select the first Editor, otherwise the first UserAdmin and finally the first Administrator
		 * If there are no groups with edit or admin privileges, assign the record to the migration user/group
		 * 
		 * */
		
        Lib.log.debug("Processing metadata with id: "+ id);

        Owner author = getUserFromMetadata(dbms, md); //--- try to match the author of the metadata
        
		// Select the groups that have admin rights on this metadata
		String query = "SELECT groupId FROM OperationAllowed WHERE metadataId=? AND operationId=4 AND groupId<>2";
		List privil = dbms.select(query, new Integer(id)).getChildren();
		dbms.commit();
		
		if (privil.size()==0){
            query = "SELECT groupId FROM OperationAllowed WHERE metadataId=? AND operationId=2 AND groupId<>2";
            privil = dbms.select(query, new Integer(id)).getChildren();
            dbms.commit();
		}
		Lib.log.debug(privil.size() + " group(s) found with 'edit' or 'admin' privilege");
				
		if (privil.size() > 0) {
	        //--- create a list with users that have enough privileges
	        for (Object grCh : privil)
	        {
	            // find a suitable owner for the record (someone with editor, useradmin or administrator role)
	            group = ((Element) grCh).getChildText("groupid");
	            
	            Lib.log.debug("Query for group: "+ group);
	            
	            query = "SELECT DISTINCT id, name, surname, profile FROM Users, UserGroups WHERE id=userId AND profile<>'RegisteredUser' AND groupId=?";

	            List usrGrps = dbms.select(query, new Integer(group)).getChildren();
	            dbms.commit();

	            for (Object o : usrGrps)
	            {
	                Element oGroup = (Element) o;

	                Owner oT = new Owner(oGroup.getChildText("id"),group,oGroup.getChildText("profile"), oGroup.getChildText("name"), oGroup.getChildText("surname"),"");
	                oT.surname = ((Element) o).getChildText("surname");
	                ownersPriv.add(oT);
	                Lib.log.debug(oT.toString() + " added as possible owner.");
	            }
	        }
		}   
		
		// Metadata author(s) and privileges found
		if (ownersPriv!=null && author!=null) {
            if (author.user != null)
            {
                // Loop through all users that are member of the group(s) of the record
                for (Owner owner : ownersPriv) {
                    if (owner.user.equals(author.user)) {
                        // We found the owner! Assign and leave
                        Lib.log.debug("Metadata (id: " + id + ") will be assigned to user: "+ author.user);

                        nMatchedUsers++;
                        owners = new ArrayList<Owner>();
                        owners.add(author);
                        return owners;
                    } 
                    else if (author.profile.equals("Administrator")) { 
                        // The metadata author is an administrator of the site, add it and leave
                        nMatchedUsers++;
                        owners = new ArrayList<Owner>();
                        owners.add(new Owner(owner.user, owner.group, "Administrator"));
                        return owners;
                    }
                }
                // A candidate was not found in the group(s), log this and proceed
                Lib.log.info("Metadata contact (user id: "+ author.user +") was not matched with candidates list");
                Lib.log.debug("Candidate list is:");
                for(Owner o : ownersPriv)
                    Lib.log.debug("  --> user: " + o.surname + " (" + o.user 
                            + "), group: "+ o.groupName + " (" + o.group + ")");
            }
        }		    
		
		// Privileges found, but no potential author
		if (ownersPriv!=null) { 
		    // Select the first editor
            // Loop through all users that are member of the group(s) of the record
            for (Owner owner : ownersPriv) {
                if (owner.profile.equals("Editor")) {
                    // We found the first Editor! Assign and leave
 
                    Lib.log.info("Metadata " + id + 
                            " will be assigned to user " + owner.surname +
                            " ("+ owner.user +") and group " + owner.groupName +
                            " ("+ owner.group+")");

                    nMatchedUsers++;
                    owners = new ArrayList<Owner>();
                    owners.add(new Owner(owner.user, owner.group, owner.profile, owner.name, owner.surname, owner.groupName));
                    return owners;
                } 
            }
            // No editor found, try for the first UserAdmin
            for (Owner owner : ownersPriv) {
                if (owner.profile.equals("UserAdmin")) {
                    // We found the first UserAdmin! Assign and leave
 
                    Lib.log.info("Metadata " + id + 
                            " will be assigned to user " + owner.surname +
                            " ("+ owner.user +") and group " + owner.groupName +
                            " ("+ owner.group+")");

                    nMatchedUsers++;
                    owners = new ArrayList<Owner>();
                    owners.add(new Owner(owner.user, owner.group, owner.profile, owner.name, owner.surname, owner.groupName));
                    return owners;
                } 
            }
            // No editor found, try for the first UserAdmin
            for (Owner owner : ownersPriv) {
                if (owner.profile.equals("Administrator")) {
                    // We found the first UserAdmin! Assign and leave

                    Lib.log.info("Metadata " + id + 
                            " will be assigned to user " + owner.surname +
                            " ("+ owner.user +") and group " + owner.groupName +
                            " ("+ owner.group+")");

                    nMatchedUsers++;
                    owners = new ArrayList<Owner>();
                    owners.add(new Owner(owner.user, owner.group, owner.profile, owner.name, owner.surname, owner.groupName));
                    return owners;
                } 
            }
		}   
		    
		// Metadata author found, but no privileges
		if (author!=null) { 
            // No editor found, try for the first UserAdmin

            if (author.user != null)
            {

                Lib.log.info("Metadata " + id + 
                        " will be assigned to user " + author.user +
                        ", group " + author.group +
                        ", profile "+ author.profile);

                nMatchedUsers++;
                owners = new ArrayList<Owner>();
                owners.add(new Owner(author.user, author.group, author.profile));
                return owners;
            } 
		} 
				
        return assignDefaultUser(id, owners);
	}

    //---------------------------------------------------------------------------

	private List<Owner> assignDefaultUser(String id, List<Owner> owners) throws Exception
	{
        if (oldUserName == null)
        {
            // throw an Exception if default user and group were not given
             throw new Exception("No candidate owner found for metadata with id: "+id);
        } else {
            // Assign default user and group if metadata is not owned
            Lib.log.debug("Metadata with id ("+ id +") has no group with admin or edit privileges. Using user/group provided by GAST)");
            owners.add(new Owner("" + oldUserId, "" + oldGroupId, "Editor", "", oldUserName, oldGroupName));
            nNoPriv++;
            return owners;
        }
	}

	//---------------------------------------------------------------------------

	private Owner getUserFromMetadata(Dbms dbms, Element md) throws Exception
	{
		//--- extract metadata author's surname
		String surname = getSurname(md);
		List<Owner> owners = new ArrayList<Owner>();
		Owner owner = new Owner();
		
		if (surname == null) {
			return null;
		}

		String query = "SELECT id, name, surname, profile FROM Users WHERE surname=? AND profile<>'RegisteredUser'";
		List users = dbms.select(query, surname).getChildren();
		dbms.commit();
		
		if (users.size()>0) {
	        // return (first) user
	        Element user = (Element) users.get(0);

	        owner.user = user.getChildText("id");
            owner.name = user.getChildText("name");
	        owner.surname = user.getChildText("surname");
	        owner.profile = user.getChildText("profile");
	        Lib.log.debug("Author: " + owner.name + " (" + owner.user + ")");
	        String queryGroups = "SELECT DISTINCT userId, groupId, name FROM UserGroups, Groups WHERE groupId=id AND userId=?";
	        List groups = dbms.select(queryGroups, user.getChildText("id")).getChildren();
	        
	        dbms.commit();
	        if (groups.size()!=0 && groups!=null) {
	            // Assign to first group
	            Element group= (Element) groups.get(0);
	            owner.group = group.getChildText("groupid");
	            owner.groupName = group.getChildText("name");
	            if (groups.size()>1) {
	                Lib.log.debug("Other groups:");
    	            for (Object gr : groups){
    	                group = (Element) gr;
    	                Lib.log.debug("  - " + group.getChildText("name") + "(" + group.getChildText("groupid") + ")");                    
    	            } 
	            }

                if (owner.group==null) Lib.log.debug(owner.name + " has no valid groups associated");
	        }
	        Lib.log.debug("Author found: " + owner.toString());
            return owner;
        }
		else 
		    Lib.log.debug("No user found for surname: "+ surname);
		    return null;
	}

	//---------------------------------------------------------------------------

	private Owner promptUser(Dbms dbms, String id, List<Owner> owners) throws SQLException
	{
		for (Owner owner : owners)
		{
			List list = dbms.select("SELECT surname, name FROM Users WHERE id = ?", new Integer(owner.user)).getChildren();

			Element rec = (Element) list.get(0);

			owner.surname = rec.getChildText("surname");
			owner.name    = rec.getChildText("name");

			list = dbms.select("SELECT name FROM Groups WHERE id = ?", new Integer(owner.group)).getChildren();

			rec = (Element) list.get(0);

			owner.groupName = rec.getChildText("name");
		}

		setupAdmins(dbms);

		UserDialog ud = new UserDialog(dlg, owners, admins, groups);

		Owner owner = ud.run();

		Lib.log.debug("For metadata with id ("+id+ ") selected owner with dialog : "+owner);

		return owner;
	}

	//---------------------------------------------------------------------------

	private void setupAdmins(Dbms dbms) throws SQLException
	{
		if (admins != null)
			return;

		admins = new ArrayList<Owner>();

		List list = dbms.select("SELECT * FROM Users ORDER BY profile, surname").getChildren();

		for (Object o : list)
		{
			Element rec = (Element) o;

			Owner owner = new Owner();
			owner.user    = rec.getChildText("id");
			owner.name    = rec.getChildText("surname") +" "+ rec.getChildText("name") +" ("+rec.getChildText("profile") +")";

			admins.add(owner);
		}

		//--- setup groups

		groups = new ArrayList<Owner>();

		list = dbms.select("SELECT * FROM Groups ORDER BY name").getChildren();

		for (Object o : list)
		{
			Element rec = (Element) o;

			Owner owner = new Owner();
			owner.group    = rec.getChildText("id");
			owner.groupName= rec.getChildText("name");

			groups.add(owner);
		}
	}

	//---------------------------------------------------------------------------

	private String getSurname(Element md)
	{
		md = md.getChild("mdContact");

		if (md == null)
			return null;

		String author = md.getChildText("rpIndName");

		if (author == null)
			return null;

		if (author.trim().length() == 0)
			return null;

		StringTokenizer st = new StringTokenizer(author.trim(), " ");

		String surname = st.nextToken();

		if (st.hasMoreTokens())
			surname = st.nextToken();

		return surname;
	}

	//---------------------------------------------------------------------------
	/** Migrates OperationAllowed */

	private void migrateOperationAllowed(Dbms oldDbms, Dbms newDbms) throws SQLException
	{
		String query = "SELECT * FROM OperationAllowed WHERE operationId<>2 AND operationId<>4";

		//--- copy metadata categories

		List operAll = oldDbms.select(query).getChildren();
		oldDbms.commit();

		for (Object oa : operAll)
		{
			Element opAll = (Element) oa;

			if (!mdIds.contains(opAll.getChildText("metadataid")))
				continue;

			Lib.database.insert(newDbms, "OperationAllowed", opAll, relationMapper);

			//--- we can have even millions of records so it is convenient to commit
			//--- at each insert

			newDbms.commit();
		}
	}

	//---------------------------------------------------------------------------
	/** Migrates MetadataCateg */

	private void migrateMetadataCateg(Dbms oldDbms, Dbms newDbms) throws SQLException
	{
		String query = "SELECT * FROM MetadataCateg";

		//--- copy metadata categories

		List oldMdCat = oldDbms.select(query).getChildren();
		oldDbms.commit();

		for (Object o : oldMdCat)
		{
			Element mdCat = (Element) o;

			if (!mdIds.contains(mdCat.getChildText("metadataid")))
				continue;

			Lib.database.insert(newDbms, "MetadataCateg", mdCat, relationMapper);

			//--- we can have even milions of records so it is convenient to commit
			//--- at each insert

			newDbms.commit();
		}
	}

	//---------------------------------------------------------------------------

	private void migrateSettings(Dbms newDbms) throws SQLException
	{
		String query = "UPDATE Settings SET value=? WHERE id=?";

		//--- the following ids are taken from the Settings.ddf file

		newDbms.execute(query, source.getNetwork(),    31);
		newDbms.execute(query, source.getNetmask(),    32);
		newDbms.execute(query, source.getPublicHost(), 21);
		newDbms.execute(query, source.getPublicPort(), 22);
		newDbms.execute(query, source.getZ3950Port(),  42);

		newDbms.commit();
	}

	//---------------------------------------------------------------------------

	private void restoreLocalizedRecords(Dbms newDbms) throws Exception
	{
		DdfLoader r = new DdfLoader();

		//--- fix groups

		LocalizFixer grp = new LocalizFixer();
		r.setHandler(grp);
		r.load(App.path + "/gast/setup/db/Groups.ddf");

		LocalizFixer grpDes = new LocalizFixer();
		r.setHandler(grpDes);
		r.load(App.path + "/gast/setup/db/GroupsDes.ddf");

		restoreLocalizedRecords(newDbms, "GroupsDes", grp, grpDes);

		//--- fix categories

		LocalizFixer cat = new LocalizFixer();
		r.setHandler(cat);
		r.load(App.path + "/gast/setup/db/Categories.ddf");

		LocalizFixer catDes = new LocalizFixer();
		r.setHandler(catDes);
		r.load(App.path + "/gast/setup/db/CategoriesDes.ddf");

		restoreLocalizedRecords(newDbms, "CategoriesDes", cat, catDes);
	}

	//---------------------------------------------------------------------------

	private void restoreLocalizedRecords(Dbms dbms, String table, LocalizFixer base,
												LocalizFixer des) throws SQLException
	{
		String query = "SELECT * FROM "+ table;

		List records = dbms.select(query).getChildren();

		for (Object r : records)
		{
			Element rec = (Element) r;

			String idDes = rec.getChildText("iddes");
			String lang  = rec.getChildText("langid");
			String label = rec.getChildText("label");

			String newId = getIdFromName(base, label);

			if (newId != null)
			{
				label = getLabelFromId(des, newId, lang);
				query = "UPDATE "+ table +" SET label=? WHERE idDes=? and langId=?";

				dbms.execute(query, label, new Integer(idDes), lang);
				dbms.commit();
			}
		}
	}

	//---------------------------------------------------------------------------

	private int getIndex(LocalizFixer fl, String name)
	{
		for (int i=0; i<fl.fields.size(); i++)
		{
			ImportField field = fl.fields.get(i);

			if (field.name.toLowerCase().equals(name.toLowerCase()))
				return i;
		}

		return -1;
	}

	//---------------------------------------------------------------------------

	private String getIdFromName(LocalizFixer lf, String name)
	{
		int idNDX   = getIndex(lf, "id");
		int nameNDX = getIndex(lf, "name");

		for (List<String> row : lf.rows)
			if (row.get(nameNDX).equals(name))
				return row.get(idNDX);

		return null;
	}

	//---------------------------------------------------------------------------

	private String getLabelFromId(LocalizFixer lf, String id, String lang)
	{
		int idDesNDX = getIndex(lf, "iddes");
		int langNDX  = getIndex(lf, "langid");
		int labelNDX = getIndex(lf, "label");

		for (List<String> row : lf.rows)
			if (row.get(idDesNDX).equals(id) && row.get(langNDX).equals(lang))
				return row.get(labelNDX);

		return null;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String      appPath;
	private GNSource    source;
	private Set<String> mdIds = new HashSet<String>();

	private String oldUserName;
	private String oldGroupName;
	private int oldUserId;
	private int oldGroupId;

	private int nNoPriv;
	private int nNoEditor;
	private int nMatchedUsers;
	private int nMultiUser;

	private ProgressDialog dlg;

	private boolean showDialog;

	public List<Owner> admins;
	public List<Owner> groups;

	//---------------------------------------------------------------------------

	private DatabaseLib.Mapper idMapper = new DatabaseLib.Mapper()
	{
		public Object map(String field, Object value)
		{
			if (field.equals("id"))
				return new Integer(value.toString());

			return value;
		}
	};

	//---------------------------------------------------------------------------

	private DatabaseLib.Mapper groupsMapper = new DatabaseLib.Mapper()
	{
		public Object map(String field, Object value)
		{
			if (field.equals("id"))
				return new Integer(value.toString());

			if (field.equals("referrer"))
			{
				String v = value.toString();

				return (v.length() == 0) ? null : new Integer(v);
			}

			return value;
		}
	};

	//---------------------------------------------------------------------------

	private DatabaseLib.Mapper relationMapper = new DatabaseLib.Mapper()
	{
		public Object map(String field, Object value)
		{
			if (field.endsWith("id"))
				return new Integer(value.toString());

			return value;
		}
	};

	//---------------------------------------------------------------------------

	private DatabaseLib.Mapper userMapper = new DatabaseLib.Mapper()
	{
		public Object map(String field, Object value)
		{
			if (field.equals("id"))
				return new Integer(value.toString());

			if (field.equals("password"))
				return Util.scramble(value.toString());

			return value;
		}
	};
}

//==============================================================================

class LocalizFixer implements DdfLoader.Handler
{
	//---------------------------------------------------------------------------
	//---
	//--- Handler interface
	//---
	//---------------------------------------------------------------------------

	public void handleFields(List<ImportField> fields) throws Exception
	{
		this.fields = fields;
	}

	//---------------------------------------------------------------------------

	public void handleRow(List<String> values) throws Exception
	{
		for (int i=0; i<values.size(); i++)
			values.set(i, Codec.decodeString(values.get(i)));

		rows.add(values);
	}

	//---------------------------------------------------------------------------

	public void cleanUp() {}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public List<ImportField>  fields;
	public List<List<String>> rows = new ArrayList<List<String>>();
}

//==============================================================================

class Owner
{
	public String user;
	public String group;
	public String profile;

	public String surname;
	public String name;
	public String groupName;

	public Owner() {}

	public Owner(String user, String group, String profile)
	{
		this.user = user;
		this.group= group;
		this.profile= profile;
	}
	
    public Owner(String user, String group, String profile, String name, String surname, String groupName)
    {
        this.user = user;
        this.group= group;
        this.profile= profile;
        this.name= name;
        this.surname= surname;
        this.groupName= groupName;
    }

    public String toString()
	{
		if (name != null)
			return name +" "+ surname +"("+user+"), group: "+ groupName +" ("+group+"), profile: "+ profile+"";

		return "user:"+ user +", group:"+ group +", profile:" + profile;
	}
}


//==============================================================================
