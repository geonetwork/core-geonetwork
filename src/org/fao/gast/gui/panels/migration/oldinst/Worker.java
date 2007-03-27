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
import jeeves.resources.dbms.Dbms;
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
	//---
	//--- Migration process
	//---
	//---------------------------------------------------------------------------

	public void run()
	{
		if (!openSource())
			return;

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

	private void executeJob(Dbms oldDbms, Dbms newDbms) throws Exception
	{
		dlg.reset(9);
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
	}

	//---------------------------------------------------------------------------

	private void removeAll(Dbms newDbms) throws SQLException
	{
		//--- we commit at each step because an old migration could have failed and
		//--- we could have plenty of records

		newDbms.execute("DELETE FROM OperationAllowed");
		newDbms.commit();
		newDbms.execute("DELETE FROM UserGroups");
		newDbms.commit();
		newDbms.execute("DELETE FROM MetadataCateg");
		newDbms.commit();
		newDbms.execute("DELETE FROM Users");
		newDbms.commit();
		newDbms.execute("DELETE FROM Metadata");
		newDbms.commit();
		newDbms.execute("DELETE FROM GroupsDes");
		newDbms.execute("DELETE FROM Groups");
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
			Lib.database.insert(newDbms, "Users", (Element) user, idMapper);

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

			//--- no, ok we can store it

			md.getChild("source").setText(newSiteId);

			Element xml = XmlSerializer.select(oldDbms, "Metadata", id);
			oldDbms.commit();

			md.addContent(new Element("root")       .setText(xml.getName()));
			md.addContent(new Element("isHarvested").setText("n"));
			md.addContent(new Element("data")       .setText(Xml.getString(xml)));

			Lib.database.insert(newDbms, "Metadata", md, idMapper);

			//--- we can have even milions of records so it is convenient to commit
			//--- at each insert

			newDbms.commit();

			mdIds.add(id);
		}

		System.out.println("Migrated metadata : "+ mdIds.size());
	}

	//---------------------------------------------------------------------------
	/** Migrates OperationAllowed */

	private void migrateOperationAllowed(Dbms oldDbms, Dbms newDbms) throws SQLException
	{
		String query = "SELECT * FROM OperationAllowed";

		//--- copy metadata categories

		List operAll = oldDbms.select(query).getChildren();
		oldDbms.commit();

		for (Object oa : operAll)
		{
			Element opAll = (Element) oa;

			if (!mdIds.contains(opAll.getChildText("metadataid")))
				continue;

			Lib.database.insert(newDbms, "OperationAllowed", opAll, relationMapper);

			//--- we can have even milions of records so it is convenient to commit
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

