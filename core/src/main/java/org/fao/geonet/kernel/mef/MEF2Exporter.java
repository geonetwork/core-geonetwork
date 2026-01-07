//=============================================================================
//===	Copyright (C) 2001-2022 Food and Agriculture Organization of the
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

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.api.records.attachments.StoreUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLib.Format;
import org.fao.geonet.kernel.mef.MEFLib.Version;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRelationRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.xml.XmlEscapers.xmlContentEscaper;
import static org.fao.geonet.Constants.CHARSET;
import static org.fao.geonet.kernel.mef.MEFConstants.*;

class MEF2Exporter {
    /**
     * Create a MEF2 file in ZIP format.
     *
     * @param uuids  List of records to export.
     * @param format {@link Format} to export.
     * @param includeAttachments If true, include attachments according to the export format and permissions.
     *                        If false, no attachments are included.
     * @return MEF2 File
     */
    public static Path doExport(ServiceContext context, Set<String> uuids,
                                Format format, boolean skipUUID, Path stylePath, boolean resolveXlink,
                                boolean removeXlinkAttribute, boolean skipError, boolean addSchemaLocation, boolean includeAttachments) throws Exception {
        return doExport(context, uuids, format, skipUUID, stylePath, resolveXlink, removeXlinkAttribute, skipError, addSchemaLocation, false, includeAttachments);
    }

    /**
     * Create a MEF2 file in ZIP format.
     *
     * @param uuids  List of records to export.
     * @param format {@link Format} to export.
     * @param includeAttachments If true, include attachments according to the export format and permissions.
     *                        If false, no attachments are included.
     * @return MEF2 File
     */
    public static Path doExport(ServiceContext context, Set<String> uuids,
                                Format format, boolean skipUUID, Path stylePath, boolean resolveXlink,
                                boolean removeXlinkAttribute, boolean skipError, boolean addSchemaLocation,
                                boolean approved, boolean includeAttachments) throws Exception {

        Path file = Files.createTempFile("mef-", ".mef");
        EsSearchManager searchManager = context.getBean(EsSearchManager.class);
        String contextLang = context.getLanguage() == null ? Geonet.DEFAULT_LANGUAGE : context.getLanguage();
        try (
            FileSystem zipFs = ZipUtil.createZipFs(file)
        ) {
            StringBuilder csvBuilder = new StringBuilder("\"schema\";\"uuid\";\"id\";\"type\";\"isHarvested\";\"title\";\"abstract\"\n");
            Element html = new Element("html").addContent(new Element("head").addContent(Arrays.asList(
                new Element("title").setText("Export Index"),
                new Element("link").setAttribute("rel", "stylesheet").
                    setAttribute("href", "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css"),
                new Element("style").setText("body {\n"
                    + "  padding-left: 10px;\n"
                    + "}\n"
                    + "p.abstract {\n"
                    + "  font-style: italic;\n"
                    + "}\n"
                    + ".entry {\n"
                    + "  padding: 20px;\n"
                    + "  margin: 20px 0;\n"
                    + "  border: 1px solid #eee;\n"
                    + "  border-left-width: 5px;\n"
                    + "  border-radius: 3px;\n"
                    + "  border-left-color: #1b809e;\n"
                    + "}\n"
                    + ".entry:hover {\n"
                    + "  background-color: #f5f5f5;\n"
                    + "}\n")
            )));
            Element body = new Element("body");
            html.addContent(body);
            for (String uuid : uuids) {
                final String cleanUUID = cleanForCsv(uuid);

                AbstractMetadata md = context.getBean(IMetadataUtils.class).findOneByUuid(uuid);

                //Here we just care if we need the approved version explicitly.
                //IMetadataUtils already filtered draft for non editors.

                if (approved) {
                    md = context.getBean(MetadataRepository.class).findOneByUuid(uuid);
                }
                String id = String.valueOf(md.getId());

                int from = 0;
                SettingInfo si = context.getBean(SettingInfo.class);
                int size = Integer.parseInt(si.getSelectionMaxRecords());

                final SearchResponse result = searchManager.query("+id:" + id, null, from, size);

                String mdSchema = null, mdTitle = null, mdAbstract = null, isHarvested = null;
                MetadataType mdType = null;

                List<Hit> hits = result.hits().hits();
                ObjectMapper objectMapper = new ObjectMapper();
                final Map<String, Object> source = objectMapper.convertValue(hits.get(0).source(), Map.class);
                mdSchema = (String) source.get(Geonet.IndexFieldNames.SCHEMA);
                mdTitle = (String) source.get(Geonet.IndexFieldNames.RESOURCETITLE);
                mdAbstract = (String) source.get(Geonet.IndexFieldNames.RESOURCEABSTRACT);
                isHarvested = (String) source.get(Geonet.IndexFieldNames.IS_HARVESTED);
                mdType = MetadataType.lookup(((String) source.get(Geonet.IndexFieldNames.IS_TEMPLATE)).charAt(0));

                csvBuilder.append('"').
                    append(cleanForCsv(mdSchema)).append("\";\"").
                    append(cleanUUID).append("\";\"").
                    append(cleanForCsv(id)).append("\";\"").
                    append(mdType.toString()).append("\";\"").
                    append(cleanForCsv(isHarvested)).append("\";\"").
                    append(cleanForCsv(mdTitle)).append("\";\"").
                    append(cleanForCsv(mdAbstract)).append("\"\n");

                body.addContent(new Element("div").setAttribute("class", "entry").addContent(Arrays.asList(
                    new Element("h4").setAttribute("class", "title").addContent(
                        new Element("a").setAttribute("href", uuid).setText(cleanXml(mdTitle))),
                    new Element("p").setAttribute("class", "abstract").setText(cleanXml(mdAbstract)),
                    new Element("table").setAttribute("class", "table").addContent(Arrays.asList(
                        new Element("thead").addContent(
                            new Element("tr").addContent(Arrays.asList(
                                new Element("th").setText("Internal ID"),
                                new Element("th").setText("UUID"),
                                new Element("th").setText("Type"),
                                new Element("th").setText("Is harvested?")
                            ))),
                        new Element("tbody").addContent(
                            new Element("tr").addContent(Arrays.asList(
                                new Element("td").setAttribute("class", "id").setText(id),
                                new Element("td").setAttribute("class", "uuid").setText(xmlContentEscaper().escape
                                    (uuid)),
                                new Element("td").setAttribute("class", "type").setText(mdType.toString()),
                                new Element("td").setAttribute("class", "isHarvested").setText(isHarvested)
                            )))
                    ))
                )));

                createMetadataFolder(context, md, zipFs, skipUUID, stylePath,
                    format, resolveXlink, removeXlinkAttribute, addSchemaLocation, includeAttachments);
            }
            Files.write(zipFs.getPath("/index.csv"), csvBuilder.toString().getBytes(Constants.CHARSET));
            Files.write(zipFs.getPath("/index.html"), Xml.getString(html).getBytes(Constants.CHARSET));
        } catch (Exception e) {
            FileUtils.deleteQuietly(file.toFile());
            throw e;
        }
        return file;
    }

    private static String cleanXml(String xmlTextContent) {
        if (xmlTextContent != null) {
            return xmlContentEscaper().escape(xmlTextContent);
        }
        return "-";
    }

    private static String cleanForCsv(String csvColumnText) {
        if (csvColumnText != null) {
            return csvColumnText.replace("\"", "'");
        }
        return "-";
    }

    /**
     * Create a metadata folder according to MEF {@link Version} 2 specification. If current record
     * is based on an ISO profil, the stylesheet /convert/to19139.xsl is used to map to ISO. Both
     * files are included in MEF file. Export relevant information according to format parameter.
     *
     * @param zipFs Zip file to add new record
     * @param includeAttachments If true, include attachments according to the export format and permissions.
     *                        If false, no attachments are included.
     */
    private static void createMetadataFolder(ServiceContext context,
                                             AbstractMetadata metadata, FileSystem zipFs, boolean skipUUID,
                                             Path stylePath, Format format, boolean resolveXlink,
                                             boolean removeXlinkAttribute,
                                             boolean addSchemaLocation, boolean includeAttachments) throws Exception {

        final Path metadataRootDir = zipFs.getPath(metadata.getUuid());
        Files.createDirectories(metadataRootDir);

        Pair<AbstractMetadata, String> recordAndMetadataForExport =
            MEFLib.retrieveMetadata(context, metadata, resolveXlink, removeXlinkAttribute, addSchemaLocation);
        AbstractMetadata record = recordAndMetadataForExport.one();
        String xmlDocumentAsString = recordAndMetadataForExport.two();

        String id = "" + record.getId();
        String isTemp = record.getDataInfo().getType().codeString;

        final Path metadataXmlDir = metadataRootDir.resolve(MD_DIR);
        Files.createDirectories(metadataXmlDir);

        for (Pair<String, String> output : ExportFormat.getFormats(context, record)) {
            Files.write(metadataXmlDir.resolve(output.one()), output.two().getBytes(CHARSET));
        }

        // --- save native metadata
        Files.write(metadataXmlDir.resolve(FILE_METADATA), xmlDocumentAsString.getBytes(CHARSET));


        // --- save Feature Catalog
        String ftUUID = getFeatureCatalogID(context, record.getId());
        if (!ftUUID.equals("")) {
            Pair<AbstractMetadata, String> ftrecordAndMetadata = MEFLib.retrieveMetadata(context, record, resolveXlink, removeXlinkAttribute, addSchemaLocation);
            Path featureMdDir = metadataRootDir.resolve(SCHEMA);
            Files.createDirectories(featureMdDir);
            Files.write(featureMdDir.resolve(FILE_METADATA), ftrecordAndMetadata.two().getBytes(CHARSET));
        }

        final Store store = context.getBean("resourceStore", Store.class);

        // Add the resources if the specified format allows it
        List<MetadataResource> publicResources = List.of();
        List<MetadataResource> privateResources = List.of();
        if (includeAttachments) {
            if (format == Format.PARTIAL || format == Format.FULL) {
                // Include public resources only for PARTIAL and FULL formats so the info file matches the MEF contents.
                publicResources = store.getResources(context, metadata.getUuid(),
                    MetadataResourceVisibility.PUBLIC, null, true);
                StoreUtils.extract(context, metadata.getUuid(), publicResources, metadataRootDir.resolve("public"), true);
            }

            if (format == Format.FULL) {
                try {
                    Lib.resource.checkPrivilege(context, id, ReservedOperation.download);
                    privateResources = store.getResources(context, metadata.getUuid(),
                        MetadataResourceVisibility.PRIVATE, null, true);
                    StoreUtils.extract(context, metadata.getUuid(), privateResources, metadataRootDir.resolve("private"), true);
                } catch (Exception e) {
                    // Current user could not download private data
                }
            }
        }

        // --- save info file
        byte[] binData = MEFLib.buildInfoFile(context, record, format, publicResources,
            privateResources, skipUUID).getBytes(Constants.ENCODING);

        Files.write(metadataRootDir.resolve(FILE_INFO), binData);
    }

    /**
     * Get Feature Catalog ID if exists using relation table.
     *
     * @param metadataId Metadata record id to search for feature catalogue for.
     * @return String Feature catalogue uuid.
     */
    private static String getFeatureCatalogID(ServiceContext context, int metadataId) throws Exception {
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        List<MetadataRelation> relations = context.getBean(MetadataRelationRepository.class).findAllById_MetadataId(metadataId);

        if (relations.isEmpty()) {
            return "";
        }

        // Assume only one feature catalogue is available for a metadata record.
        int ftId = relations.get(0).getId().getRelatedId();

        String ftUuid = dm.getMetadataUuid("" + ftId);

        return ftUuid != null ? ftUuid : "";
    }
}
