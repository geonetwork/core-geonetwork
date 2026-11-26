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
import static org.fao.geonet.kernel.mef.MEFConstants.DIR_STORE;
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

import com.google.common.net.UrlEscapers;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.api.exception.AttachmentsExportLimitExceededException;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
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
                                boolean approved, boolean includeAttachments) throws Exception {
        return MEFExporter.doExport(context, uuid, Format.parse(format),
            skipUUID, resolveXlink, removeXlinkAttribute, addSchemaLocation, approved, includeAttachments);
    }

    // --------------------------------------------------------------------------

    public static Path doExport(ServiceContext context, Integer id,
                                String format, boolean skipUUID, boolean resolveXlink,
                                boolean removeXlinkAttribute, boolean addSchemaLocation, boolean includeAttachments) throws Exception {
        return MEFExporter.doExport(context, id, Format.parse(format),
            skipUUID, resolveXlink, removeXlinkAttribute, addSchemaLocation, includeAttachments);
    }

    // --------------------------------------------------------------------------

    public static Path doMEF2Export(ServiceContext context,
                                    Set<String> uuids, String format, boolean skipUUID, Path stylePath, boolean resolveXlink,
                                    boolean removeXlinkAttribute, boolean skipError, boolean addSchemaLocation,
                                    boolean approved, boolean includeAttachments)
        throws Exception {
        return MEF2Exporter.doExport(context, uuids, Format.parse(format),
            skipUUID, stylePath, resolveXlink, removeXlinkAttribute,
            skipError, addSchemaLocation, approved, includeAttachments);
    }

    // --------------------------------------------------------------------------

    /**
     * Create a MEF {@link Version#V3} archive: the same one-folder-per-record container as
     * {@link #doMEF2Export}, but with each record's attachments written flat under a single
     * {@code store} directory (see {@link MEF3Exporter}) instead of split across
     * {@code public}/{@code private}.
     */
    public static Path doMEF3Export(ServiceContext context,
                                    Set<String> uuids, String format, boolean skipUUID, Path stylePath, boolean resolveXlink,
                                    boolean removeXlinkAttribute, boolean skipError, boolean addSchemaLocation,
                                    boolean approved, boolean includeAttachments)
        throws Exception {
        return MEF3Exporter.doExport(context, uuids, Format.parse(format),
            skipUUID, stylePath, resolveXlink, removeXlinkAttribute,
            skipError, addSchemaLocation, approved, includeAttachments);
    }

    // --------------------------------------------------------------------------

    public static void visit(Path mefFile, IVisitor visitor, IMEFVisitor v)
        throws Exception {
        visitor.visit(mefFile, v);
    }

    // --------------------------------------------------------------------------

    /**
     * Return MEF file version according to ZIP file content.
     * <p>
     * {@link Version#V2} and {@link Version#V3} share the same one-folder-per-record container,
     * distinguished only by their attachment layout (legacy {@code public}/{@code private} split
     * vs flat {@code store}) - telling them apart requires peeking at the first per-record folder
     * found, since that's not visible from the zip root alone.
     *
     * @param mefFile mefFile to check version
     * @return the MEF version the archive is written in
     */
    public static Version getMEFVersion(Path mefFile) {
        try (FileSystem fileSystem = ZipUtil.openZipFs(mefFile)) {
            final Path metadataXmlFile = fileSystem.getPath("metadata.xml");
            final Path infoXmlFile = fileSystem.getPath("info.xml");
            if (Files.exists(metadataXmlFile) || Files.exists(infoXmlFile)) {
                return Version.V1;
            }

            Path root = fileSystem.getRootDirectories().iterator().next();
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                for (Path recordDir : paths) {
                    if (Files.isDirectory(recordDir)) {
                        return Files.isDirectory(recordDir.resolve(DIR_STORE)) ? Version.V3 : Version.V2;
                    }
                }
            }
            return Version.V2;
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
     *
     * @param unifiedStore Whether the caller physically writes attachments into a single flat
     *                     {@code store} directory ({@link MEF3Exporter}) or splits them across
     *                     {@code public}/{@code private} directories (legacy MEF1/MEF2 - see
     *                     {@link MEFExporter}/{@link MEF2Exporter}). Must match the caller's
     *                     actual physical layout: the info.xml file section shape (a single
     *                     unified {@code <store>} element vs. separate {@code <public>}/
     *                     {@code <private>} elements) needs to describe the same layout the ZIP
     *                     itself was written with, or a reader relying on info.xml alone (eg.
     *                     {@link org.fao.geonet.kernel.harvest.harvester.geonet.BaseGeoNetworkAligner})
     *                     would look for files in the wrong place.
     * @param flattenedResourceNames Only meaningful when {@code unifiedStore} is false - the
     *                     name mapping from {@link #flattenResourceNames}, so a flattened
     *                     resource is registered under the same name it was actually written
     *                     under. Ignored (may be null) when {@code unifiedStore} is true.
     */
    static String buildInfoFile(ServiceContext context, AbstractMetadata md,
                                Format format, List<MetadataResource> pubResources,
                                List<MetadataResource> priResources, boolean skipUUID,
                                boolean unifiedStore, Map<String, String> flattenedResourceNames)
        throws Exception {
        Element info = new Element("info");
        info.setAttribute("version", VERSION);

        info.addContent(buildInfoGeneral(md, format, skipUUID, context));
        info.addContent(buildInfoCategories(md));
        info.addContent(buildInfoPrivileges(context, md));

        if (unifiedStore) {
            List<MetadataResource> allResources = new ArrayList<>();
            if (pubResources != null) {
                allResources.addAll(pubResources);
            }
            if (priResources != null) {
                allResources.addAll(priResources);
            }
            info.addContent(buildInfoFiles("store", allResources));
        } else {
            info.addContent(buildInfoFiles("public", pubResources, flattenedResourceNames));
            if (priResources != null) {
                info.addContent(buildInfoFiles("private", priResources, flattenedResourceNames));
            } else {
                info.addContent(new Element("private"));
            }
        }

        return Xml.getString(new Document(info));
    }

    /**
     * Compute a MEF v1/v2-safe flat name for each resource in the given list, replacing every
     * "/" in a nested resource's name with "__" (eg. {@code test/document.pdf} becomes
     * {@code test__document.pdf}), so it can be written directly into a flat public/private
     * directory without creating any real subdirectory there. This matters because GeoNetwork
     * versions that predate subfolder support list that directory non-recursively with no check
     * for a nested directory entry, and fail (eg. {@code .../public/test -> is a directory}) the
     * moment they reach one - flattening avoids ever writing one in the first place. MEF version 3
     * ({@link MEF3Exporter}, the modern, recommended default) is unaffected by any of this and
     * keeps exporting nested resources under their real path.
     * <p>
     * A name that doesn't need flattening (no "/") is still included in the returned map, mapped
     * to itself, so callers can look up every resource's target name uniformly rather than special
     * -casing the identity mapping. Only a genuine collision - two different original names that
     * flatten (or already are) the exact same string, eg. a nested {@code test/document.pdf}
     * flattening to the same name as an existing, unrelated top-level {@code test__document.pdf} -
     * gets a disambiguating numeric suffix inserted before the file extension; expected to be rare
     * in practice.
     *
     * @return a map from each resource's original name to the name it should actually be written
     * under (and referenced as, in both info.xml and any URL inside the record's own metadata.xml
     * that points at it) for this export. Never null; empty if {@code resources} is null or empty.
     */
    static Map<String, String> flattenResourceNames(List<MetadataResource> resources) {
        Map<String, String> flattenedNames = new LinkedHashMap<>();
        if (resources == null) {
            return flattenedNames;
        }
        Set<String> usedNames = new HashSet<>();
        for (MetadataResource resource : resources) {
            String originalName = resource.getFilename();
            String candidate = originalName.replace("/", "__");
            String finalName = candidate;
            int suffix = 1;
            while (!usedNames.add(finalName)) {
                int dot = candidate.lastIndexOf('.');
                finalName = dot > 0
                    ? candidate.substring(0, dot) + "-" + suffix + candidate.substring(dot)
                    : candidate + "-" + suffix;
                suffix++;
            }
            flattenedNames.put(originalName, finalName);
        }
        return flattenedNames;
    }

    /**
     * Rewrite any reference, inside the given serialized record XML, to a resource whose name was
     * changed by {@link #flattenResourceNames} - eg. a distribution link or thumbnail URL pointing
     * at {@code .../attachments/test/document.pdf} gets rewritten to
     * {@code .../attachments/test__document.pdf} - so the record's own online resource links still
     * resolve correctly against the flattened layout a legacy MEF v1/v2 export actually writes to
     * disk. Without this, a record that *links* (not just uploads) a nested attachment would
     * export with a dangling reference in its own metadata.xml.
     * <p>
     * This is a plain string substitution, not schema-aware XPath rewriting: the exact old and new
     * URLs are both fully known here (same base URL/UUID prefix as {@code resource.getUrl()}, only
     * the escaped filename tail changes), so it works correctly regardless of which element or
     * namespace happens to hold the link in any given metadata schema, without needing to parse
     * the XML at all.
     *
     * @param xmlDocumentAsString The serialized record XML to rewrite.
     * @param resources           The resources whose names were (possibly) changed.
     * @param flattenedNames      The name mapping from {@link #flattenResourceNames}.
     * @return The rewritten XML, or the original string unchanged if nothing needed rewriting.
     */
    static String rewriteFlattenedResourceUrls(String xmlDocumentAsString, List<MetadataResource> resources,
                                               Map<String, String> flattenedNames) {
        if (resources == null || flattenedNames == null || flattenedNames.isEmpty()) {
            return xmlDocumentAsString;
        }
        for (MetadataResource resource : resources) {
            String originalName = resource.getFilename();
            String flattenedName = flattenedNames.get(originalName);
            String oldUrl = resource.getUrl();
            if (flattenedName == null || flattenedName.equals(originalName) || oldUrl == null) {
                continue;
            }
            String escapedOldName = UrlEscapers.urlFragmentEscaper().escape(originalName);
            if (!oldUrl.endsWith(escapedOldName)) {
                // Unexpected URL shape (eg. a Store implementation building URLs differently
                // than FilesystemStoreResource's baseUrl + uuid + "/attachments/" + escaped
                // filename convention) - skip rather than risk a wrong substitution.
                continue;
            }
            String escapedFlattenedName = UrlEscapers.urlFragmentEscaper().escape(flattenedName);
            String newUrl = oldUrl.substring(0, oldUrl.length() - escapedOldName.length()) + escapedFlattenedName;
            xmlDocumentAsString = xmlDocumentAsString.replace(oldUrl, newUrl);
        }
        return xmlDocumentAsString;
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
        return buildInfoFiles(name, resources, null);
    }

    /**
     * Build file section of info file.
     *
     * @param flattenedNames Optional name mapping from {@link #flattenResourceNames} - when a
     *                       resource's original name is a key in this map, the corresponding
     *                       value is registered as its {@code name} attribute instead of the
     *                       resource's own {@link MetadataResource#getFilename()}. May be null,
     *                       equivalent to an empty map (every resource keeps its own name).
     */
    static Element buildInfoFiles(String name, List<MetadataResource> resources, Map<String, String> flattenedNames) {
        Element root = new Element(name);


        if (resources != null)
            for (MetadataResource resource : resources) {
                String date = new ISODate(resource.getLastModification().getTime(), false).toString();

                Element el = new Element("file");
                String filename = (flattenedNames != null && flattenedNames.containsKey(resource.getFilename()))
                    ? flattenedNames.get(resource.getFilename())
                    : resource.getFilename();
                el.setAttribute("name", filename);
                el.setAttribute("changeDate", date);
                if (resource.getVisibility() != null) {
                    el.setAttribute("access", resource.getVisibility().toString());
                }
                if (resource.getMimeType() != null && !resource.getMimeType().isEmpty()) {
                    el.setAttribute("mimetype", resource.getMimeType());
                }

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

    /**
     * Whether a file with the given name is registered in the given {@code <file>} element list
     * (from {@link #getFilesElement}). Used by {@link MEFVisitor}/{@link MEF3Visitor} to decide
     * which handler (public/private) to invoke for a resource read from the flat, visibility-less
     * {@code store/} folder introduced with MEF 3.0, since the folder itself no longer implies
     * visibility the way the legacy {@code public/}/{@code private/} folders did.
     */
    static boolean isRegisteredFile(List<Element> files, String fileName) {
        for (Element file : files) {
            if (fileName.equals(file.getAttributeValue("name"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the {@code <file>} elements for a given access level ("public" or "private") from an
     * {@code <info>} element, for use as a {@code changeDate} lookup table (see
     * {@link #getChangeDate}) by {@link MEFVisitor}, {@link MEF2Visitor} and {@link MEF3Visitor}.
     * <p>
     * Supports both the MEF 3.0 unified {@code <store>} format (info.xml version "3.0"), where
     * files are filtered by their own {@code access} attribute, and the pre-3.0
     * {@code <public>}/{@code <private>} format, for archives written before this format existed.
     * This is independent of a record's physical attachment layout: a MEF 3.0-schema info.xml can
     * be paired with either the flat {@code store/} folder (see {@link MEFConstants#DIR_STORE},
     * read by {@link MEFVisitor}/{@link MEF3Visitor}) or the legacy {@code public/}/{@code private/}
     * folders (see {@link MEFConstants#DIR_PUBLIC}/{@link MEFConstants#DIR_PRIVATE}, read by
     * {@link MEFVisitor}/{@link MEF2Visitor}) - this method itself is only about locating the
     * changeDate/mimetype metadata for a given file, not about which physical folder(s) to read.
     * <p>
     * Also used directly by {@link org.fao.geonet.kernel.harvest.harvester.geonet.BaseGeoNetworkAligner},
     * which parses info.xml independently of the visitor classes above.
     */
    public static List<Element> getFilesElement(Element info, String access) {
        Element store = info.getChild("store");
        if (store != null) {
            List<Element> files = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Element> children = store.getChildren("file");
            for (Element file : children) {
                if (access.equals(file.getAttributeValue("access"))) {
                    files.add(file);
                }
            }
            return files;
        }
        Element legacy = info.getChild(access);
        if (legacy != null) {
            @SuppressWarnings("unchecked")
            List<Element> children = legacy.getChildren();
            return children;
        }
        return new ArrayList<>();
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
            file = doExport(context, metadata.getUuid(), "full", false, true, false, false, true, true);
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
        V2(Constants.MEF_V2_ACCEPT_TYPE),
        /**
         * Version 3 uses the same one-folder-per-record container as version 2, but each
         * record's attachments - public and private alike - live in a single flat {@code store}
         * directory instead of being split across {@code public}/{@code private}. Visibility is
         * recorded per file via the {@code access} attribute on info.xml's unified
         * {@code <store>} element (see {@link MEFLib#buildInfoFiles}), not by physical location.
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
         *   +--- store
         *          +---- all public and private documents and thumbnails
         * </pre>
         */
        V3(Constants.MEF_V3_ACCEPT_TYPE);

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
            public static final String MEF_V3_ACCEPT_TYPE = "application/x-gn-mef-3-zip";
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

    /**
     * Checks if the total size of attachments for given metadata records is under the configured size limit.
     *
     * @param metadataUuids The UUIDs of the metadata records.
     * @param approved      Whether to use the approved version of the metadata records.
     * @throws AttachmentsExportLimitExceededException if the total size of attachments exceeds the configured limit.
     */
    public static void checkAttachmentsUnderSizeLimit(Set<String> metadataUuids, boolean approved) throws AttachmentsExportLimitExceededException {
        if (attachmentsExceedExportLimit(metadataUuids, approved)) {
            if (metadataUuids.size() == 1) {
                throw new AttachmentsExportLimitExceededException("Total size of attachments for the selected record exceeds the export limit.")
                    .withMessageKey("exception.attachmentsExportLimitExceededException")
                    .withDescriptionKey("exception.attachmentsExportLimitExceededException.single.description");
            } else {
                throw new AttachmentsExportLimitExceededException("Total size of attachments across selected records exceeds the export limit.")
                    .withMessageKey("exception.attachmentsExportLimitExceededException")
                    .withDescriptionKey("exception.attachmentsExportLimitExceededException.batch.description");
            }
        }
    }

    /**
     * Checks if the total size of attachments for a given metadata record is under the configured size limit.
     *
     * @param metadataUuids The UUIDs of the metadata records.
     * @param approved      Whether to use the approved version of the metadata records.
     * @return `true` if the total size of attachments exceeds the configured limit, `false` otherwise.
     */
    public static boolean attachmentsExceedExportLimit(Set<String> metadataUuids, boolean approved) {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        EsSearchManager searchManager = ApplicationContextHolder.get().getBean(EsSearchManager.class);
        return attachmentsExceedExportLimit(metadataUuids, approved, settingManager, searchManager);
    }

    /**
     * Checks if the total size of resources for a metadata record is within the configured size limit.
     *
     * Retrieves the maximum size limit in bytes from the settings. If no limit is configured, returns
     * {@code true}. Otherwise, retrieves the resources associated with the metadata UUID from the
     * Elasticsearch index, computes their total size, and compares against the limit.
     *
     * @param metadataUuids    The UUIDs of the metadata records to check.
     * @param approved         Whether to use the approved version of the metadata records.
     * @param settingManager  The SettingManager instance for accessing configuration settings.
     * @param searchManager   The EsSearchManager instance for accessing Elasticsearch resources.
     * @return {@code true} if the total size of resources exceeds the configured limit, {@code false} otherwise.
     */
    static boolean attachmentsExceedExportLimit(Set<String> metadataUuids,
                                              boolean approved,
                                              SettingManager settingManager,
                                              EsSearchManager searchManager) {

        Long maxSizeLimit = getMaxAttachmentSizeInBytes(settingManager);
        if (maxSizeLimit == null) {
            return false;
        }

        long totalSize = searchManager.getTotalSizeOfResources(metadataUuids, approved);

        return totalSize > maxSizeLimit;
    }

    /**
     * Retrieves the maximum attachment size limit configured in the system settings.
     *
     * Attempts to read the {@code METADATA_ZIPEXPORT_ATTACHMENTSSIZELIMIT} setting from the SettingManager
     * and converts it from MB to bytes. If the setting value is invalid (non-numeric) or not configured,
     * a warning is logged and null is returned to indicate no limit should be applied.
     *
     * @param settingManager The SettingManager instance used to retrieve configuration settings.
     * @return The maximum attachment size limit in bytes, or {@code null} if no limit is configured
     *         or if the configured value is invalid.
     */
    static Long getMaxAttachmentSizeInBytes(SettingManager settingManager) {
        Long maxSizeLimitInMb;
        try {
            maxSizeLimitInMb = settingManager.getValueAsLong(Settings.METADATA_ZIPEXPORT_ATTACHMENTSSIZELIMIT);
        } catch (NumberFormatException e) {
            Log.warning(Geonet.INDEX_ENGINE,
                "Invalid value for setting \"" + Settings.METADATA_ZIPEXPORT_ATTACHMENTSSIZELIMIT + "\". " +
                    "No resource size limit will be applied.");
            return null;
        }

        if (maxSizeLimitInMb == null || maxSizeLimitInMb < 0) {
            return null;
        }

        return maxSizeLimitInMb * 1024 * 1024;
    }
}
