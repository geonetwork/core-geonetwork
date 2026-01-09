//=============================================================================
//===	Copyright (C) 2001-2009 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.harvest.harvester.localfilesystem;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Harvester for local filesystem.
 *
 * @author heikki doeleman
 */
public class LocalFilesystemHarvester extends AbstractHarvester<HarvestResult, LocalFilesystemParams> {

    @Override
    protected void storeNodeExtra(LocalFilesystemParams params, String path, String siteId, String optionsId) throws SQLException {
        setParams(params);

        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + siteId, "recurse", params.recurse);
        harvesterSettingsManager.add("id:" + siteId, "directory", params.directoryname);
        harvesterSettingsManager.add("id:" + siteId, "recordType", params.recordType);
        harvesterSettingsManager.add("id:" + siteId, "nodelete", params.nodelete);
        harvesterSettingsManager.add("id:" + siteId, "checkFileLastModifiedForUpdate", params.checkFileLastModifiedForUpdate);
        harvesterSettingsManager.add("id:" + siteId, "beforeScript", params.beforeScript);
    }

    @Override
    protected LocalFilesystemParams createParams() {
        return new LocalFilesystemParams(dataMan);
    }

    /**
     * Aligns new results from filesystem harvesting. Contrary to practice in e.g. CSW Harvesting,
     * files removed from the harvesting source are NOT removed from the database. Also, no checks
     * on modification date are done; the result gets inserted or replaced if the result appears to
     * be in a supported schema.
     *
     * @param root the directory to visit
     */
    private HarvestResult align(Path root) throws Exception {
        log.debug("Start of alignment for : " + params.getName());
        final LocalFsHarvesterFileVisitor visitor = new LocalFsHarvesterFileVisitor(cancelMonitor, context, params, this);
        if (params.recurse) {
            Files.walkFileTree(root, visitor);
        } else {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                for (Path path : paths) {
                    if (path != null && Files.isRegularFile(path)) {
                        visitor.visitFile(path, Files.readAttributes(path, BasicFileAttributes.class));
                    }
                }
            }
        }
        result = visitor.getResult();
        log.debug(String.format("Scan directory is done. %d files analyzed.", result.totalMetadata));
        Set<Integer> idsForHarvestingResult = visitor.getListOfRecords();
        Set<Integer> idsResultHs = Sets.newHashSet(idsForHarvestingResult);

        if (!params.nodelete) {
            log.debug("Starting to delete locally existing metadata " +
                "from the same source if they " +
                " were not in this harvesting result...");
            List<Integer> existingMetadata = context.getBean(MetadataRepository.class).findIdsBy((Specification<Metadata>) MetadataSpecs.hasHarvesterUuid(params.getUuid()));
            for (Integer existingId : existingMetadata) {

                if (cancelMonitor.get()) {
                    return this.result;
                }
                if (!idsResultHs.contains(existingId)) {
                    log.debug("  Removing: " + existingId);
                    metadataManager.deleteMetadata(context, existingId.toString());
                    result.locallyRemoved++;
                }
            }
        }

        log.debug("Starting indexing in batch thread pool...");

        List<Integer> listOfRecordsToIndex = Lists.newArrayList(visitor.getListOfRecordsToIndex());
        log.debug(String.format(
            "Starting indexing in batch thread pool of %d updated records ...",
            listOfRecordsToIndex.size()));
        dataMan.batchIndexInThreadPool(context, listOfRecordsToIndex);

        log.debug("End of alignment for : " + params.getName());
        return result;
    }

    void updateMetadata(Element xml, final String id, GroupMapper localGroups, final CategoryMapper localCateg,
        String changeDate, BaseAligner<LocalFilesystemParams> aligner,
        boolean force) throws Exception {

        log.debug("  - Updating metadata with id: " + id);

        String language = context.getLanguage();

        // Translate metadata
        if (params.isTranslateContent()) {
            String schema = dataMan.getMetadataSchema(id);
            xml = aligner.translateMetadataContent(context, xml, schema);
        }

        final AbstractMetadata metadata = metadataManager.updateMetadata(context, id, xml, false, false, language, changeDate,
            true, IndexingMode.none);

        if (force) {
            //change ownership of metadata to new harvester (Used in OVERRIDE option)
            log.debug(String.format("  - Changing source of metadata id %s to '%s' harvester", id, params.getName()));

            metadata.getHarvestInfo().setUuid(params.getUuid());
            metadata.getSourceInfo().setSourceId(params.getUuid());
            metadataManager.save(metadata);
        }

        aligner.addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadata.getCategories().clear();
        aligner.addCategories(metadata, params.getCategories(), localCateg, context, null, true);

        metadataManager.flush();

        dataMan.indexMetadata(id, true);
    }

    String addMetadata(Element xml, String uuid, String schema, GroupMapper localGroups, final CategoryMapper localCateg,
                       String createDate, BaseAligner aligner, boolean index) throws Exception {

        log.debug("  - Adding metadata with remote uuid: " + uuid);
        Element md = xml;

        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(uuid);

        MetadataType metadataType = MetadataType.lookup(params.recordType);

        String xmlUuid = null;
        if (metadataType == MetadataType.METADATA) {
            xmlUuid = metadataUtils.extractUUID(schema, md);
        } else if (metadataType == MetadataType.SUB_TEMPLATE) {
            xmlUuid = md.getAttributeValue("uuid");
        } else if (metadataType == MetadataType.TEMPLATE_OF_SUB_TEMPLATE) {
            xmlUuid = md.getAttributeValue("uuid");
        }

        if (!uuid.equals(xmlUuid)) {
            md = metadataUtils.setUUID(schema, uuid, md);
        }

        // Translate metadata
        if (params.isTranslateContent()) {
            md = aligner.translateMetadataContent(context, md, schema);
        }

        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(xml.getQualifiedName()).
            setType(metadataType).
            setCreateDate(new ISODate(createDate)).
            setChangeDate(new ISODate(createDate));
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(aligner.getOwner()).
            setGroupOwner(Integer.valueOf(params.getOwnerIdGroup()));
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid());

        aligner.addCategories(metadata, params.getCategories(), localCateg, context, null, false);

        metadata = metadataManager.insertMetadata(context, metadata, md, IndexingMode.none, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        aligner.addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadataManager.flush();

        if (index) {
            dataMan.indexMetadata(id, true);
        }
        return id;
    }

    @Override
    public void doHarvest(Logger l) throws Exception {
        log.debug("LocalFilesystem doHarvest: top directory is " + params.directoryname + ", recurse is " + params.recurse);
        runBeforeScript();
        Path directory = IO.toPath(params.directoryname);
        this.result = align(directory);
    }

    private void runBeforeScript() throws IOException, InterruptedException {
        if (StringUtils.isEmpty(params.beforeScript)) {
			return;
		}

        // Script MUST be limited to well known ones added by a
        // catalog admin in the data directory.
        log.info("Checking script: " + params.beforeScript);
        List<String> args = new ArrayList<String>(Arrays.asList(params.beforeScript.split(" ")));
        if (isScriptAllowed(args)) {
            log.info("Running script: " + params.beforeScript);
            Process process = new ProcessBuilder(args).
                redirectError(ProcessBuilder.Redirect.INHERIT).
                redirectOutput(ProcessBuilder.Redirect.INHERIT).
                start();
            int result = process.waitFor();
            if (result != 0) {
                log.warning("The beforeScript failed with exit value=" + Integer.toString(result));
                throw new RuntimeException("The beforeScript returned an error: " + Integer.toString(result));
            }
        } else {
            throw new RuntimeException(String.format(
                "Script %s is not allowed. Only script in the data directory can be triggered.",
                params.beforeScript));
        }
	}

    public boolean isScriptAllowed(List<String> args) {
        String scriptFile = args.get(0);
        if(scriptFile == null) {
            log.warning("The beforeScript can't be null.");
            return false;
        }
        if(scriptFile.contains("..")) {
            log.warning("The beforeScript can't contains '..'.");
            return false;
        }

        GeonetworkDataDirectory dataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
        Path scriptPath = dataDirectory.getConfigDir().resolve(scriptFile);
        if(!Files.exists(scriptPath)) {
            log.warning("The beforeScript MUST exists.");
            return false;
        } else {
            args.set(0, scriptPath.toString());
        }

        List<String> argsWithSemiColon = args
            .stream()
            .filter(a -> a.contains(";")
                || a.contains("|")
                || a.contains("&")
                || a.contains("`")
                || a.contains("$"))
            .collect(Collectors.toList());
        if (argsWithSemiColon.size() > 0) {
            log.warning("The beforeScript can't contains ';|&`$'. Only one script can be triggered with simple arguments.");
            return false;
        }
        return true;
    }
}
