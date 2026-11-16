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

package org.fao.geonet.api.records;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.guiservices.versioning.MetadataAction;
import org.fao.geonet.guiservices.versioning.MetadataActionListSingleton;
import org.fao.geonet.guiservices.versioning.MyLogEntryHandler;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fao.geonet.api.ApiParams.*;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordVersionning")
@ReadWriteController
public class MetadataVersionningApi {

    @Autowired
    LanguageUtils languageUtils;
    @Autowired
    DataManager dataManager;
    @Autowired
    AccessManager accessMan;
    @Autowired
    IMetadataUtils metadataRepository;
    @Autowired
    GeonetworkDataDirectory dataDirectory;
    @Autowired
    SvnManager svnManager;

    private static final Map<String, Comparator<MetadataAction>> LOG_SORT = new HashMap<>();

    static {
        LOG_SORT.put("date", MetadataAction.DATE_COMPARATOR_ASC);
        LOG_SORT.put("user", MetadataAction.USERNAME_COMPARATOR_ASC);
        LOG_SORT.put("ip", MetadataAction.IP_COMPARATOR_ASC);
        LOG_SORT.put("action", MetadataAction.ACTION_COMPARATOR_ASC);
        LOG_SORT.put("subject", MetadataAction.SUBJECT_COMPARATOR_ASC);
        LOG_SORT.put("id", MetadataAction.ID_COMPARATOR_ASC);
        LOG_SORT.put("title", MetadataAction.TITLE_COMPARATOR_ASC);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "(Experimental) Enable version control",
        description = "")
    @RequestMapping(
        value = "/{metadataUuid}/versions",
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public ResponseEntity enableVersionControl(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        dataManager.versionMetadata(ApiUtils.createServiceContext(request),
            String.valueOf(metadata.getId()), metadata.getXmlData(false));

        return new ResponseEntity(HttpStatus.CREATED);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "(Experimental) Enable version control for one or more records",
        description = "")
    @RequestMapping(
        value = "/versions",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public MetadataProcessingReport enableVersionControlForRecords(
        @Parameter(
            description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        HttpServletRequest request,
        @Parameter(hidden = true)
            HttpSession session
    ) throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            for (String uuid : records) {
                if (!metadataRepository.existsMetadataUuid(uuid)) {
                    report.incrementNullRecords();
                } else {
                    for (AbstractMetadata metadata : metadataRepository.findAllByUuid(uuid)) {
                        if (!accessMan.canEdit(
                            ApiUtils.createServiceContext(request), String.valueOf(metadata.getId()))) {
                            report.addNotEditableMetadataId(metadata.getId());
                        } else {
                            dataManager.versionMetadata(ApiUtils.createServiceContext(request),
                                String.valueOf(metadata.getId()), metadata.getXmlData(false));
                            report.incrementProcessedRecords();
                        }
                    }
                }
            }
        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }

        return report;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "(Experimental) Get the version control log",
        description = "Changes recorded in the version control repository (records added, modified or deleted, " +
            "and changes to their owner, privileges, categories or status). Empty when version control is disabled.")
    @GetMapping(value = "/versions/log", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public VersionLog getVersionLog(
        @Parameter(description = "Sort by date, user, ip, action, subject, id or title")
        @RequestParam(defaultValue = "date") String sort,
        @Parameter(description = "ASC or DESC")
        @RequestParam(defaultValue = "DESC") String dir,
        @RequestParam(defaultValue = "0") int start,
        @RequestParam(defaultValue = "30") int limit) throws Exception {
        Comparator<MetadataAction> comparator = LOG_SORT.get(sort);
        if (comparator == null) {
            throw new IllegalArgumentException(String.format("Unknown sort '%s'. Use one of %s.", sort, LOG_SORT.keySet()));
        }
        if ("DESC".equalsIgnoreCase(dir)) {
            comparator = comparator.reversed();
        }

        List<MetadataAction> actions = refreshVersionLog();
        actions.sort(comparator);
        List<VersionLogEntry> entries = actions.stream()
            .skip(Math.max(start, 0))
            .limit(Math.max(limit, 0))
            .map(VersionLogEntry::new)
            .collect(Collectors.toList());
        return new VersionLog(actions.size(), entries);
    }

    /**
     * Reads the changes committed to the repository since the last call. Reading titles is slow, so
     * the changes are kept in memory. Returns a copy that the caller can sort.
     */
    private synchronized List<MetadataAction> refreshVersionLog() throws Exception {
        if (!svnManager.isEnabled()) {
            return new ArrayList<>();
        }
        MetadataActionListSingleton singleton = MetadataActionListSingleton.getInstance();
        List<MetadataAction> actions = singleton.getMetadataActions();

        SVNRepository repository = SVNRepositoryFactory.create(SVNURL.fromFile(dataDirectory.getMetadataRevisionDir().toFile()));
        try {
            long startRevision = actions == null || actions.isEmpty() ? 0L : actions.get(actions.size() - 1).getRevision() + 1;
            if (startRevision <= repository.getLatestRevision()) {
                MyLogEntryHandler logEntryHandler = new MyLogEntryHandler();
                repository.log(new String[]{""}, startRevision, -1, true, true, logEntryHandler);
                List<MetadataAction> newActions = logEntryHandler.getMetadataActionList();
                for (MetadataAction action : newActions) {
                    action.loadTitle(repository);
                }
                if (actions == null) {
                    actions = newActions;
                } else {
                    actions.addAll(newActions);
                }
                singleton.setMetadataActions(actions);
            }
        } finally {
            repository.closeSession();
        }
        return actions == null ? new ArrayList<>() : new ArrayList<>(actions);
    }

    public static class VersionLog {
        public final int totalCount;
        public final List<VersionLogEntry> entries;

        VersionLog(int totalCount, List<VersionLogEntry> entries) {
            this.totalCount = totalCount;
            this.entries = entries;
        }
    }

    public static class VersionLogEntry {
        public final String date;
        public final String user;
        public final String ip;
        public final String action;
        public final String subject;
        public final int id;
        public final String title;

        VersionLogEntry(MetadataAction action) {
            this.date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(action.getDate());
            this.user = action.getUsername();
            this.ip = action.getIp();
            this.action = action.translatedAction();
            this.subject = action.translatedSubject();
            this.id = action.getId();
            this.title = action.getTitle();
        }
    }
}
