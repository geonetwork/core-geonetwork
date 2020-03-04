//=============================================================================
//===	Copyright (C) 2001-2014 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.formatters;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.records.formatters.cache.FormatterCache;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.index.IndexingList;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.Log;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A task which runs every X sec in order to create or update landing pages
 * <p/>
 * See configuration in config-spring-geonetwork.xml for interval.
 * <p/>
 */
public class LandingPageTask extends QuartzJobBean {

    @Autowired
    protected ConfigurableApplicationContext applicationContext;
    @Autowired
    protected ServiceManager serviceManager;
    @Autowired
    FormatterCache formatterCache;

    private String landingPageFormatter;
    private Map<String, String> landingPageFormatterParameters = new HashMap<>();
    private String landingPageLanguage = Geonet.DEFAULT_LANGUAGE;

    public String getLandingPageFormatter() {
        return landingPageFormatter;
    }

    public void setLandingPageFormatter(String landingPageFormatter) {
        if (landingPageFormatter.contains("?")) {
            final String[] strings = landingPageFormatter.split("\\?");
            this.landingPageFormatter = strings[0];
            if (strings.length > 1) {
                for (String param : strings[1].split("&")) {
                    if(param.contains("=")) {
                        final String[] paramAndValue = param.split("=");
                        this.landingPageFormatterParameters.put(paramAndValue[0], paramAndValue[1]);
                    } else {
                        this.landingPageFormatterParameters.put(param, "");
                    }
                }
            }
        } else {
            this.landingPageFormatter = landingPageFormatter;
        }
    }

    public void setLandingPageLanguage(String landingPageLanguage) {
        this.landingPageLanguage = landingPageLanguage;
    }

    private void buildLandingPages(ServiceContext serviceContext) {
        ApplicationContextHolder.set(applicationContext);
        IndexingList list = applicationContext.getBean("landingPageList", IndexingList.class);
        Set<Integer> metadataIdentifiers = list.getIdentifiers();
        if (metadataIdentifiers.size() > 0) {
            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                Log.debug(Geonet.INDEX_ENGINE, "Landing page task / List of records to update: "
                    + metadataIdentifiers.toString() + ".");
            }

            for (Integer metadataIdentifier : metadataIdentifiers) {
                formatterCache.buildLandingPage(metadataIdentifier);
            }
        }
    }

    @Override
    protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
        ServiceContext serviceContext = serviceManager.createServiceContext("landingPage", applicationContext);
        serviceContext.setLanguage(landingPageLanguage);
        serviceContext.setAsThreadLocal();

        if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
            Log.debug(Geonet.INDEX_ENGINE, "Landing page task / Start at: "
                + new Date() + ". Checking if any records need to be updated ...");
        }
        buildLandingPages(serviceContext);
    }
}
