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

	public void setOldUser(String user)
	{
		oldUserName = user;
	}

	public void setOldGroup(String group)
	{
		oldGroupName = group;
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

		// Chcek if the user is an Editor
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
								nMatchedUsers + " users were successfully assigned from their surname\n";

		Lib.gui.showInfo(dlg, message);
		Lib.log.info(message);
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

		String query = "SELECT id, uuid, schemaId, isTemplate, createDate, "+
							"       lastChangeDate as changeDate, source "+
							"FROM   Metadata";

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

			//--- calculate owner

			query = "SELECT groupId FROM OperationAllowed WHERE metadataId=? AND operationId=4";
			List privil = oldDbms.select(query, new Integer(id)).getChildren();
			oldDbms.commit();

			String owner      = null;
			String groupOwner = null;

			//--- retrieve metadata

			md.getChild("source").setText(newSiteId);

			Element xml = XmlSerializer.select(oldDbms, "Metadata", id);
			oldDbms.commit();

			//--- find out group with admin privilege

			if (privil.size() == 0)
			{
//				Lib.log.debug("Metadata with id:"+id+" has no group with 'admin' privilege. Trying with the 'edit' one");

				//--- re-execute the query, now looking for the 'edit' privilege
				query  = "SELECT groupId FROM OperationAllowed WHERE metadataId=? AND operationId=2";
				privil = oldDbms.select(query, new Integer(id)).getChildren();
				oldDbms.commit();

				if (privil.size() == 0)
				{
					if (oldUserName == null)
						// throw an Exception if default user and group were not given
						throw new Exception("Metadata has no 'admin' and 'edit' privileges --> id: " + id);
					else
					{
						// assign default user and group if metadata is not owned
						Lib.log.info("Metadata has no admin privilege --> id: " + id + " - assigned to default user and group");
						owner      = "" + oldUserId;
						groupOwner = "" + oldGroupId;
						nNoPriv++;
					}
				}
			}

			if (privil.size() != 0)
			{
				//--- scan all admin (or editor) groups looking for the first editor

				List<String> possibleOwners = new ArrayList<String>();

				for (int i=0; i<privil.size(); i++)
				{
					//--- calculate group owner (the one with 'admin' or 'edit' privilege)
					groupOwner = ((Element) privil.get(i)).getChildText("groupid");

					query = "SELECT DISTINCT id FROM Users, UserGroups WHERE id=userId AND profile='Editor' AND groupId=?";

					List usrGrps = oldDbms.select(query, new Integer(groupOwner)).getChildren();
					oldDbms.commit();

					if (usrGrps.size() != 0)
						possibleOwners.add(((Element) usrGrps.get(0)).getChildText("id"));
				}

				//--- only 1 possible editor -> assign it

				if (possibleOwners.size() == 1)
					owner = possibleOwners.get(0);

				//--- more possible editors -> try to match the best one

				else if (possibleOwners.size() > 1)
				{
					Lib.log.debug("Found "+ possibleOwners.size() +" owners for metadata with : " + id);

					//--- extract metadata author's surname
					String surname = getSurname(xml);

					if (surname == null)
					{
						Lib.log.debug("   No surname found in metadata, assigning first possible owner : "+ possibleOwners.get(0));
						owner = possibleOwners.get(0);
					}
					else
					{
						query = "SELECT id FROM Users WHERE surname=? AND profile='Editor'";

						List users = oldDbms.select(query, surname).getChildren();
						oldDbms.commit();

						if (users.size() == 0)
						{
							Lib.log.debug("   No surname '"+ surname +"' found in users table, assigning first possible owner : "+ possibleOwners.get(0));
							owner = possibleOwners.get(0);
						}

						else if (users.size() == 1)
						{
							for (Object o : users)
							{
								String userId = ((Element) o).getChildText("id");

								if (possibleOwners.contains(userId))
								{
									Lib.log.info("   -> User successfully assigned with id : "+userId +" for surname : "+surname);
									nMatchedUsers++;
									owner = userId;
									break;
								}

								if (owner == null)
								{
									Lib.log.debug("List of possible users does not contain user with surname : "+ surname);
									Lib.log.debug("   -> Assigning first possible owner : "+ possibleOwners.get(0));
									owner = possibleOwners.get(0);
								}
							}
						}

						else
						{
							Lib.log.debug("Several users found with the same surname, assigning first possible owner : "+ possibleOwners.get(0));

							for (Object o : users)
								Lib.log.info("   -> Found user with id : "+ ((Element)o).getChildText("id"));

							owner = possibleOwners.get(0);
						}
					}
				}

				//--- no candidates -> use a default

				else
				{
					if (oldUserName == null)
					{
						// throw an Exception if default user and group were not given
						//throw new Exception("No editor for metadata --> id: " + id);
						Lib.log.info("Skipping metadata with no editors id:"+id+ " (assigned to administrator)");

						for (Object rec : privil)
							Lib.log.info("   -> Found group with admin/edit privilege was : "+ ((Element) rec).getChildText("groupid"));

						owner="1";
					}
					else
					{
						// assign default user and group if metadata is not owned
//						System.out.println("No editor for metadata --> id: " + id + " - assigned to default user and group"); // DEBUG
						owner      = "" + oldUserId;
						groupOwner = "" + oldGroupId;
						nNoEditor++;
					}
				}
			}

			//--- no, ok we can store it

			md.addContent(new Element("root")       .setText(xml.getName()));
			md.addContent(new Element("isHarvested").setText("n"));
			md.addContent(new Element("data")       .setText(Xml.getString(xml)));
			md.addContent(new Element("owner")      .setText(owner));
			md.addContent(new Element("groupOwner") .setText(groupOwner));

//			Lib.log.debug("metadata: " + id + " - owner: " + owner + " - group owner: " + groupOwner);

			Lib.database.insert(newDbms, "Metadata", md, idMapper);

			//--- we can have even millions of records so it is convenient to commit
			//--- at each insert

			newDbms.commit();

			mdIds.add(id);
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

	private ProgressDialog dlg;

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

