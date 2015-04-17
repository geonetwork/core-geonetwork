//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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
package org.fao.geonet.notifier;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataNotification;
import org.fao.geonet.domain.MetadataNotificationAction;
import org.fao.geonet.domain.MetadataNotificationId;
import org.fao.geonet.domain.MetadataNotifier;
import org.fao.geonet.repository.MetadataNotificationRepository;
import org.fao.geonet.repository.MetadataNotifierRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;


/**
 * Manages notification of metadata changes.
 *
 * @author jose garcia
 */
public class MetadataNotifierManager {
    /**
     * Updates all unregistered metadata.
     *
     * @throws MetadataNotifierException
     */
    public void updateMetadataBatch() throws MetadataNotifierException {
        if (Log.isDebugEnabled("MetadataNotifierManager"))
            Log.debug("MetadataNotifierManager", "updateMetadata unregistered");

        MetadataNotifierClient client = ApplicationContextHolder.get().getBean(MetadataNotifierClient.class);

        for (MetadataNotifier notifier : loadNotifiers()) {

            try {
                final Map<Metadata, MetadataNotification> unregisteredMetadata = getUnnotifiedMetadata(notifier.getId(),
                        MetadataNotificationAction.UPDATE);

                // process metadata
                for (Map.Entry<Metadata, MetadataNotification> entry : unregisteredMetadata.entrySet()) {
                    Metadata metadata = entry.getKey();

                    // Update/insert notification
                    client.webUpdate(notifier, metadata.getData(), metadata.getUuid());

                    // mark metadata as notified for current notifier service
                    setMetadataNotified(metadata.getId(), notifier, false);
                }

                Map<Metadata, MetadataNotification> unregisteredMetadataToDelete = getUnnotifiedMetadata(notifier.getId(), MetadataNotificationAction.DELETE);

                // process metadata
                for (Map.Entry<Metadata, MetadataNotification> entry : unregisteredMetadataToDelete.entrySet()) {
                    Metadata metadata = entry.getKey();

                    String uuid = metadata.getUuid();

                    // Delete notification
                    client.webDelete(notifier, uuid);

                    // mark metadata as notified for current notifier service
                    setMetadataNotified(metadata.getId(), notifier, true);
                }

            } catch (Exception ex) {
                Log.error("MetadataNotifierManager", "updateMetadataBatch ERROR: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
     }

    /**
     * Updates/inserts a metadata record.
     *
     * @param ISO19139 Metadata content
     * @param uuid     Metadata uuid identifier
     * @param context       GeoNetwork context
     * @throws MetadataNotifierException
     */
    public void updateMetadata(Element ISO19139, String id, String uuid, ServiceContext context) throws MetadataNotifierException {
        final ConfigurableApplicationContext applicationContext = context.getApplicationContext();
        ScheduledThreadPoolExecutor timer = applicationContext.getBean("timerThreadPool", ScheduledThreadPoolExecutor.class);

        timer.schedule(new UpdateTask(ISO19139, id, uuid), 10, TimeUnit.MILLISECONDS);
    }

    /**
     * Deletes a metadata record.
     *
     * @param uuid
     * @param context   GeoNetwork context
     * @throws MetadataNotifierException
     */
    public void deleteMetadata(String id, String uuid, ServiceContext context) throws MetadataNotifierException {
        final ConfigurableApplicationContext applicationContext = context.getApplicationContext();
        ScheduledThreadPoolExecutor timer = applicationContext.getBean("timerThreadPool", ScheduledThreadPoolExecutor.class);
        timer.schedule(new DeleteTask(id, uuid), 10, TimeUnit.MILLISECONDS);
    }


    private List<MetadataNotifier> loadNotifiers() throws MetadataNotifierException {
            try {
                final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
                MetadataNotifierRepository metadataNotifierRepository = applicationContext.getBean(MetadataNotifierRepository.class);

                return metadataNotifierRepository.findAllByEnabled(true);
            } catch (Exception ex) {
                Log.error("MetadataNotifierManager", "loadNotifiers: " + ex.getMessage(), ex);
                throw new MetadataNotifierException(ex.getMessage(), ex);
            }
    }

    @SuppressWarnings("serial")
    static final class MetadataNotifierException extends Exception {
        public MetadataNotifierException(String newMessage) {
            super(newMessage);
        }

        public MetadataNotifierException(String message, Exception ex) {
            super(message, ex);
        }
    }

    class UpdateTask implements Runnable {
        private int metadataId;
        private Element ISO19139;
        private String uuid;

        UpdateTask(Element ISO19139, String metadataId, String uuid) {
            this.metadataId = Integer.valueOf(metadataId);
            this.uuid = uuid;
            this.ISO19139 = ISO19139;
        }

        public void run() {
            try {
                String metadataString = Xml.getString(ISO19139);
                if (Log.isDebugEnabled("MetadataNotifierManager")) {
                    Log.debug("MetadataNotifierManager", "updateMetadata before (uuid): " + uuid);
                }

                final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
                MetadataNotifierClient client = applicationContext.getBean(MetadataNotifierClient.class);

                for (MetadataNotifier service : loadNotifiers()) {

                    // Catch individual errors
                    try {
                        client.webUpdate(service, metadataString, uuid);

                        if (Log.isDebugEnabled("MetadataNotifierManager")) {
                            Log.debug("MetadataNotifierManager", "updateMetadata (uuid): " + uuid);
                        }

                        // mark metadata as notified for current notifier service
                        setMetadataNotified(metadataId, service, false);

                    } catch (Exception ex) {
                        Log.error("MetadataNotifierManager", "updateMetadata ERROR (uuid): " + uuid + "notifier url "
                                                             + service.getUrl()+ " " + ex.getMessage());
                        ex.printStackTrace();

                        // mark metadata as not notified for current notifier service
                        try {
                            setMetadataNotifiedError(metadataId, service, false, ex.getMessage());
                        } catch (Exception ex2) {
                            Log.error("MetadataNotifierManager", "updateMetadata ERROR (uuid): " + uuid + "notifier url " +
                                                                 service.getUrl() + " " + ex2.getMessage());
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class DeleteTask implements Runnable {
        private int metadataId;
        private String uuid;

        DeleteTask(String metadataId, String uuid) {
            this.metadataId = Integer.valueOf(metadataId);
            this.uuid = uuid;
        }

        public void run() {
            try {
                final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
                MetadataNotifierClient client = applicationContext.getBean(MetadataNotifierClient.class);

                for (MetadataNotifier service : loadNotifiers()) {
                    int notifierId = service.getId();
                    String notifierUrl = service.getUrl();
                    String username = service.getUsername();
                    String password = String.valueOf(service.getPassword());

                    try {
                        if (Log.isDebugEnabled("MetadataNotifierManager")) {
                            Log.debug("MetadataNotifierManager", "deleteMetadata before (uuid): " + uuid);
                        }
                        client.webDelete(service, uuid);
                        if (Log.isDebugEnabled("MetadataNotifierManager")) {
                            Log.debug("MetadataNotifierManager", "deleteMetadata (uuid): " + uuid);
                        }

                        System.out.println("deleteMetadata (id): " + metadataId + " notifier id: " + notifierId);

                        // mark metadata as notified for current notifier service
                        setMetadataNotified(metadataId, service, true);
                    } catch (Exception ex) {
                        System.out.println("deleteMetadata ERROR:" + ex.getMessage());

                        Log.error("MetadataNotifierManager", "deleteMetadata ERROR (uuid): " + uuid + " " + ex.getMessage());
                        ex.printStackTrace();


                        // mark metadata as not notified for current notifier service
                        try {
                            setMetadataNotifiedError(metadataId, service, true, ex.getMessage());
                        } catch (Exception ex2) {
                            Log.error("MetadataNotifierManager", "updateMetadata ERROR (uuid): " + uuid + "notifier url " +
                                                                 notifierUrl + " " + ex2.getMessage());
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieves the unnotified metadata to update/insert for a notifier service
     *
     * @param notifierId
     * @return
     * @throws Exception
     */
    private Map<Metadata, MetadataNotification> getUnnotifiedMetadata(int notifierId,
                                                                      MetadataNotificationAction... actions) throws Exception {
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata start");
        }
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        MetadataNotificationRepository metadataNotificationRepository = applicationContext.getBean(MetadataNotificationRepository.class);
        MetadataRepository metadataRepository = applicationContext.getBean(MetadataRepository.class);

        List<MetadataNotification> unNotified = metadataNotificationRepository.findAllNotNotifiedForNotifier(notifierId, actions);

        Map<Integer, MetadataNotification> idToNotification = new HashMap<Integer, MetadataNotification>();
        for (MetadataNotification metadataNotification : unNotified) {
            idToNotification.put(metadataNotification.getId().getMetadataId(), metadataNotification);

        }

        final Iterable<Metadata> allMetadata = metadataRepository.findAll(idToNotification.keySet());
        Map<Metadata, MetadataNotification> notificationMap = new HashMap<Metadata, MetadataNotification>();

        for (Metadata metadata : allMetadata) {
            notificationMap.put(metadata, idToNotification.get(metadata.getId()));
        }

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata returning #" + notificationMap.size() + " results");
        }
        return notificationMap;
    }

    /**
     * Marks a metadata record as notified for a notifier service.
     *
     * @param deleteNotification Indicates if the notification was a delete action
     * @throws Exception
     */
    private void setMetadataNotified(int metadataId, MetadataNotifier notifier,
                                     boolean deleteNotification) throws Exception {

        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        MetadataNotificationRepository metadataNotificationRepository = applicationContext.getBean(MetadataNotificationRepository.class);

        final MetadataNotificationId notificationId = new MetadataNotificationId().
                setMetadataId(metadataId).
                setNotifierId(notifier.getId());
        if (deleteNotification) {
            metadataNotificationRepository.delete(notificationId);
        } else {
            MetadataNotification notification = metadataNotificationRepository.findOne(notificationId);
            notification.setNotified(true);
            notification.setAction(MetadataNotificationAction.UPDATE);
            metadataNotificationRepository.save(notification);
        }

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "setMetadataNotified finished for metadata with id " + metadataId + "and notifier with id "
                                           + notifier.getId());
        }
    }

    /**
     * Marks a metadata record as notified for a notifier service.
     *
     *
     * @param metadataId Metadata identifier
     * @throws Exception
     */
    private void setMetadataNotifiedError(final int metadataId, final MetadataNotifier notifier,
                                          final boolean deleteNotification, final String error) throws Exception {
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "setMetadataNotifiedError");
        }
        try {
            final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
            MetadataNotificationRepository metadataNotificationRepository = applicationContext.getBean(MetadataNotificationRepository
                    .class);

            MetadataNotificationId id = new MetadataNotificationId().setMetadataId(metadataId).setNotifierId(notifier.getId());
            metadataNotificationRepository.update(id, new Updater<MetadataNotification>() {
                @Override
                public void apply(@Nonnull MetadataNotification entity) {
                    entity.setErrorMessage(error);
                    if (deleteNotification == true) {
                        entity.setAction(MetadataNotificationAction.DELETE);
                    } else {
                        entity.setAction(MetadataNotificationAction.UPDATE);

                    }
                }
            });

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "setMetadataNotifiedError finished for metadata with id " + metadataId + "and notitifer " +
                                               "with id " + notifier.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
