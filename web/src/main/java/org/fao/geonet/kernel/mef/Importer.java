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

package org.fao.geonet.kernel.mef;

import jeeves.exceptions.BadFormatEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.ISODate;
import org.fao.oaipmh.exceptions.BadArgumentException;
import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Importer {
	public static List<String> doImport(final Element params,
			final ServiceContext context, File mefFile, final String stylePath)
			throws Exception {
			return doImport(params, context, mefFile, stylePath, false);
	}

	public static List<String> doImport(final Element params,
			final ServiceContext context, File mefFile, final String stylePath,
			final boolean indexGroup) throws Exception {
		final GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		final DataManager dm = gc.getDataManager();

		// Load preferred schema and set to iso19139 by default
		final String preferredSchema = (gc.getHandlerConfig()
				.getMandatoryValue("preferredSchema") != null ? gc
				.getHandlerConfig().getMandatoryValue("preferredSchema")
				: "iso19139");

		final Dbms dbms = (Dbms) context.getResourceManager().open(
				Geonet.Res.MAIN_DB);

		final List<String> id = new ArrayList<String>();
		final List<Element> md = new ArrayList<Element>();
		final List<Element> fc = new ArrayList<Element>();

		// Try to define MEF version from mef file not from parameter
		String fileType = Util.getParam(params, "file_type", "mef");
		if (fileType.equals("mef")) {
			MEFLib.Version version = MEFLib.getMEFVersion(mefFile);
			if (version.equals(MEFLib.Version.V2))
				fileType = "mef2";
		}

		IVisitor visitor;

		if (fileType.equals("single"))
			visitor = new XmlVisitor();
		else if (fileType.equals("mef"))
			visitor = new MEFVisitor();
		else if (fileType.equals("mef2"))
			visitor = new MEF2Visitor();
		else
			throw new BadArgumentException("Bad file type parameter.");

		// --- import metadata from MEF, Xml, ZIP files
		MEFLib.visit(mefFile, visitor, new IMEFVisitor() {

			public void handleMetadata(Element metadata, int index)
					throws Exception {
				Log.debug(Geonet.MEF, "Collecting metadata:\n"
						+ Xml.getString(metadata));
				md.add(index, metadata);
			}

			public void handleMetadataFiles(File[] Files, int index)
					throws Exception {
				Log.debug(Geonet.MEF, "Multiple metadata files");

				Element metadataValidForImport = null;

                for (File file : Files) {
                    if (file != null && !file.isDirectory()) {
                        Element metadata = Xml.loadFile(file);
                        String metadataSchema = dm.autodetectSchema(metadata, null);

                        // If local node doesn't know metadata
                        // schema try to load next xml file.
                        if (metadataSchema == null) {
                            continue;
                        }

                        // If schema is preferred local node schema
                        // load that file.
                        if (metadataSchema.equals(preferredSchema)) {
                            Log.debug(Geonet.MEF, "Found metadata file "
                                    + file.getName()
                                    + " with preferred schema ("
                                    + preferredSchema + ").");
                            handleMetadata(metadata, index);
                            return;
                        }
                        else {
                            Log.debug(Geonet.MEF, "Found metadata file "
                                    + file.getName() + " with known schema ("
                                    + metadataSchema + ").");
                            metadataValidForImport = metadata;
                        }
                    }
                }

				// Import a valid metadata if not one found
				// with preferred schema.
				if (metadataValidForImport != null) {
					Log
							.debug(Geonet.MEF,
									"Importing metadata with valide schema but not preferred one.");
					handleMetadata(metadataValidForImport, index);
                } else
					throw new BadFormatEx("No valid metadata file found.");
			}

			// --------------------------------------------------------------------

			public void handleFeatureCat(Element featureCat, int index)
					throws Exception {
				if (featureCat != null) {
					Log.debug(Geonet.MEF, "Collecting feature catalog:\n"
							+ Xml.getString(featureCat));
				}
				fc.add(index, featureCat);
			}

			// --------------------------------------------------------------------

			/**
			 * Record is not a template by default. No category attached to
			 * record by default. No stylesheet used by default. If no site
			 * identifier provided, use current node id by default. No
			 * validation by default.
			 * 
			 * If record is a template and not a MEF file always generate a new
			 * UUID.
			 */
			public void handleInfo(Element info, int index) throws Exception {

				String FS = File.separator;

				String uuid = null;
				String createDate = null;
				String changeDate = null;
				String source;
				String sourceName = null;
				// Schema in info.xml is not used anymore.
				// as we use autodetect schema to define
				// metadata schema.
				// String schema = null;
				String isTemplate;
				String localId = null;
				String rating = null;
				String popularity = null;
				String groupId = null;
				Element categs = null;
				Element privileges;
				boolean validate = false;

				
				// Apply a stylesheet transformation if requested
				String style = Util.getParam(params, Params.STYLESHEET,
				"_none_");

				if (!style.equals("_none_"))
					md.add(index, Xml.transform(md.get(index), stylePath
							+ FS + style));
				
				
				Element metadata = md.get(index);
				String schema = dm.autodetectSchema(metadata);

				if (schema == null)
					throw new Exception("Unknown schema format : " + schema);

				// Handle non MEF files insertion
				if (info.getChildren().size() == 0) {
					source = Util.getParam(params, Params.SITE_ID, gc
							.getSiteId());
					isTemplate = Util.getParam(params, Params.TEMPLATE, "n");

					String category = Util
							.getParam(params, Params.CATEGORY, "");
					if (!category.equals("")) {
						categs = new Element("categories");
						categs.addContent((new Element("category"))
								.setAttribute("name", category));
					}

					groupId = Util.getParam(params, Params.GROUP);
					privileges = new Element("group");
					privileges.addContent(new Element("operation")
							.setAttribute("name", "view"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "editing"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "download"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "notify"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "dynamic"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "featured"));

					// Get the Metadata uuid if it's not a template.
					if (isTemplate.equals("n"))
						uuid = dm.extractUUID(schema, md.get(index));

					validate = Util.getParam(params, Params.VALIDATE, "off")
							.equals("on");

				} else {
					Log.debug(Geonet.MEF, "Collecting info file:\n"
							+ Xml.getString(info));
					
					categs = info.getChild("categories");
					privileges = info.getChild("privileges");

					Element general = info.getChild("general");

					uuid = general.getChildText("uuid");
					createDate = general.getChildText("createDate");
					changeDate = general.getChildText("changeDate");
					// --- If siteId is not set, set to current node
					source = Util.getParam(general, Params.SITE_ID, gc
							.getSiteId());
					sourceName = general.getChildText("siteName");
					localId = general.getChildText("localId");
					isTemplate = general.getChildText("isTemplate").equals(
							"true") ? "y" : "n";
					rating = general.getChildText("rating");
					popularity = general.getChildText("popularity");
				}

				if (validate) {
					// Validate xsd and schematron
					dm.validateMetadata(schema, metadata, context);
                }

				String uuidAction = Util.getParam(params, Params.UUID_ACTION,
						Params.NOTHING);

				importRecord(uuid, localId, uuidAction, md, schema, index,
						source, sourceName, context, id, createDate,
						changeDate, groupId, isTemplate);

				if (fc.size() != 0 && fc.get(index) != null) {
					// UUID is set as @uuid in root element
					uuid = UUID.randomUUID().toString();

					fc.add(index, dm.setUUID("iso19110", uuid, fc.get(index)));

                    //
                    // insert metadata
                    //
                    int userid = context.getUserSession().getUserIdAsInt();
                    String group = null, docType = null, title = null, category = null;
                    boolean ufo = false, indexImmediate = false;
                    String fcId = dm.insertMetadata(dbms, "iso19110", fc.get(index), context.getSerialFactory().getSerial(dbms, "Metadata"), uuid,
                            userid, group, source, isTemplate, docType, title, category, createDate, changeDate, ufo, indexImmediate);

					Log.debug(Geonet.MEF, "Adding Feature catalog with uuid: " + uuid);

					// Create database relation between metadata and feature
					// catalog
					String mdId = id.get(index);
					String query = "INSERT INTO Relations (id, relatedId) VALUES (?, ?)";
					dbms.execute(query, Integer.parseInt(mdId), Integer.parseInt(fcId));

					id.add(fcId);
					// TODO : privileges not handled for feature catalog ...
				}

				int iId = Integer.parseInt(id.get(index));

				if (rating != null)
					dbms.execute("UPDATE Metadata SET rating=? WHERE id=?",
							new Integer(rating), iId);

				if (popularity != null)
					dbms.execute("UPDATE Metadata SET popularity=? WHERE id=?",
							new Integer(popularity), iId);

				dm.setTemplateExt(dbms, iId, isTemplate, null);
				dm.setHarvestedExt(dbms, iId, null);

				String pubDir = Lib.resource.getDir(context, "public", id
						.get(index));
				String priDir = Lib.resource.getDir(context, "private", id
						.get(index));

				new File(pubDir).mkdirs();
				new File(priDir).mkdirs();

				if (categs != null)
					addCategories(dm, dbms, id.get(index), categs);

				if (groupId == null)
					addPrivileges(dm, dbms, id.get(index), privileges);
				else
					addOperations(dm, dbms, privileges, id.get(index), groupId);

				if (indexGroup) {
					dm.indexMetadataGroup(dbms, id.get(index));
				}
                else {
                    boolean indexGroup = false;
                    dm.indexMetadata(dbms, id.get(index), indexGroup);
				}
			}

			// --------------------------------------------------------------------

			public void handlePublicFile(String file, String changeDate,
					InputStream is, int index) throws IOException {
				Log.debug(Geonet.MEF, "Adding public file with name=" + file);
				saveFile(context, id.get(index), "public", file, changeDate, is);
			}

			// --------------------------------------------------------------------

			public void handlePrivateFile(String file, String changeDate,
					InputStream is, int index) throws IOException {
				Log.debug(Geonet.MEF, "Adding private file with name=" + file);
				saveFile(context, id.get(index), "private", file, changeDate,
						is);
			}

		});

		return id;
	}

	public static void importRecord(String uuid, String localId,
			String uuidAction, List<Element> md, String schema, int index,
			String source, String sourceName, ServiceContext context,
			List<String> id, String createDate, String changeDate,
			String groupId, String isTemplate) throws Exception {

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getDataManager();
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		if (uuid == null || uuid.equals("")
				|| uuidAction.equals(Params.GENERATE_UUID)) {
			String newuuid = UUID.randomUUID().toString();
			source = null;

			Log
					.debug(Geonet.MEF, "Replacing UUID " + uuid + " with "
							+ newuuid);
			uuid = newuuid;

			// --- set uuid inside metadata
			md.add(index, dm.setUUID(schema, uuid, md.get(index)));
		} else {
			if (sourceName == null)
				sourceName = "???";

			if (source == null || source.trim().length() == 0)
				throw new Exception(
						"Missing siteId parameter from info.xml file");

			// --- only update sources table if source is not current site 
			if (!source.equals(gc.getSiteId())) { 
				Lib.sources.update(dbms, source, sourceName, true); 
			}
		}

		try {
			if (dm.existsMetadataUuid(dbms, uuid) && !uuidAction.equals(Params.NOTHING)) {
                // user has privileges to replace the existing metadata
                if(dm.getAccessManager().canEdit(context, dm.getMetadataId(dbms, uuid))) {
                    dm.deleteMetadata(dbms, dm.getMetadataId(dbms, uuid));
                    Log.debug(Geonet.MEF, "Deleting existing metadata with UUID : " + uuid);
                }
                // user does not hav privileges to replace the existing metadata
                else {
                    throw new UnAuthorizedException("User has no privilege to replace existing metadata", null);
                }
			}
		}
        catch (Exception e) {
            throw new Exception(" Existing metadata with UUID " + uuid + " could not be deleted. Error is: " + e.getMessage());
		}



		Log.debug(Geonet.MEF, "Adding metadata with uuid:" + uuid);
		
		// Try to insert record with localId provided, if not use a new id.
		boolean insertedWithLocalId = false;
		if (localId != null && !localId.equals("")) {
			try {
				int iLocalId = Integer.parseInt(localId);
		
				// Use the same id to insert the metadata record.
				// This is an optional element. If present, indicates the
				// id used locally by the sourceId actor to store the metadata. Its
				// purpose is just to allow the reuse of the same local id when
				// reimporting a metadata. 
				if (!dm.existsMetadata(dbms, iLocalId)) {
					Log.debug(Geonet.MEF, "Using given localId: " + localId);

                    //
                    // insert metadata
                    //
                    String docType = "", title = null, category = null;
                    boolean ufo = false, indexImmediate = false;
                    dm.insertMetadata(dbms, schema, md.get(index), iLocalId, uuid, context.getUserSession().getUserIdAsInt(), groupId, source,
                        isTemplate, docType, title, category, createDate, changeDate, ufo, indexImmediate);

					id.add(index, Integer.toString(iLocalId));

					insertedWithLocalId = true;
				}
			} catch (NumberFormatException e) {
				Log.debug(Geonet.MEF, "Invalid localId provided: " + localId + ". Adding record with a new id.");
			}
		} 
		
		if (!insertedWithLocalId) {
            //
            // insert metadata
            //
            int userid = context.getUserSession().getUserIdAsInt();
            String docType = null, title = null, category = null;
            boolean ufo = false, indexImmediate = false;
            id.add(index,
                    dm.insertMetadata(dbms, schema, md.get(index), context.getSerialFactory().getSerial(dbms, "Metadata"), uuid,
                    userid, groupId, source, isTemplate, docType, title, category, createDate, changeDate, ufo, indexImmediate));
		}

	}

	// --------------------------------------------------------------------------

	private static void saveFile(ServiceContext context, String id,
			String access, String file, String changeDate, InputStream is)
			throws IOException {
		String dir = Lib.resource.getDir(context, access, id);

		File outFile = new File(dir, file);
		FileOutputStream os = new FileOutputStream(outFile);
		BinaryFile.copy(is, os, false, true);

		outFile.setLastModified(new ISODate(changeDate).getSeconds() * 1000);
	}

	/**
	 * Add categories registered in information file.
	 * 
	 * @param dm
	 * @param dbms
	 * @param id
	 * @param categ
	 * @throws Exception
	 */
	public static void addCategories(DataManager dm, Dbms dbms, String id,
			Element categ) throws Exception {
		List locCats = dbms.select("SELECT id,name FROM Categories")
				.getChildren();
		List list = categ.getChildren("category");

        for (Object aList : list) {
            String catName = ((Element) aList).getAttributeValue("name");
            String catId = mapLocalEntity(locCats, catName);

            if (catId == null) {
                Log.debug(Geonet.MEF, " - Skipping inexistent category : "
                        + catName);
            }
            else {
                // --- metadata category exists locally
                Log.debug(Geonet.MEF, " - Setting category : " + catName);
                dm.setCategory(dbms, id, catId);
            }
        }
	}

	/**
	 * Add privileges according to information file.
	 * 
	 * @param dm
	 * @param dbms
	 * @param id
	 * @param privil
	 * @throws Exception
	 */
	private static void addPrivileges(DataManager dm, Dbms dbms, String id,
			Element privil) throws Exception {
		List locGrps = dbms.select("SELECT id,name FROM Groups").getChildren();
		List list = privil.getChildren("group");

		for (Object g : list) {
			Element group = (Element) g;
			String grpName = group.getAttributeValue("name");
			boolean groupOwner = group.getAttributeValue("groupOwner") != null;
			String grpId = mapLocalEntity(locGrps, grpName);

			if (grpId == null)
				Log.debug(Geonet.MEF, " - Skipping inesistent group : "
						+ grpName);
			else {
				// --- metadata group exists locally

				Log.debug(Geonet.MEF, " - Setting privileges for group : "
						+ grpName);
				addOperations(dm, dbms, group, id, grpId);
				if (groupOwner) {
					Log.debug(Geonet.MEF, grpName + " set as group Owner ");
					dm.setGroupOwner(dbms, id, grpId);
				}
			}
		}
	}

	/**
	 * Add operations according to information file.
	 * 
	 * @param dm
	 * @param dbms
	 * @param group
	 * @param id
	 * @param grpId
	 * @throws Exception
	 */
	private static void addOperations(DataManager dm, Dbms dbms, Element group,
			String id, String grpId) throws Exception {
		List opers = group.getChildren("operation");

        for (Object oper1 : opers) {
            Element oper = (Element) oper1;
            String opName = oper.getAttributeValue("name");

            int opId = dm.getAccessManager().getPrivilegeId(opName);

            if (opId == -1) {
                Log.debug(Geonet.MEF, "   Skipping --> " + opName);
            }
            else {
                // --- operation exists locally

                Log.debug(Geonet.MEF, "   Adding --> " + opName);
                dm.setOperation(dbms, id, grpId, opId + "");
            }
        }
	}

	private static String mapLocalEntity(List entities, String name) {
		for (Object e : entities) {
			Element entity = (Element) e;

			if (entity.getChildText("name").equals(name)
					|| entity.getChildText("id").equals(name))
				return entity.getChildText("id");
		}

		return null;
	}
}

// =============================================================================

