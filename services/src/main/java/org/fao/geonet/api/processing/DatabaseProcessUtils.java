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

package org.fao.geonet.api.processing;

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.processing.report.MetadataReplacementProcessingReport;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

public class DatabaseProcessUtils {
    /**
     * Process a metadata with a database SQL query.
     * Checks that the result of the query is still XML valid.
     */
    public static Element process(ServiceContext context, String id,
                                  boolean useRegexp,
                                  String search,
                                  String replace,
                                  String flags,
                                  boolean save, boolean index,
                                  boolean updateDateStamp,
                                  MetadataReplacementProcessingReport report) throws Exception {
        AccessManager accessMan = context.getBean(AccessManager.class);
        DataManager dataMan = context.getBean(DataManager.class);
        IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

        report.incrementProcessedRecords();

        // When a record is deleted the UUID is in the selection manager
        // and when retrieving id, return null
        if (id == null) {
            report.incrementNullRecords();
            return null;
        }

        int iId = Integer.valueOf(id);
        AbstractMetadata info = metadataUtils.findOne(id);


        if (info == null) {
            report.addNotFoundMetadataId(iId);
        } else if (!accessMan.canEdit(context, id)) {
            report.addNotEditableMetadataId(iId);
        } else {
            Element wellFormedXml = null;
            try {
                Lib.resource.checkEditPrivilege(context, id);

                String updatedXml =
                    useRegexp
                        ? (StringUtils.isNotEmpty(flags)
                          ? metadataRepository.selectOneWithRegexSearchAndReplaceWithFlags(
                        info.getUuid(), search, replace, flags)
                          : metadataRepository.selectOneWithRegexSearchAndReplace(
                        info.getUuid(), search, replace))
                        : metadataRepository.selectOneWithSearchAndReplace(
                        info.getUuid(), search, replace);

                // Check XML is still well formed.
                wellFormedXml = Xml.loadString(updatedXml, false);

                String beforeProcessXML = info.getData();
                if (beforeProcessXML.equals(updatedXml)) {
                    report.incrementUnchangedRecords();
                } else {
                    // --- save metadata and return status
                    if (save) {
                        boolean validate = false;
                        boolean ufo = true;
                        String language = context.getLanguage();

                        dataMan.updateMetadata(context, id, wellFormedXml, validate, ufo, language, new ISODate().toString(),
                            updateDateStamp, index ? IndexingMode.full : IndexingMode.none);
                        if (index) {
                            dataMan.indexMetadata(id, DirectIndexSubmitter.INSTANCE);
                        }
                    }
                }



                report.addMetadataId(iId);
                // TODO : it could be relevant to list at least
                // if there was any change in the record or not.
                // Using hash on processMd and metadata ?
            } catch (Exception e) {
                report.addMetadataError(info, e);
                context.error("  Processing failed with error " + e.getMessage());
                context.error(e);
            }
            return wellFormedXml;
        }
        return null;
    }
}
