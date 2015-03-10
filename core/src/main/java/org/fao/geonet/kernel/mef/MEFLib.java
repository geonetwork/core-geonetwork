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

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.OperationRepository;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nonnull;

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

    // --------------------------------------------------------------------------
	
	public static List<String> doImport(Element params, ServiceContext context, Path mefFile, Path stylePath) throws Exception {
		return Importer.doImport(params, context, mefFile, stylePath);
	}

	// --------------------------------------------------------------------------

	public static Path doExport(ServiceContext context, String uuid,
			String format, boolean skipUUID, boolean resolveXlink, boolean removeXlinkAttribute) throws Exception {
		return MEFExporter.doExport(context, uuid, Format.parse(format),
				skipUUID, resolveXlink, removeXlinkAttribute);
	}

	// --------------------------------------------------------------------------

	public static Path doMEF2Export(ServiceContext context,
			Set<String> uuids, String format, boolean skipUUID, Path stylePath, boolean resolveXlink, boolean removeXlinkAttribute)
			throws Exception {
		return MEF2Exporter.doExport(context, uuids, Format.parse(format),
				skipUUID, stylePath, resolveXlink, removeXlinkAttribute);
	}

	// --------------------------------------------------------------------------

	public static void visit(Path mefFile, IVisitor visitor, IMEFVisitor v)
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
	public static Version getMEFVersion(Path mefFile) {
        try (FileSystem fileSystem = ZipUtil.openZipFs(mefFile)) {
            final Path metadataXmlFile = fileSystem.getPath("metadata.xml");
            final Path infoXmlFile = fileSystem.getPath("info.xml");
            if (Files.exists(metadataXmlFile) || Files.exists(infoXmlFile)) {
                return Version.V1;
            } else {

                return Version.V2;
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * Get metadata record.
	 * 
	 * @param uuid
	 * @return A pair composed of the domain object metadata
	 *  AND the record to be exported (includes Xlink resolution
	 *  and filters depending on user session).
	 */
	static Pair<Metadata, String> retrieveMetadata(ServiceContext context, String uuid, boolean resolveXlink, boolean removeXlinkAttribute)
			throws Exception {

        final Metadata metadata = context.getBean(MetadataRepository.class).findOneByUuid(uuid);

		if (metadata == null) {
			throw new MetadataNotFoundEx("uuid=" + uuid);
        }


		// Retrieve the metadata document
		// using data manager in order to
		// apply all filters (like XLinks,
		// withheld)
        DataManager dm = context.getBean(DataManager.class);
		String id = ""+metadata.getId();
        boolean forEditing = false;
        boolean withEditorValidationErrors = false;
        Element metadataForExportXml = dm.getMetadata(context, id, forEditing, withEditorValidationErrors, !removeXlinkAttribute);
		metadataForExportXml.removeChild("info", Edit.NAMESPACE);
		String metadataForExportAsString = Xml.getString(metadataForExportXml);

		// Prepend xml declaration if needed.
		if (!metadataForExportAsString.startsWith("<?xml")) {
			metadataForExportAsString =
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					metadataForExportAsString;
		}

        return Pair.read(metadata, metadataForExportAsString);
	}

	/**
	 * Add file to ZIP file
	 * 
	 * @param zos
	 * @param name
	 * @param string
	 * @throws IOException
	 */
	static void addFile(ZipOutputStream zos, String name, @Nonnull String string) throws IOException {
        addFile(zos, name, new ByteArrayInputStream(string.getBytes("UTF-8")));
    }
	static void addFile(ZipOutputStream zos, String name, @Nonnull InputStream in)
			throws IOException {
	       ZipEntry entry = null;
	        try {
	            entry = new ZipEntry(name);
	            zos.putNextEntry(entry);
	            BinaryFile.copy(in, zos);
	        } finally {
	            try {
	                if(zos != null) {
	                    zos.closeEntry();
	                }
	            } finally {
	                IOUtils.closeQuietly(in);
	            }
	        }
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
	static String buildInfoFile(ServiceContext context, Metadata md,
			Format format, Path pubDir, Path priDir, boolean skipUUID)
			throws Exception {
		Element info = new Element("info");
		info.setAttribute("version", VERSION);

		info.addContent(buildInfoGeneral(md, format, skipUUID, context));
		info.addContent(buildInfoCategories(md));
		info.addContent(buildInfoPrivileges(context, md));

		info.addContent(buildInfoFiles("public", pubDir.toString()));
		info.addContent(buildInfoFiles("private", priDir.toString()));

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
	static Element buildInfoGeneral(Metadata md, Format format,
			boolean skipUUID, ServiceContext context) {
		String id = String.valueOf(md.getId());
		String uuid = md.getUuid();
		String schema = md.getDataInfo().getSchemaId();
		String isTemplate = md.getDataInfo().getType().codeString;
		String createDate = md.getDataInfo().getCreateDate().getDateAndTime();
		String changeDate = md.getDataInfo().getChangeDate().getDateAndTime();
		String siteId = md.getSourceInfo().getSourceId();
		String rating = "" + md.getDataInfo().getRating();
		String popularity = "" + md.getDataInfo().getPopularity();

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
					.setText(gc.getBean(SettingManager.class).getSiteName()));
		}

		return general;
	}

	/**
	 * Build category section of info file.
	 * 
	 * @param md
	 * @return
	 * @throws SQLException
	 */
	static Element buildInfoCategories(Metadata md)
			throws SQLException {
		Element categ = new Element("categories");


        for (MetadataCategory category : md.getCategories()) {
            String name = category.getName();

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
	static Element buildInfoPrivileges(ServiceContext context, Metadata md)
			throws Exception {

		int iId = md.getId();

		OperationAllowedRepository allowedRepository = context.getBean(OperationAllowedRepository.class);
		GroupRepository groupRepository = context.getBean(GroupRepository.class);
		OperationRepository operationRepository = context.getBean(OperationRepository.class);

		allowedRepository.findAllById_MetadataId(iId);

		// Get group Owner ID
		Integer grpOwnerId = md.getSourceInfo().getGroupOwner();
		String grpOwnerName = "";

		HashMap<String, ArrayList<String>> hmPriv = new HashMap<String, ArrayList<String>>();

		// --- retrieve accessible groups

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		AccessManager am = gc.getBean(AccessManager.class);

		Set<Integer> userGroups = am.getUserGroups(context.getUserSession(), context.getIpAddress(), false);

		// --- scan query result to collect info

        OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
        List<OperationAllowed> opsAllowed = operationAllowedRepository.findAllById_MetadataId(iId);

        for (OperationAllowed operationAllowed : opsAllowed) {
            int grpId = operationAllowed.getId().getGroupId();
            Group group = groupRepository.findOne(grpId);
            String grpName = group.getName();

            if (!userGroups.contains(grpId)) {
                continue;
            }

            Operation operation = operationRepository.findOne(operationAllowed.getId().getOperationId());
            String operName = operation.getName();

            if (grpOwnerId != null && grpOwnerId == grpId) {
                grpOwnerName = grpName;
            }

            ArrayList<String> al = hmPriv.get(grpName);

            if (al == null) {
                al = new ArrayList<String>();
                hmPriv.put(grpName, al);
            }

            al.add(operName);
        }

		// --- generate elements

		Element privil = new Element("privileges");

		for (Map.Entry<String, ArrayList<String>> entry : hmPriv.entrySet()) {
		    String grpName = entry.getKey();
			Element group = new Element("group");
			group.setAttribute("name", grpName);
			// Handle group owner
			if (grpName.equals(grpOwnerName))
				group.setAttribute("groupOwner", Boolean.TRUE.toString());

			privil.addContent(group);

			for (String operName : entry.getValue()) {
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
				String date = new ISODate(file.lastModified(), false).toString();

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
			Element file = f;
			String name = file.getAttributeValue("name");
			String date = file.getAttributeValue("changeDate");

			if (name.equals(fileName))
				return date;
		}

		throw new Exception("File not found in info.xml : " + fileName);
	}

}

// =============================================================================

