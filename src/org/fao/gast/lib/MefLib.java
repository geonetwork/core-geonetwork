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

package org.fao.gast.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import jeeves.exceptions.BadFormatEx;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.mef.MEF2Visitor;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.mef.IMEFVisitor;
import org.fao.geonet.kernel.mef.MEFVisitor;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

//=============================================================================

public class MefLib {
	// ---------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// ---------------------------------------------------------------------------

	public void doImport(final Dbms dbms, final int id, File mefFile)
			throws Exception {
		final Element md[] = { null };
		final Element fc[] = { null };

		// --- import metadata from MEF file

		Lib.log.info("Adding MEF file : " + mefFile.getAbsolutePath());

		MEFLib.visit(mefFile, new MEFVisitor(), new IMEFVisitor() {
			public void handleMetadata(Element mdata, int index)
					throws Exception {
				md[index] = mdata;
			}

			// --------------------------------------------------------------------

			public void handleMetadataFiles(File[] files, int index)
					throws Exception {
				// Preferred schema is define in config.xml for GeoNetwork.
				// TODO : add as parameter for GAST
				String preferredSchema = "iso19139";

				Element metadataValidForImport = null;

				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file != null && !file.isDirectory()) {
						Element metadata = Xml.loadFile(file);
						// Use MetadataLib instead of DataManager because run
						// without starting the application.
						String metadataSchema = MetadataLib
								.autodetectSchema(metadata);

						// If local node doesn't know metadata
						// schema try to load next xml file.
						if (metadataSchema == null)
							continue;

						// If schema is preferred local node schema
						// load that file.
						if (metadataSchema.equals(preferredSchema)) {
							Lib.log.debug("Found metadata file "
									+ file.getName()
									+ " with preferred schema ("
									+ preferredSchema + ").");
							handleMetadata(metadata, index);
							return;
						} else {
							Lib.log.debug("Found metadata file "
									+ file.getName() + " with known schema ("
									+ metadataSchema + ").");
							metadataValidForImport = metadata;
						}
					}
				}

				// Import a valid metadata if not one found
				// with preferred schema.
				if (metadataValidForImport != null) {
					Lib.log
							.debug("Importing metadata with valide schema but not preferred one.");
					handleMetadata(metadataValidForImport, index);
					return;
				} else
					throw new BadFormatEx("No valid metadata file found.");
			}

			// --------------------------------------------------------------------

			public void handleInfo(Element info, int index) throws Exception {
				addMetadata(dbms, id, md[index], fc[index], info);
			}

			// --------------------------------------------------------------------

			public void handlePublicFile(String file, String changeDate,
					InputStream is, int index) throws IOException {
				saveFile(id, file, changeDate, is, "public");
			}

			// --------------------------------------------------------------------

			public void handlePrivateFile(String file, String changeDate,
					InputStream is, int index) throws IOException {
				saveFile(id, file, changeDate, is, "private");
			}

			public void handleFeatureCat(Element featureCat, int index)
					throws Exception {
				fc[index] = featureCat;
			}
		});
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// ---------------------------------------------------------------------------

	private void addMetadata(Dbms dbms, int id, Element md, Element fc,
			Element info) throws Exception {
		int owner = 1;

		String groupOwner = null;

		Element general = info.getChild("general");

		String uuid = general.getChildText("uuid");
		String createDate = general.getChildText("createDate");
		String changeDate = general.getChildText("changeDate");
		String source = general.getChildText("siteId");
		String schema = general.getChildText("schema");
		String isTemplate = general.getChildText("isTemplate").equals("true") ? "y"
				: "n";

		boolean dcore = schema.equals("dublin-core");
		boolean fgdc = schema.equals("fgdc-std");
		boolean iso115 = schema.equals("iso19115");
		boolean iso139 = schema.equals("iso19139");
		boolean iso139fra = schema.equals("iso19139fra");

		// TODO : Add this as parameters for GAST
		if (!dcore && !fgdc && !iso115 && !iso139 && !iso139fra)
			throw new Exception("Unknown schema format : " + schema);

		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
			source = Lib.site.getSiteId(dbms);

			// --- set uuid inside metadata
			md = Lib.metadata.setUUID(schema, uuid, md);
		}

		Lib.log.debug(" - Adding metadata with uuid : " + uuid
				+ " using schema:" + schema);

		Lib.metadata.insertMetadata(dbms, schema, md, id, source, createDate,
				changeDate, uuid, owner, groupOwner, isTemplate, null);

		addCategories(dbms, id, info.getChild("categories"));
		addPrivileges(dbms, id, info.getChild("privileges"));

		if (fc != null) {
			uuid = UUID.randomUUID().toString();
			// FIXME : UUID is set as gfc;name of the feature catalogue...
			// Should we add a uuid to feature catalog ?
			fc = Lib.metadata.setUUID("iso19110", uuid, fc);

			int fcId = id + 1;
			Lib.metadata.insertMetadata(dbms, "iso19110", fc, fcId, source,
					createDate, changeDate, uuid, owner, groupOwner,
					isTemplate, null);

			Lib.log.debug("Adding Feature catalog with uuid=" + uuid);

			// Create database relation between metadata and feature catalog
			String query = "INSERT INTO Relations (id, relatedId) "
					+ "VALUES (?, ?)";
			dbms.execute(query, id, fcId);
			Lib.log.debug("Relation between metadata (" + id
					+ ") and feature catalog (" + fcId + ") created.");
		}

	}

	// --------------------------------------------------------------------------

	private void saveFile(int id, String file, String changeDate,
			InputStream is, String access) throws IOException {
		Lib.log.debug(" - Adding '" + access + "' file with name : " + file);

		File outDir = new File(Lib.metadata.getDir(id, access));
		File outFile = new File(outDir, file);

		Lib.log.debug("   - Destination folder is : "
				+ outDir.getAbsolutePath());
		outDir.mkdirs();

		Lib.io.save(outFile, is);
		outFile.setLastModified(new ISODate(changeDate).getSeconds() * 1000);
	}

	// --------------------------------------------------------------------------
	// --- Categories
	// --------------------------------------------------------------------------

	private void addCategories(Dbms dbms, int id, Element categ)
			throws Exception {
		List locCats = dbms.select("SELECT id, name FROM Categories")
				.getChildren();
		List list = categ.getChildren("category");

		for (Iterator j = list.iterator(); j.hasNext();) {
			String catName = ((Element) j.next()).getAttributeValue("name");
			String catId = mapLocalEntity(locCats, catName);

			if (catId == null)
				Lib.log.debug(" - Skipping inesistent category : " + catName);
			else {
				// --- metadata category exists locally

				Lib.log.debug(" - Setting category : " + catName);

				String query = "INSERT INTO MetadataCateg(metadataId, categoryId) VALUES(?,?)";

				dbms.execute(query, id, new Integer(catId));
			}
		}
	}

	// --------------------------------------------------------------------------
	// --- Privileges
	// --------------------------------------------------------------------------

	private void addPrivileges(Dbms dbms, int id, Element privil)
			throws Exception {
		List locGrps = dbms.select("SELECT id,name FROM Groups").getChildren();
		List list = privil.getChildren("group");

		for (Object g : list) {
			Element group = (Element) g;
			String grpName = group.getAttributeValue("name");
			String grpId = mapLocalEntity(locGrps, grpName);

			if (grpId == null)
				Lib.log.debug(" - Skipping inesistent group : " + grpName);
			else {
				// --- metadata group exists locally

				Lib.log.debug(" - Setting privileges for group : " + grpName);
				addOperations(dbms, group, id, Integer.parseInt(grpId));
			}
		}
	}

	// --------------------------------------------------------------------------

	private void addOperations(Dbms dbms, Element group, int id, int grpId)
			throws Exception {
		List opers = group.getChildren("operation");

		for (int j = 0; j < opers.size(); j++) {
			Element oper = (Element) opers.get(j);
			String opName = oper.getAttributeValue("name");

			int opId = getPrivilegeId(opName);

			if (opId == -1)
				Lib.log.debug("   Skipping --> " + opName);
			else {
				// --- operation exists locally

				Lib.log.debug("   Adding --> " + opName);

				String query = "INSERT INTO OperationAllowed(metadataId, groupId, operationId) VALUES(?,?,?)";

				dbms.execute(query, id, grpId, opId);
			}
		}
	}

	// --------------------------------------------------------------------------

	private int getPrivilegeId(String descr) {
		if (descr.equals("view"))
			return 0;
		if (descr.equals("download"))
			return 1;
		if (descr.equals("notify"))
			return 3;
		if (descr.equals("dynamic"))
			return 5;
		if (descr.equals("featured"))
			return 6;

		return -1;
	}

	// --------------------------------------------------------------------------
	// --- General private methods
	// --------------------------------------------------------------------------

	private String mapLocalEntity(List entities, String name) {
		for (Object e : entities) {
			Element entity = (Element) e;

			if (entity.getChildText("name").equals(name))
				return entity.getChildText("id");
		}

		return null;
	}
}

// =============================================================================

