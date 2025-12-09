/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.harvest.harvester.localfilesystem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.csw.Aligner;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fao.geonet.kernel.HarvestValidationEnum.NOVALIDATION;


/**
 * @author Jesse on 11/6/2014.
 */
class LocalFsHarvesterFileVisitor extends SimpleFileVisitor<Path> {

    private final LocalFilesystemParams params;
    private final DataManager dataMan;
    private final LocalFilesystemHarvester harvester;
    private final HarvestResult result = new HarvestResult();
    private final IMetadataUtils repo;
    private final ServiceContext context;
    private final AtomicBoolean cancelMonitor;
    private final BaseAligner aligner;
    private final CategoryMapper localCateg;
    private final GroupMapper localGroups;
    private final Set<Integer> listOfRecords = Sets.newHashSet();
    private final Set<Integer> listOfRecordsToIndex = Sets.newHashSet();
    private boolean transformIt = false;
    private Path thisXslt;
    private long startTime;

    public LocalFsHarvesterFileVisitor(AtomicBoolean cancelMonitor, ServiceContext context, LocalFilesystemParams params, LocalFilesystemHarvester harvester) throws Exception {
        this.aligner = new LocalFileSytemAligner(cancelMonitor, params);
        this.cancelMonitor = cancelMonitor;
        this.context = context;
        if (!params.getImportXslt().equals("none")) {
            thisXslt = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class)
                .getXsltConversion(params.getImportXslt());
            transformIt = true;
        }
        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        this.params = params;
        this.dataMan = context.getBean(DataManager.class);
        this.harvester = harvester;
        this.repo = context.getBean(IMetadataUtils.class);
        this.startTime = System.currentTimeMillis();

        harvester.getLogger().debug(String.format("Start visiting files at %s.", this.startTime));
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (cancelMonitor.get()) {
            return FileVisitResult.TERMINATE;
        }

        if (file == null || file.getFileName() == null) {
            return FileVisitResult.CONTINUE;
        }

        boolean isMef = MEFLib.isValidArchiveExtensionForMEF(file.getFileName().toString());
        boolean isXml = file.getFileName().toString().endsWith(".xml");
        boolean isJson = file.getFileName().toString().endsWith(".json");

        if (!isMef && !isXml && !isJson) {
            return FileVisitResult.CONTINUE;
        }

        try {
            result.totalMetadata++;

            if (harvester.getLogger().isDebugEnabled() && result.totalMetadata % 1000 == 0) {
                long elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
                harvester.getLogger().debug("{} records inserted in {} s ({} records/s).", new Object[] {
                    result.totalMetadata,
                    elapsedTime,
                    result.totalMetadata / elapsedTime});
            }

            if(isMef) {
                processMef(file);
            } else if(isJson) {
                processJson(file);
            } else {
                processXml(file);
            }
        } catch (Exception e) {
            harvester.getLogger().error("An error occurred while harvesting file {}. Error is: {}.",
                file.toAbsolutePath().normalize(), e.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }


    // Reads a JSON file, transform it to XML and use the same workflow as for XML files
    // inspired by:
    // https://github.com/geonetwork/core-geonetwork/blob/c57f5de06e5e456af1ee55178eca437235b1d499/harvesters/src/main/java/org/fao/geonet/kernel/harvest/harvester/simpleUrl/Harvester.java#L239
    private void processJson(Path file) throws Exception {
        Path filePath = file.toAbsolutePath().normalize();

        ObjectMapper objectMapper = new ObjectMapper();
        Element recordAsElement;
        try {
            harvester.getLogger().debug("reading file: {}", filePath);
            String uuid = com.google.common.io.Files.getNameWithoutExtension(file.getFileName().toString());
            String recordAsJson = objectMapper.readTree(filePath.toFile()).toString();
            JSONObject sanitizedJson = sanitize(new JSONObject(recordAsJson));
            String recordAsXml = XML.toString(sanitizedJson,"record")
                .replace("<@", "<")
                .replace("</@", "</")
                .replaceAll("(:)(?![^<>]*<)", "_"); // this removes colon from property names
            recordAsXml = Xml.stripNonValidXMLCharacters(recordAsXml);
            recordAsElement = Xml.loadString(recordAsXml, false);
            recordAsElement.addContent(new Element("uuid").setText(uuid));
        } catch (JsonProcessingException e) {
            harvester.getLogger().error("Error processing JSON from file {}, ignoring", filePath);
            harvester.getLogger().error("full stack", e);
            result.badFormat++;
            return;
        } catch (JDOMException e) {
            harvester.getLogger().error("Error transforming JSON into XML from file {}, ignoring", filePath);
            harvester.getLogger().error("full stack", e);
            result.badFormat++;
            return;
        } catch (Exception e) {
            harvester.getLogger().error("Error retrieving JSON from file {}, ignoring", filePath);
            harvester.getLogger().error("full stack", e);
            result.unretrievable++;
            return;
        }

        processXmlData(file, recordAsElement);
    }

    /**
     * Replace whitespace in keys to underscore (mutates object)
     */
    public static JSONObject sanitize(JSONObject json) throws JSONException {
        JSONArray names = json.names();
        if (names != null) {
            for (int i = 0; i < names.length(); i++) {
                String key = names.getString(i);
                if (key.contains(" ")) {
                    String oldKey = key;
                    key = key.replace(" ", "_");
                    json.put(key, json.get(oldKey));
                    json.remove(oldKey);
                }
                Object value = json.opt(key);
                if (value instanceof JSONObject) {
                    sanitize((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    sanitize((JSONArray) value);
                }
            }
        }
        return json;
    }

    public static JSONArray sanitize(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONObject) {
                sanitize((JSONObject) value);
            } else if (value instanceof JSONArray) {
                sanitize((JSONArray) value);
            }
        }
        return array;
    }

    private void processXml(Path file) throws Exception {
        Path filePath = file.toAbsolutePath().normalize();

        Element xml;
        try {
            harvester.getLogger().debug(String.format("reading file: %s", filePath));
            xml = Xml.loadFile(file);
        } catch (JDOMException e) {
            harvester.getLogger().error("Error loading XML from file {}, ignoring", filePath);
            harvester.getLogger().error("full stack", e);
            result.badFormat++;
            return;
        } catch (Exception e) {
            harvester.getLogger().error("Error retrieving XML from file {}, ignoring", filePath);
            harvester.getLogger().error("full stack", e);
            result.unretrievable++;
            return;
        }

        processXmlData(file, xml);
    }

    private void processXmlData(Path file, Element rawXml) throws Exception {
        Path filePath = file.toAbsolutePath().normalize();

        Element xml = rawXml;
        if (transformIt) {
            try {
                xml = Xml.transform(xml, thisXslt);
            } catch (Exception e) {
                harvester.getLogger().error("Cannot transform XML from file {}, ignoring. Error was: {}", filePath, e.getMessage());
                result.badFormat++;
                return;
            }
        }

        String schema = null;
        try {
            schema = dataMan.autodetectSchema(xml, null);
        } catch (Exception e) {
            result.unknownSchema++;
            return;
        }

        try {
            Integer groupIdVal = null;
            if (StringUtils.isNotEmpty(params.getOwnerIdGroup())) {
                groupIdVal = Integer.parseInt(params.getOwnerIdGroup());
            }

            params.getValidate().validate(dataMan, context, xml, groupIdVal);
        } catch (Exception e) {
            harvester.getLogger().error("Cannot validate XML from file {}, ignoring. Error was: {}", filePath, e.getMessage());
            result.doesNotValidate++;
            return;
        }

        String uuid = getUuidFromFile(xml, filePath, schema);
        if (StringUtils.isEmpty(uuid)) {
            result.badFormat++;
            return;
        }

        Aligner.applyBatchEdits(uuid, xml, schema, params.getBatchEdits(), context, null);

        String id = dataMan.getMetadataId(uuid);
        if (id == null) {
            String createDate = getCreateDate(file, xml, schema, uuid);

            id = addMetadata(xml, schema, uuid, createDate);
        } else {
            final AbstractMetadata metadata = repo.findOne(id);
            if (!params.getUuid().equals(metadata.getHarvestInfo().getUuid())) {
                // Metadata exists and belongs to another source (local node or other harvester)
                switch (params.getOverrideUuid()) {
                    case OVERRIDE:
                        updateMetadata(file, filePath, xml, schema, id, metadata, true);
                        break;
                    case RANDOM:
                        harvester.getLogger().debug("Generating random uuid for remote record with uuid " + metadata.getUuid());
                        String createDate = getCreateDate(file, xml, schema, uuid);
                        String newUuid = UUID.randomUUID().toString();
                        id = addMetadata(xml, schema, newUuid, createDate);

                        break;
                    case SKIP:
                        harvester.getLogger().debug("Skipping record with uuid " + metadata.getUuid());
                        result.uuidSkipped++;
                        result.unchangedMetadata++;

                        break;
                    default:
                        // Do nothing
                        break;
                }
            } else {
                //record exists and belongs to this harvester
                updateMetadata(file, filePath, xml, schema, id, metadata, false);
            }

        }
        listOfRecords.add(Integer.valueOf(id));
    }

    private String getCreateDate(Path file, Element xml, String schema, String uuid) throws IOException {
        // For new record change date will be the time of metadata xml date change or the date when
        // the record was harvested (if can't be obtained the metadata xml date change)
        String createDate;
        // or the last modified date of the file
        if (params.checkFileLastModifiedForUpdate) {
            createDate = new ISODate(Files.getLastModifiedTime(file).toMillis(), false).getDateAndTime();
        } else {
            try {
                createDate = dataMan.extractDateModified(schema, xml);
            } catch (Exception ex) {
                harvester.getLogger().error("LocalFilesystemHarvester - addMetadata - can't get metadata modified date for metadata uuid= {} " +
                    "using current date for modified date", uuid);
                createDate = new ISODate().toString();
            }
        }
        return createDate;
    }

    private void updateMetadata(Path file, Path filePath, Element xml, String schema, String id,
                                AbstractMetadata metadata, boolean force)
        throws Exception {
        // Check last modified date of the file with the record change date
        // to check if an update is required
        if (params.checkFileLastModifiedForUpdate) {
            Date fileDate = new Date(Files.getLastModifiedTime(file).toMillis());

            ISODate modified = new ISODate();
            if (metadata.getDataInfo() != null) {
                modified = metadata.getDataInfo().getChangeDate();
            }

            Date recordDate = modified.toDate();

            String changeDate = new ISODate(fileDate.getTime(), false).getDateAndTime();

            harvester.getLogger().debug(" File date is: {} / record date is: {}", filePath, modified);

            if (DateUtils.truncate(recordDate, Calendar.SECOND)
                .before(DateUtils.truncate(fileDate, Calendar.SECOND))) {
                harvester.getLogger().debug(String.format("  Db record is older than file. Updating record with id: %s", id));
                updateMedata(xml, id, changeDate, force);
            } else {
                harvester.getLogger().debug("  Db record is not older than last modified date of file. No need for update.");
                result.unchangedMetadata++;
            }
        } else {
            harvester.getLogger().debug("  updating existing metadata, id is: " + id);

            String changeDate;

            try {
                changeDate = dataMan.extractDateModified(schema, xml);
            } catch (Exception ex) {
                harvester.getLogger().error("LocalFilesystemHarvester - updateMetadata - can't get metadata modified date for " +
                    "metadata id= {}, using current date for modified date", id);
                changeDate = new ISODate().toString();
            }

            updateMedata(xml, id, changeDate, force);
        }
    }

    private void processMef(Path file) {
        Path filePath = file.toAbsolutePath().normalize();

        harvester.getLogger().debug(String.format("reading file: %s", filePath));
        try {
            String xsl = params.getImportXslt();
            MEFLib.Version version = MEFLib.getMEFVersion(file);
            String fileType = version == MEFLib.Version.V1 ? "mef" : "mef2";
            String style = (xsl == null || xsl.equals("none")) ? "_none_" : xsl;
            MetadataType isTemplate = MetadataType.lookup(params.recordType);

            MEFLib.UuidAction uuidAction;
            switch (params.getOverrideUuid()) {
                case SKIP:
                    uuidAction = MEFLib.UuidAction.NOTHING;
                    break;
                case RANDOM:
                    uuidAction = MEFLib.UuidAction.GENERATEUUID;
                    break;
                case OVERRIDE:
                default:
                    uuidAction = MEFLib.UuidAction.OVERWRITE;
            }


            List<String> ids = MEFLib.doImport(
                fileType,
                uuidAction,
                style,
                params.getUuid(),
                isTemplate,
                Iterables.toArray(params.getCategories(), String.class),
                params.getOwnerIdGroup(),
                params.getValidate() != NOVALIDATION,
                false, context, file);
            for (String id : ids) {
                harvester.getLogger().debug(String.format("Metadata imported from MEF: %s", id));
                context.getBean(MetadataRepository.class).update(Integer.valueOf(id), new Updater<Metadata>() {
                    @Override
                    public void apply(@Nonnull final Metadata metadata) {
                        metadata.getHarvestInfo().setHarvested(true);
                        metadata.getHarvestInfo().setUuid(params.getUuid());
                        metadata.getSourceInfo().setOwner(aligner.getOwner());
                    }
                });
                aligner.addPrivileges(id, params.getPrivileges(), localGroups, context);
                listOfRecordsToIndex.add(Integer.valueOf(id));
                listOfRecords.add(Integer.valueOf(id));
                result.addedMetadata++;
            }
        } catch (Exception e) {
            harvester.getLogger().error("Error retrieving MEF from file {}, ignoring", filePath);
            harvester.getLogger().error("Error: ",  e);
            result.unretrievable++;
        }
    }

    private String getUuidFromFile(Element xml, Path filePath, String schema) {
        String uuid = null;
        try {
            uuid = dataMan.extractUUID(schema, xml);
        } catch (Exception e) {
            harvester.getLogger().debug("Failed to extract metadata UUID for file {}" +
                " using XSL extract-uuid. The record is probably " +
                "a subtemplate. Will check uuid attribute on root element.", filePath);

            // Extract UUID from uuid attribute in subtemplates
            String uuidAttribute = xml.getAttributeValue("uuid");
            if (uuidAttribute != null) {
                harvester.getLogger().debug("Found uuid attribute {} for file {}.", uuidAttribute, filePath);
                uuid = uuidAttribute;
            } else {
                // Assigning a new UUID
                uuid = UUID.randomUUID().toString();
                harvester.getLogger().debug("No UUID found, the record will be assigned a random uuid {} for file {}.", uuid, filePath);
            }
        }
        return uuid;
    }

    private String addMetadata(Element xml, String schema, String uuid, String createDate) throws Exception {
        harvester.getLogger().debug("adding new metadata");
        String id = harvester.addMetadata(xml, uuid, schema, localGroups, localCateg, createDate, aligner, false);
        listOfRecordsToIndex.add(Integer.valueOf(id));
        result.addedMetadata++;
        return id;
    }

    private void updateMedata(Element xml, String id, String changeDate, boolean force) throws Exception {
        harvester.updateMetadata(xml, id, localGroups, localCateg, changeDate, aligner, force);
        listOfRecordsToIndex.add(Integer.valueOf(id));
        result.updatedMetadata++;
    }

    public HarvestResult getResult() {
        return result;
    }

    public Set<Integer> getListOfRecords() {
        return listOfRecords;
    }

    public Set<Integer> getListOfRecordsToIndex() {
        return listOfRecordsToIndex;
    }
}
