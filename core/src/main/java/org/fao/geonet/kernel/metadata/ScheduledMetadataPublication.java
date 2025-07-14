//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.metadata;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.StatusValueType;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.utils.Log;
import org.locationtech.jts.util.Assert;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Task checking on a regular basis the list of records
 * to be published.
 */
public class ScheduledMetadataPublication extends QuartzJobBean {
    @Autowired
    protected ServiceManager serviceManager;

    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private MetadataStatusRepository metadataStatusRepository;

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MetadataPublicationService metadataPublicationService;

    public ScheduledMetadataPublication() {
    }

    /**
     * Checks the list of records scheduled to be published.
     *
     */
    @Override
    protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
        applicationContext = ApplicationContextHolder.get();
        ServiceContext serviceContext = createServiceContext();
        List<StatusValueType> types = new ArrayList<>();
        types.add(StatusValueType.task);

        // Get current date without time part
        ISODate now = new ISODate(new ISODate().getDateAsString() + "T00:00:00.000Z");

        List<MetadataStatus> metadataStatusList = metadataStatusRepository.findMetadataStatusForScheduledPublication(now);
        metadataStatusList.forEach(status -> {
            int metadataId = status.getMetadataId();

            try {
                // Publish the metadata
                AbstractMetadata metadata = metadataUtils.findOne(metadataId);
                if (metadata != null) {
                    metadataPublicationService.shareMetadataWithReservedGroup(serviceContext, metadata, true, "default");
                } else {
                    Log.warning(Geonet.GEONETWORK, "Can not schedule publish the metadata with ID: " + metadataId + " because it does not exist.");
                }

                // Mark the status as closed
                status.setCloseDate(new ISODate());
                metadataStatusRepository.save(status);
            } catch (Exception e) {
                Log.error(Geonet.GEONETWORK, "Error publishing metadata with ID with scheduled publication: " + metadataId, e);
            }
        });
    }

    private ServiceContext createServiceContext() {
        ServiceContext serviceContext = serviceManager.createServiceContext("scheduledpublication", applicationContext);
        serviceContext.setLanguage("eng");
        loginAsAdmin(serviceContext);
        serviceContext.setAsThreadLocal();

        return serviceContext;
    }

    private void loginAsAdmin(ServiceContext serviceContext) {
        final User adminUser = userRepository.findAll(
            UserSpecs.hasProfile(Profile.Administrator),
            PageRequest.of(0, 1)).getContent().get(0);
        Assert.isTrue(adminUser != null, "The system does not have an admin user");
        UserSession session = new UserSession();
        session.loginAs(adminUser);
        serviceContext.setUserSession(session);
    }
}
