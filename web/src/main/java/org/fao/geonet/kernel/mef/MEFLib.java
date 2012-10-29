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

import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PRIVATE;
import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PUBLIC;
import static org.fao.geonet.kernel.mef.MEFConstants.FS;
import static org.fao.geonet.kernel.mef.MEFConstants.VERSION;


/**
 * Utility class for MEF import and export.
 */
public class MEFLib {

	public enum Format {
		/**
		 * Only metadata record and infomation
		 */
		SIMPLE,
		/**
		 * Include public folder
		 */
		PARTIAL,
		/**
		 * Include private folder. Full is default format if none defined.
		 */
		FULL;

		// ------------------------------------------------------------------------

		public static Format parse(String format) throws BadInputEx {
			if (format == null)
				return FULL;
			// throw new MissingParameterEx("format");

			if (format.equals("simple"))
				return SIMPLE;
			if (format.equals("partial"))
				return PARTIAL;
			if (format.equals("full"))
				return FULL;

			throw new BadParameterEx("format", format);
		}

		// ------------------------------------------------------------------------

		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	/**
	 * MEF file version.
	 * 
	 * MEF file is composed of one or more metadata record with extra
	 * information managed by GeoNetwork. Metadata is in XML format. An
	 * information file (info.xml) is used to transfert general informations,
	 * categories, privileges and file references information. A public and
	 * private directories allows data transfert (eg. thumbnails, data upload).
	 * 
	 */
	public enum Version {
		/**
		 * Version 1 is composed of one metadata file. <pre>
		 * Root 
		 * | 
		 * +--- metadata.xml
		 * +--- info.xml 
		 * +--- public 
		 * |    +---- all public documents and thumbnails
		 * +--- private 
		 *      +---- all private documents and thumbnails
		 * </pre>
		 */
		V1,
		/**
		 * Version 2 is composed of one or more metadata records. Each records
		 * are stored in a directory named using record's uuid.
		 * 
		 * <pre>
		 * Root 
		 * |
		 * + 0..n metadata 
		 *   +--- metadata 
		 *   |      +--- metadata.xml (ISO19139)
		 *   |      +--- (optional) metadata.profil.xml (ISO19139profil) Require a
		 * schema/convert/toiso19139.xsl to map to ISO. 
		 *   +--- info.xml 
		 *   +--- applschema ISO 19110 record 
		 *   +--- public 
		 *   |      +---- all public documents and thumbnails 
		 *   +--- private 
		 *          +---- all private documents and thumbnails
		 * </pre>
		 */
		V2
	}
	
	public static List<String> doImportIndexGroup(Element params, ServiceContext context, File mefFile, String stylePath) throws Exception {
		return Importer.doImport(params, context, mefFile, stylePath, true);
	}

	// --------------------------------------------------------------------------
	
	public static List<String> doImport(Element params, ServiceContext context,
			File mefFile, String stylePath) throws Exception {
		return Importer.doImport(params, context, mefFile, stylePath);
	}

	// --------------------------------------------------------------------------

	public static String doExport(ServiceContext context, String uuid,
			String format, boolean skipUUID, boolean resolveXlink, boolean removeXlinkAttribute) throws Exception {
		return MEFExporter.doExport(context, uuid, Format.parse(format),
				skipUUID, resolveXlink, removeXlinkAttribute);
	}

	// --------------------------------------------------------------------------

	public static String doMEF2Export(ServiceContext context,
			Set<String> uuids, String format, boolean skipUUID, String stylePath, boolean resolveXlink, boolean removeXlinkAttribute)
			throws Exception {
		return MEF2Exporter.doExport(context, uuids, Format.parse(format),
				skipUUID, stylePath, resolveXlink, removeXlinkAttribute);
	}

	// --------------------------------------------------------------------------

	public static void visit(File mefFile, IVisitor visitor, IMEFVisitor v)
			throws Exception {
		visitor.visit(mefFile, v);
	}

	/**
	 * Return MEF file version according to ZIP file content.
	 * 
	 * @param mefFile
	 *            mefFile to check version
	 * @return v1
	 */
	public static Version getMEFVersion(File mefFile) {

		try {
			ZipInputStream zis = new ZipInputStream(
					new FileInputStream(mefFile));
			ZipEntry entry;

			try {
				while ((entry = zis.getNextEntry()) != null) {
					String fullName = entry.getName();
					if (fullName.equals("metadata.xml") || fullName.equals("info.xml"))
						return Version.V1;
					zis.closeEntry();
				}
			} finally {
				zis.close();
			}
			return Version.V2;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	};

	/**
	 * Get metadata record.
	 * 
	 * @param dbms
	 * @param uuid
	 * @return
	 */
	static Element retrieveMetadata(ServiceContext context, Dbms dbms, String uuid, boolean resolveXlink, boolean removeXlinkAttribute)
			throws Exception {
		List list = dbms.select("SELECT * FROM Metadata WHERE uuid=?", uuid).getChildren();


		if (list.size() == 0)
			throw new MetadataNotFoundEx("uuid=" + uuid);

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getDataManager();

		Element record = (Element) list.get(0);
		String id = record.getChildText("id");
        record.removeChildren("data");
        boolean forEditing = false;
        boolean withEditorValidationErrors = false;
        Element metadata = dm.getMetadata(context, id, forEditing, withEditorValidationErrors, !removeXlinkAttribute);
        metadata.removeChild("info", Edit.NAMESPACE);
        Element mdEl = new Element("data").setText(Xml.getString(metadata));
        record.addContent(mdEl);

        return record;
	}

	/**
	 * Add an entry to ZIP file
	 * 
	 * @param zos
	 * @param name
	 * @throws IOException
	 */
	static void createDir(ZipOutputStream zos, String name) throws IOException {
		ZipEntry entry = new ZipEntry(name);
		zos.putNextEntry(entry);
		zos.closeEntry();
	}

	/**
	 * Add file to ZIP file
	 * 
	 * @param zos
	 * @param name
	 * @param is
	 * @throws IOException
	 */
	static void addFile(ZipOutputStream zos, String name, InputStream is)
			throws IOException {
		ZipEntry entry = new ZipEntry(name);
		zos.putNextEntry(entry);
		BinaryFile.copy(is, zos, true, false);
		zos.closeEntry();
	}

	/**
	 * Save public directory (thumbnails or other uploaded documents).
	 * 
	 * @param zos
	 * @param dir
	 * @param uuid
	 *            Metadata uuid
	 * @throws IOException
	 */
	static void savePublic(ZipOutputStream zos, String dir, String uuid)
			throws IOException {
		File[] files = new File(dir).listFiles(filter);

		if (files != null)
			for (File file : files)
				addFile(zos, (uuid != null ? uuid : "") + FS + DIR_PUBLIC
						+ file.getName(), new FileInputStream(file));
	}

	/**
	 * Save private directory (thumbnails or other uploaded documents).
	 * 
	 * @param zos
	 * @param dir
	 * @param uuid
	 *            Metadata uuid
	 * @throws IOException
	 */
	static void savePrivate(ZipOutputStream zos, String dir, String uuid)
			throws IOException {
		File[] files = new File(dir).listFiles(filter);

		if (files != null)
			for (File file : files)
				addFile(zos, (uuid != null ? uuid : "") + FS + DIR_PRIVATE
						+ file.getName(), new FileInputStream(file));
	}

	/**
	 * Build an info file.
	 * 
	 * @param context
	 * @param md
	 * @param format
	 * @param pubDir
	 * @param priDir
	 * @param skipUUID
	 * @return
	 * @throws Exception
	 */
	static String buildInfoFile(ServiceContext context, Element md,
			Format format, String pubDir, String priDir, boolean skipUUID)
			throws Exception {
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		Element info = new Element("info");
		info.setAttribute("version", VERSION);

		info.addContent(buildInfoGeneral(md, format, skipUUID, context));
		info.addContent(buildInfoCategories(dbms, md));
		info.addContent(buildInfoPrivileges(context, md));

		info.addContent(buildInfoFiles("public", pubDir));
		info.addContent(buildInfoFiles("private", priDir));

		return Xml.getString(new Document(info));
	}

	/**
	 * Build general section of info file.
	 * 
	 * 
	 * @param md
	 * @param format
	 * @param skipUUID
	 *            If true, do not add uuid, site identifier and site name.
	 * @param context
	 * @return
	 */
	static Element buildInfoGeneral(Element md, Format format,
			boolean skipUUID, ServiceContext context) {
		String id = md.getChildText("id");
		String uuid = md.getChildText("uuid");
		String schema = md.getChildText("schemaid");
		String isTemplate = md.getChildText("istemplate").equals("y") ? "true"
				: "false";
		String createDate = md.getChildText("createdate");
		String changeDate = md.getChildText("changedate");
		String siteId = md.getChildText("source");
		String rating = md.getChildText("rating");
		String popularity = md.getChildText("popularity");

		Element general = new Element("general").addContent(
				new Element("createDate").setText(createDate)).addContent(
				new Element("changeDate").setText(changeDate)).addContent(
				new Element("schema").setText(schema)).addContent(
				new Element("isTemplate").setText(isTemplate)).addContent(
				new Element("localId").setText(id)).addContent(
				new Element("format").setText(format.toString())).addContent(
				new Element("rating").setText(rating)).addContent(
				new Element("popularity").setText(popularity));

		if (!skipUUID) {
			GeonetContext gc = (GeonetContext) context
					.getHandlerContext(Geonet.CONTEXT_NAME);

			general.addContent(new Element("uuid").setText(uuid));
			general.addContent(new Element("siteId").setText(siteId));
			general.addContent(new Element("siteName")
					.setText(gc.getSiteName()));
		}

		return general;
	}

	/**
	 * Build category section of info file.
	 * 
	 * @param dbms
	 * @param md
	 * @return
	 * @throws SQLException
	 */
	static Element buildInfoCategories(Dbms dbms, Element md)
			throws SQLException {
		Element categ = new Element("categories");

		String id = md.getChildText("id");
		String query = "SELECT name FROM MetadataCateg, Categories "
				+ "WHERE categoryId = id AND metadataId = ?";

		List list = dbms.select(query, new Integer(id)).getChildren();

		for (int i = 0; i < list.size(); i++) {
			Element record = (Element) list.get(i);
			String name = record.getChildText("name");

			Element cat = new Element("category");
			cat.setAttribute("name", name);

			categ.addContent(cat);
		}

		return categ;
	}

	/**
	 * Build priviliges section of info file.
	 * 
	 * @param context
	 * @param md
	 * @return
	 * @throws Exception
	 */
	static Element buildInfoPrivileges(ServiceContext context, Element md)
			throws Exception {
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		String id = md.getChildText("id");
		int iId = new Integer(id);
		String query = "SELECT Groups.id as grpid, Groups.name as grpName, Operations.name as operName "
				+ "FROM   OperationAllowed, Groups, Operations "
				+ "WHERE  groupId = Groups.id "
				+ "  AND  operationId = Operations.id "
				+ "  AND  metadataId = ?";

		String grpOwnerQuery = "SELECT groupOwner FROM Metadata WHERE id = ?";
		// Only one groupOwner per metadata
		Element grpOwnerRs = dbms.select(grpOwnerQuery, iId).getChild("record");
		// Get group Owner ID
		String grpOwnerId = grpOwnerRs.getChildText("groupowner");
		String grpOwnerName = "";

		HashMap<String, ArrayList<String>> hmPriv = new HashMap<String, ArrayList<String>>();

		// --- retrieve accessible groups

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		AccessManager am = gc.getAccessManager();

		Set<String> userGroups = am.getUserGroups(dbms, context
				.getUserSession(), context.getIpAddress(), false);

		// --- scan query result to collect info

		List list = dbms.select(query, iId).getChildren();

		for (int i = 0; i < list.size(); i++) {
			Element record = (Element) list.get(i);
			String grpId = record.getChildText("grpid");
			String grpName = record.getChildText("grpname");
			String operName = record.getChildText("opername");

			if (!userGroups.contains(grpId))
				continue;

			if (grpOwnerId != null && grpOwnerId.equals(grpId))
				grpOwnerName = grpName;

			ArrayList<String> al = hmPriv.get(grpName);

			if (al == null) {
				al = new ArrayList<String>();
				hmPriv.put(grpName, al);
			}

			al.add(operName);
		}

		// --- generate elements

		Element privil = new Element("privileges");

		for (String grpName : hmPriv.keySet()) {
			Element group = new Element("group");
			group.setAttribute("name", grpName);
			// Handle group owner
			if (grpName.equals(grpOwnerName))
				group.setAttribute("groupOwner", Boolean.TRUE.toString());

			privil.addContent(group);

			for (String operName : hmPriv.get(grpName)) {
				Element oper = new Element("operation");
				oper.setAttribute("name", operName);

				group.addContent(oper);
			}
		}

		return privil;
	}

	/**
	 * Build file section of info file.
	 * 
	 * @param name
	 * @param dir
	 * @return
	 */
	static Element buildInfoFiles(String name, String dir) {
		Element root = new Element(name);

		File[] files = new File(dir).listFiles(filter);

		if (files != null)
			for (File file : files) {
				String date = new ISODate(file.lastModified()).toString();

				Element el = new Element("file");
				el.setAttribute("name", file.getName());
				el.setAttribute("changeDate", date);

				root.addContent(el);
			}

		return root;
	}

	/**
	 * File filter to exclude .svn files.
	 */
	private static FileFilter filter = new FileFilter() {
		public boolean accept(File pathname) {
			if (pathname.getName().equals(".svn"))
				return false;

			return true;
		}
	};

	static String getChangeDate(List<Element> files, String fileName)
			throws Exception {
		for (Element f : files) {
			Element file = (Element) f;
			String name = file.getAttributeValue("name");
			String date = file.getAttributeValue("changeDate");

			if (name.equals(fileName))
				return date;
		}

		throw new Exception("File not found in info.xml : " + fileName);
	}

}

// =============================================================================

