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

import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PRIVATE;
import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PUBLIC;
import static org.fao.geonet.kernel.mef.MEFConstants.FS;
import static org.fao.geonet.kernel.mef.MEFConstants.VERSION;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.OperationRepository;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;


/**
 * Utility class for MEF import and export.
 */
public class MEFLib {

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

    public static List<String> doImport(String fileType,
                                        final MEFLib.UuidAction uuidAction,
                                        final String style,
                                        final String source,
                                        final MetadataType isTemplate,
                                        final String[] category,
                                        final String groupId,
                                        final boolean validate,
                                        final boolean assign,
                                        final ServiceContext context,
                                        final Path mefFile) throws Exception {
        return Importer.doImport(fileType, uuidAction, style, source, isTemplate, category, groupId, validate, assign, context, mefFile);
    }

    public static List<String> doImport(Element params, ServiceContext context, Path mefFile, Path stylePath) throws Exception {
        return Importer.doImport(params, context, mefFile, stylePath);
    }

    // --------------------------------------------------------------------------

    public static Path doExport(ServiceContext context, String uuid,
                                String format, boolean skipUUID, boolean resolveXlink,
                                boolean removeXlinkAttribute, boolean addSchemaLocation,
                                boolean approved) throws Exception {
        return MEFExporter.doExport(context, uuid, Format.parse(format),
            skipUUID, resolveXlink, removeXlinkAttribute, addSchemaLocation, approved);
    }

    // --------------------------------------------------------------------------

    public static Path doExport(ServiceContext context, Integer id,
                                String format, boolean skipUUID, boolean resolveXlink,
                                boolean removeXlinkAttribute, boolean addSchemaLocation) throws Exception {
        return MEFExporter.doExport(context, id, Format.parse(format),
            skipUUID, resolveXlink, removeXlinkAttribute, addSchemaLocation);
    }

    // --------------------------------------------------------------------------

    public static Path doMEF2Export(ServiceContext context,
                                    Set<String> uuids, String format, boolean skipUUID, Path stylePath, boolean resolveXlink,
                                    boolean removeXlinkAttribute, boolean skipError, boolean addSchemaLocation,
                                    boolean approved)
        throws Exception {
        return MEF2Exporter.doExport(context, uuids, Format.parse(format),
            skipUUID, stylePath, resolveXlink, removeXlinkAttribute,
            skipError, addSchemaLocation, approved);
    }

    // --------------------------------------------------------------------------

    public static void visit(Path mefFile, IVisitor visitor, IMEFVisitor v)
        throws Exception {
        visitor.visit(mefFile, v);
    }

    // --------------------------------------------------------------------------

    /**
     * Return MEF file version according to ZIP file content.
     *
     * @param mefFile mefFile to check version
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
     * @return A pair composed of the domain object metadata AND the record to be exported (includes
     * Xlink resolution and filters depending on user session).
     */
    static Pair<AbstractMetadata, String> retrieveMetadata(ServiceContext context, AbstractMetadata metadata,
                                                   boolean resolveXlink,
                                                   boolean removeXlinkAttribute,
                                                   boolean addSchemaLocation)
        throws Exception {

        if (metadata == null) {
            throw new MetadataNotFoundEx("");
        }


        return retrieveMetadata(context, removeXlinkAttribute, addSchemaLocation, metadata);
    }

    /**
     * Get metadata record.
     *
     * @return A pair composed of the domain object metadata AND the record to be exported (includes
     * Xlink resolution and filters depending on user session).
     */
    static Pair<AbstractMetadata, String> retrieveMetadata(ServiceContext context, Integer id,
                                                   boolean resolveXlink,
                                                   boolean removeXlinkAttribute,
                                                   boolean addSchemaLocation)
        throws Exception {

        final AbstractMetadata metadata = context.getBean(IMetadataUtils.class).findOne(id);

        if (metadata == null) {
            throw new MetadataNotFoundEx("id=" + id);
        }


        return retrieveMetadata(context, removeXlinkAttribute, addSchemaLocation, metadata);
    }

	private static Pair<AbstractMetadata, String> retrieveMetadata(ServiceContext context, boolean removeXlinkAttribute,
			boolean addSchemaLocation, final AbstractMetadata metadata) throws Exception {
		// Retrieve the metadata document
        // using data manager in order to
        // apply all filters (like XLinks,
        // withheld)
        DataManager dm = context.getBean(DataManager.class);
        String id = "" + metadata.getId();
        boolean forEditing = false;
        boolean withEditorValidationErrors = false;
        Element metadataForExportXml = dm.getMetadata(context, id, forEditing, withEditorValidationErrors, !removeXlinkAttribute);
        metadataForExportXml.removeChild("info", Edit.NAMESPACE);

        if (addSchemaLocation) {
            SchemaManager schemaManager = context.getBean(SchemaManager.class);

            Attribute schemaLocAtt = schemaManager.getSchemaLocation(
                metadata.getDataInfo().getSchemaId(), context);

            if (schemaLocAtt != null) {
                if (metadataForExportXml.getAttribute(
                    schemaLocAtt.getName(),
                    schemaLocAtt.getNamespace()) == null) {
                    metadataForExportXml.setAttribute(schemaLocAtt);
                    // make sure namespace declaration for schemalocation is present -
                    // remove it first (does nothing if not there) then add it
                    metadataForExportXml.removeNamespaceDeclaration(schemaLocAtt.getNamespace());
                    metadataForExportXml.addNamespaceDeclaration(schemaLocAtt.getNamespace());
                }
            }
        }

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
                if (zos != null) {
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
     * @param uuid Metadata uuid
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
     * @param uuid Metadata uuid
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
     */
    static String buildInfoFile(ServiceContext context, AbstractMetadata md,
                                Format format, List<MetadataResource> pubResources,
                                List<MetadataResource> priResources, boolean skipUUID)
        throws Exception {
        Element info = new Element("info");
        info.setAttribute("version", VERSION);

        info.addContent(buildInfoGeneral(md, format, skipUUID, context));
        info.addContent(buildInfoCategories(md));
        info.addContent(buildInfoPrivileges(context, md));

        info.addContent(buildInfoFiles("public", pubResources));
        if (priResources != null) {
            info.addContent(buildInfoFiles("private", priResources));
        } else {
            info.addContent(new Element("private"));
        }

        return Xml.getString(new Document(info));
    }

    /**
     * Build general section of info file.
     *
     * @param skipUUID If true, do not add uuid, site identifier and site name.
     */
    static Element buildInfoGeneral(AbstractMetadata md, Format format,
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
     */
    static Element buildInfoCategories(AbstractMetadata md)
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
     */
    static Element buildInfoPrivileges(ServiceContext context, AbstractMetadata md)
        throws Exception {

        int iId = md.getId();

        OperationAllowedRepository allowedRepository = context.getBean(OperationAllowedRepository.class);
        GroupRepository groupRepository = context.getBean(GroupRepository.class);
        OperationRepository operationRepository = context.getBean(OperationRepository.class);

        allowedRepository.findAllById_MetadataId(iId);

        // Get group Owner ID
        Integer grpOwnerId = md.getSourceInfo().getGroupOwner();
        String grpOwnerName = "";

        Map<String, ArrayList<String>> hmPriv = new HashMap<String, ArrayList<String>>();

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
            Optional<Group> group = groupRepository.findById(grpId);

            if (!group.isPresent()) {
                continue;
            }

            String grpName = group.get().getName();

            if (!userGroups.contains(grpId)) {
                continue;
            }

            Optional<Operation> operation = operationRepository.findById(operationAllowed.getId().getOperationId());
            if (!operation.isPresent()) {
                continue;
            }

            String operName = operation.get().getName();

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
     */
    static Element buildInfoFiles(String name, List<MetadataResource> resources) {
        Element root = new Element(name);


        if (resources != null)
            for (MetadataResource resource : resources) {
                String date = new ISODate(resource.getLastModification().getTime(), false).toString();

                Element el = new Element("file");
                el.setAttribute("name", resource.getFilename());
                el.setAttribute("changeDate", date);

                root.addContent(el);
            }

        return root;
    }

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

    public static void backupRecord(AbstractMetadata metadata, ServiceContext context) {
        Log.trace(Geonet.DATA_MANAGER, "Backing up record " + metadata.getId());
        Path outDir = Lib.resource.getRemovedDir(metadata.getId());
        Path outFile;
        try {
            // When metadata records contains character not supported by filesystem
            // it may be an issue. eg. acri-st.fr/96443
            outFile = outDir.resolve(URLEncoder.encode(metadata.getUuid(), Constants.ENCODING) + ".zip");
        } catch (UnsupportedEncodingException e1) {
            outFile = outDir.resolve(String.format(
                "backup-%s-%s.mef",
                new Date(), metadata.getUuid()));
        }

        Path file = null;
        try {
            file = doExport(context, metadata.getUuid(), "full", false, true, false, false, true);
            Files.createDirectories(outDir);
            try (InputStream is = IO.newInputStream(file);
                 OutputStream os = Files.newOutputStream(outFile)) {
                BinaryFile.copy(is, os);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error performing backup on record '" + metadata.getUuid() + "'. Contact the system administrator if the problem persists: " + e.getMessage(), e);
        } finally {
            if (file != null) {
                IO.deleteFile(file, false, Geonet.MEF);
            }
        }
    }

    public enum UuidAction {
        GENERATEUUID("generateUUID"),
        NOTHING("nothing"),

        /**
         * Update the XML of the metadata record.
         */
        OVERWRITE("overwrite"),

        /**
         * Remove the metadata (and privileges, status, ...)
         * and insert the new one with the same UUID.
         */
        REMOVE_AND_REPLACE("removeAndReplace");
        String name;

        UuidAction(String name) {
            this.name = name;
        }

        public static UuidAction parse(String value) {
            for (UuidAction v : values()) {
                if (v.name.equalsIgnoreCase(value)) {
                    return v;
                }
            }
            return UuidAction.NOTHING;
        }

    }

    public enum Format {
        /**
         * Only metadata record and information
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

            if (format.equalsIgnoreCase("simple"))
                return SIMPLE;
            if (format.equalsIgnoreCase("partial"))
                return PARTIAL;
            if (format.equalsIgnoreCase("full"))
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
     * MEF file is composed of one or more metadata record with extra information managed by
     * GeoNetwork. Metadata is in XML format. An information file (info.xml) is used to transfer
     * general informations, categories, privileges and file references information. A public and
     * private directories allows data transfer (eg. thumbnails, data upload).
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
        V1(Constants.MEF_V1_ACCEPT_TYPE),
        /**
         * Version 2 is composed of one or more metadata records. Each records are stored in a
         * directory named using record's uuid.
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
        V2(Constants.MEF_V2_ACCEPT_TYPE);

        String acceptType;

        Version(String acceptType) {
            this.acceptType = acceptType;
        }

        /**
         * Return version 2 by default.
         */
        static public Version find(String acceptType) {
            for (Version v : values()) {
                if (v.acceptType.equalsIgnoreCase(acceptType)) {
                    return v;
                }
            }
            return V2;
        }

        @Override
        public String toString() {
            return this.acceptType;
        }

        public static class Constants {
            public static final String MEF_V1_ACCEPT_TYPE = "application/x-gn-mef-1-zip";
            public static final String MEF_V2_ACCEPT_TYPE = "application/x-gn-mef-2-zip";
        }
    }

    /**
     * Search for XML, MEF or ZIP file.
     */
    public static class MefOrXmlFileFilter implements DirectoryStream.Filter<Path> {
        @Override
        public boolean accept(Path file) throws IOException {
            String name = file.getFileName().toString();
            return (name.toLowerCase().endsWith(".xml") ||
                name.toLowerCase().endsWith(".mef") ||
                name.toLowerCase().endsWith(".zip"));
        }
    }

    public static boolean isValidArchiveExtensionForMEF(String filename) {
        String lowercasedFileName = filename.toLowerCase();
        return lowercasedFileName.endsWith(".zip") ||
            lowercasedFileName.endsWith(".mef");
    }

    public static boolean isValidExtensionForMEF(String filename) {
        String lowercasedFileName = filename.toLowerCase();
        return lowercasedFileName.endsWith(".xml") ||
            isValidArchiveExtensionForMEF(lowercasedFileName);
    }
}
