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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;
import jeeves.server.context.ServiceContext;

/**
 * Harvester for local filesystem.
 *
 * @author heikki doeleman
 */
public class LocalFilesystemHarvester extends AbstractHarvester<HarvestResult> {

    //FIXME Put on a different file?
    private LocalFilesystemParams params;


    @Override
    protected void storeNodeExtra(AbstractParams params, String path, String siteId, String optionsId) throws SQLException {
        LocalFilesystemParams lp = (LocalFilesystemParams) params;
        super.setParams(lp);

        harvesterSettingsManager.add("id:" + siteId, "icon", lp.icon);
        harvesterSettingsManager.add("id:" + siteId, "recurse", lp.recurse);
        harvesterSettingsManager.add("id:" + siteId, "directory", lp.directoryname);
        harvesterSettingsManager.add("id:" + siteId, "recordType", lp.recordType);
        harvesterSettingsManager.add("id:" + siteId, "nodelete", lp.nodelete);
        harvesterSettingsManager.add("id:" + siteId, "checkFileLastModifiedForUpdate", lp.checkFileLastModifiedForUpdate);
        harvesterSettingsManager.add("id:" + siteId, "beforeScript", lp.beforeScript);
    }

    @Override
    protected String doAdd(Element node) throws BadInputEx, SQLException {
        params = new LocalFilesystemParams(dataMan);
        super.setParams(params);

        //--- retrieve/initialize information
        params.create(node);

        //--- force the creation of a new uuid
        params.setUuid(UUID.randomUUID().toString());

        String id = harvesterSettingsManager.add("harvesting", "node", getType());
        storeNode(params, "id:" + id);

        Source source = new Source(params.getUuid(), params.getName(), params.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.getUuid());

        return id;
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
        final LocalFsHarvesterFileVisitor visitor = new LocalFsHarvesterFileVisitor(cancelMonitor, context, params, this, log);
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
        log.debug(String.format("Scan directory is done. %d files analyzed.",
            result.totalMetadata));
        Set<Integer> idsForHarvestingResult = visitor.getListOfRecords();
        Set<Integer> idsResultHs = Sets.newHashSet(idsForHarvestingResult);

        if (!params.nodelete) {
            log.debug("Starting to delete locally existing metadata " +
                "from the same source if they " +
                " were not in this harvesting result...");
            List<Integer> existingMetadata = context.getBean(MetadataRepository.class).findAllIdsBy((Specification<Metadata>) MetadataSpecs.hasHarvesterUuid(params.getUuid()));
            for (Integer existingId : existingMetadata) {

                if (cancelMonitor.get()) {
                    return this.result;
                }
                if (!idsResultHs.contains(existingId)) {
                    log.debug("  Removing: " + existingId);
                    dataMan.deleteMetadata(context, existingId.toString());
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

    void updateMetadata(Element xml, final String id, GroupMapper localGroups,
                        final CategoryMapper localCateg, String changeDate, BaseAligner aligner) throws Exception {
        updateMetadata(xml, id, localGroups, localCateg, changeDate, aligner, true);
    }

    void updateMetadata(Element xml, final String id, GroupMapper localGroups,
                        final CategoryMapper localCateg, String changeDate, BaseAligner aligner, boolean indexAfterUpdate) throws Exception {
        log.debug("  - Updating metadata with id: " + id);

        //
        // update metadata
        //

        String language = context.getLanguage();

        final AbstractMetadata metadata = dataMan.updateMetadata(context, id, xml, false, false, false, language, changeDate,
            true);

        OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
        repository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(id));
        aligner.addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

        metadata.getCategories().clear();
        aligner.addCategories(metadata, params.getCategories(), localCateg, context, log, null, true);

        dataMan.flush();

        if (indexAfterUpdate == true) {
            dataMan.indexMetadata(id, true, null);
        }
    }


    /**
     * Inserts a metadata into the database. If index param is true, Lucene index is updated after
     * insertion, else the indexation step is skipped
     *
     * @param createDate TODO
     */
    String addMetadata(Element xml, String uuid, String schema, GroupMapper localGroups, final CategoryMapper localCateg,
                       String createDate, BaseAligner aligner) throws Exception {
        return addMetadata(xml, uuid, schema, localGroups, localCateg, createDate, aligner, true);
    }

    String addMetadata(Element xml, String uuid, String schema, GroupMapper localGroups, final CategoryMapper localCateg,
                       String createDate, BaseAligner aligner, boolean index) throws Exception {
        log.debug("  - Adding metadata with remote uuid: " + uuid);


        //
        // insert metadata
        //
        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(uuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(xml.getQualifiedName()).
            setType(MetadataType.lookup(params.recordType)).
            setCreateDate(new ISODate(createDate)).
            setChangeDate(new ISODate(createDate));
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(Integer.parseInt(params.getOwnerId())).
            setGroupOwner(Integer.valueOf(params.getOwnerIdGroup()));
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid());

        aligner.addCategories(metadata, params.getCategories(), localCateg, context, log, null, false);

        metadata = dataMan.insertMetadata(context, metadata, xml, true, false, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        aligner.addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

        dataMan.flush();

        if (index) {
            dataMan.indexMetadata(id, true, null);
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

    @Override
    protected void doInit(Element entry, ServiceContext context) throws BadInputEx {
        params = new LocalFilesystemParams(dataMan);
        super.setParams(params);
        params.create(entry);
    }

    @Override
    protected void doUpdate(String id, Element node) throws BadInputEx, SQLException {
        LocalFilesystemParams copy = params.copy();

        //--- update variables
        copy.update(node);

        String path = "harvesting/id:" + id;

        harvesterSettingsManager.removeChildren(path);

        //--- update database
        storeNode(copy, path);

        //--- we update a copy first because if there is an exception LocalFilesystemParams
        //--- could be half updated and so it could be in an inconsistent state

        Source source = new Source(copy.getUuid(), copy.getName(), copy.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + copy.icon, copy.getUuid());

        params = copy;
        super.setParams(params);

    }

    private void runBeforeScript() throws IOException, InterruptedException {
		if (StringUtils.isEmpty(params.beforeScript)) {
			return;  // Nothing to run
		}
		log.info("Running the before script: " + params.beforeScript);
        List<String> args = new ArrayList<String>(Arrays.asList(params.beforeScript.split(" ")));
        Process process = new ProcessBuilder(args).
				redirectError(ProcessBuilder.Redirect.INHERIT).
				redirectOutput(ProcessBuilder.Redirect.INHERIT).
				start();
		int result = process.waitFor();
		if ( result != 0 ) {
			log.warning("The beforeScript failed with exit value=" + Integer.toString(result));
			throw new RuntimeException("The beforeScript returned an error: " + Integer.toString(result));
		}
	}
}
