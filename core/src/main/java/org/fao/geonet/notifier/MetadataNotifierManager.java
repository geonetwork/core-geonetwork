//=============================================================================
//===   Copyright (C) 2001-2010 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.notifier;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.MetadataNotificationRepository;
import org.fao.geonet.repository.MetadataNotifierRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


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
                    setMetadataNotified(metadata.getId(), metadata.getUuid(), notifier, MetadataNotificationAction.UPDATE);
                }

                final Map<Metadata, MetadataNotification> unregisteredMetadataToPublish = getUnnotifiedMetadata(notifier.getId(), MetadataNotificationAction.PUBLISH);
                for (Map.Entry<Metadata, MetadataNotification> entry : unregisteredMetadataToPublish.entrySet()) {
                    Metadata metadata = entry.getKey();

                    // Update/insert notification
                    client.webPublish(notifier, metadata.getData(), metadata.getUuid());

                    // mark metadata as notified for current notifier service
                    setMetadataNotified(metadata.getId(), metadata.getUuid(), notifier, MetadataNotificationAction.PUBLISH);
                }

                final Map<Metadata, MetadataNotification> unregisteredMetadataToUnpublish = getUnnotifiedMetadata(notifier.getId(), MetadataNotificationAction.UNPUBLISH);
                for (Map.Entry<Metadata, MetadataNotification> entry : unregisteredMetadataToUnpublish.entrySet()) {
                    Metadata metadata = entry.getKey();

                    // Update/insert notification
                    client.webUnpublish(notifier, metadata.getData(), metadata.getUuid());

                    // mark metadata as notified for current notifier service
                    setMetadataNotified(metadata.getId(), metadata.getUuid(), notifier, MetadataNotificationAction.UNPUBLISH);
                }

                Map<Metadata, MetadataNotification> unregisteredMetadataToDelete = getUnnotifiedMetadata(notifier.getId(), MetadataNotificationAction.DELETE);

                // process metadata
                for (Map.Entry<Metadata, MetadataNotification> entry : unregisteredMetadataToDelete.entrySet()) {
                    Metadata metadata = entry.getKey();

                    String uuid = metadata.getUuid();

                    // Delete notification
                    client.webDelete(notifier, uuid);

                    // mark metadata as notified for current notifier service
                    setMetadataNotified(metadata.getId(), metadata.getUuid(), notifier, MetadataNotificationAction.DELETE);
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
     * @param metadataElement Metadata content
     * @param uuid            Metadata uuid identifier
     * @param context         GeoNetwork context
     * @throws MetadataNotifierException
     */
    public void updateMetadata(Element metadataElement, String id, String uuid, ServiceContext context) {
        final ConfigurableApplicationContext applicationContext = context.getApplicationContext();
        ScheduledThreadPoolExecutor timer = applicationContext.getBean("timerThreadPool", ScheduledThreadPoolExecutor.class);

        timer.schedule(new UpdateTask(metadataElement, id, uuid), 10, TimeUnit.SECONDS);
    }

    /**
     * Deletes a metadata record.
     *
     * @param uuid
     * @param context GeoNetwork context
     * @throws MetadataNotifierException
     */
    public void deleteMetadata(String id, String uuid, ServiceContext context) {
        final ConfigurableApplicationContext applicationContext = context.getApplicationContext();
        ScheduledThreadPoolExecutor timer = applicationContext.getBean("timerThreadPool", ScheduledThreadPoolExecutor.class);
        timer.schedule(new DeleteTask(id, uuid), 10, TimeUnit.SECONDS);
    }

    /**
     * Notification sent when a metadata is published/unpublished.
     *
     * @param metadataElement Metadata content.
     * @param id GeoNetwork identifier for the metadata
     * @param uuid UUID unique identifier of the metadata.
     * @param publish true is the metadata has been published, false if it has been unpublished.
     * @param serviceContext GeoNetwork context.
     */
    public void publishMetadata(final Element metadataElement, final String id, final String uuid, final boolean publish,
                                final ServiceContext serviceContext) {
        final ConfigurableApplicationContext applicationContext = serviceContext.getApplicationContext();
        ScheduledThreadPoolExecutor timer = applicationContext.getBean("timerThreadPool", ScheduledThreadPoolExecutor.class);
        timer.schedule(new UpdateTask(metadataElement, id, uuid, publish), 10, TimeUnit.SECONDS);


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

    /**
     * Retrieves the unnotified metadata to update/insert for a notifier service
     *
     * @param notifierId
     * @return
     * @throws Exception
     */
    private Map<Metadata, MetadataNotification> getUnnotifiedMetadata(int notifierId,
                                                                      MetadataNotificationAction... actions) {
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata start");
        }
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        MetadataNotificationRepository metadataNotificationRepository = applicationContext.getBean(MetadataNotificationRepository.class);
        MetadataRepository metadataRepository = applicationContext.getBean(MetadataRepository.class);

        List<MetadataNotification> unNotified = metadataNotificationRepository.findAllNotNotifiedForNotifier(notifierId, actions);

        Map<Integer, MetadataNotification> idToNotification = new HashMap<>();
        for (MetadataNotification metadataNotification : unNotified) {
            idToNotification.put(metadataNotification.getId().getMetadataId(), metadataNotification);

        }

        final Iterable<Metadata> allMetadata = metadataRepository.findAll(idToNotification.keySet());
        Map<Metadata, MetadataNotification> notificationMap = new HashMap<>();

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
     * @param action Indicates if the notification was a delete action
     * @throws Exception
     */
    private void setMetadataNotified(final int metadataId, final String uuid, final MetadataNotifier notifier,
                                     final MetadataNotificationAction action) {

        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        MetadataNotificationRepository metadataNotificationRepository = applicationContext.getBean(MetadataNotificationRepository.class);

        final MetadataNotificationId notificationId = new MetadataNotificationId().
                setMetadataId(metadataId).
                setNotifierId(notifier.getId());
        MetadataNotification notification = metadataNotificationRepository.findOne(notificationId);
        if (notification == null) {
            notification = new MetadataNotification();
            notification.setId(notificationId);
        }
        notification.setMetadataUuid(uuid);
        notification.setNotified(true);
        notification.setAction(action);

        metadataNotificationRepository.save(notification);


        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "setMetadataNotified finished for metadata with id " + metadataId + " and notifier with id "
                    + notifier.getId());
        }
    }

    /**
     * Marks a metadata record as notified for a notifier service.
     *
     * @param metadataId Metadata identifier
     * @throws Exception
     */
    private void setMetadataNotifiedError(final int metadataId, final String uuid, final MetadataNotifier notifier,
                                          final MetadataNotificationAction action, final String error) {
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "setMetadataNotifiedError");
        }
        try {
            final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
            MetadataNotificationRepository metadataNotificationRepository = applicationContext.getBean(MetadataNotificationRepository
                    .class);

            MetadataNotificationId id = new MetadataNotificationId().setMetadataId(metadataId).setNotifierId(notifier.getId());
            MetadataNotification errorNotification = metadataNotificationRepository.findOne(id);
            if (errorNotification == null) {
                // Not existing notification
                errorNotification = new MetadataNotification();
                errorNotification.setId(id);
                errorNotification.setNotified(true);
                errorNotification.setMetadataUuid(uuid);
                errorNotification.setAction(action);

                metadataNotificationRepository.save(errorNotification);
            } else {
                // notification already exists. Update it.
                metadataNotificationRepository.update(id, new Updater<MetadataNotification>() {
                    @Override
                    public void apply(@Nonnull MetadataNotification entity) {
                        entity.setErrorMessage(error);
                        entity.setAction(action);
                    }
                });
            }

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "setMetadataNotifiedError finished for metadata with id " + metadataId + "and notitifer " +
                        "with id " + notifier.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
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
        private final int metadataId;
        private final Element metadataElement;
        private final String uuid;
        private final Boolean publish;

        UpdateTask(Element metadataElement, String metadataId, String uuid) {
            this(metadataElement, metadataId, uuid, null);
        }

        public UpdateTask(Element metadataElement, String metadataId, String uuid, Boolean publish) {
            this.metadataId = Integer.parseInt(metadataId);
            this.uuid = uuid;
            this.metadataElement = metadataElement;
            this.publish = publish;

        }

        public void run() {
            try {
                String metadataString = Xml.getString(metadataElement);
                if (Log.isDebugEnabled("MetadataNotifierManager")) {
                    Log.debug("MetadataNotifierManager", "updateMetadata before (uuid): " + uuid);
                }

                final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
                MetadataNotifierClient client = applicationContext.getBean(MetadataNotifierClient.class);
                final MetadataNotificationAction action;
                if (publish == null) {
                    action = MetadataNotificationAction.UPDATE;
                } else if (publish) {
                    action = MetadataNotificationAction.PUBLISH;
                } else {
                    action = MetadataNotificationAction.UNPUBLISH;
                }

                for (MetadataNotifier service : loadNotifiers()) {

                    // Catch individual errors
                    try {
                        switch (action) {
                            case UPDATE:
                                client.webUpdate(service, metadataString, uuid);
                                break;
                            case PUBLISH:
                                client.webPublish(service, metadataString, uuid);
                                break;
                            case UNPUBLISH:
                                client.webUnpublish(service, metadataString, uuid);
                                break;
                            default:
                                break;
                        }

                        if (Log.isDebugEnabled("MetadataNotifierManager")) {
                            Log.debug("MetadataNotifierManager", "updateMetadata: " + action + " (uuid): " + uuid);
                        }

                        // mark metadata as notified for current notifier service
                        setMetadataNotified(metadataId, uuid, service, action);

                    } catch (Exception ex) {
                        Log.error("MetadataNotifierManager", "updateMetadata " + action + " ERROR (uuid): " + uuid + "notifier url "
                                + service.getUrl() + " " + ex.getMessage());
                        ex.printStackTrace();

                        // mark metadata as not notified for current notifier service
                        try {
                            setMetadataNotifiedError(metadataId, uuid, service, action, ex.getMessage());
                        } catch (Exception ex2) {
                            Log.error("MetadataNotifierManager", "updateMetadata " + action + " ERROR (uuid): " + uuid + "notifier url " +
                                    service.getUrl() + " " + ex2.getMessage());
                        }
                    }

                }
            } catch (Exception e) {
                Log.error("MetadataNotifierManager", "updateTask ERROR (uuid): " + uuid + ", (id): " + metadataId
                        + " " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    class DeleteTask implements Runnable {
        private final int metadataId;
        private final String uuid;

        DeleteTask(String metadataId, String uuid) {
            this.metadataId = Integer.parseInt(metadataId);
            this.uuid = uuid;
        }

        public void run() {
            try {
                final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
                MetadataNotifierClient client = applicationContext.getBean(MetadataNotifierClient.class);
                final MetadataNotificationAction action = MetadataNotificationAction.DELETE;

                for (MetadataNotifier service : loadNotifiers()) {
                    int notifierId = service.getId();
                    String notifierUrl = service.getUrl();

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
                        setMetadataNotified(metadataId, uuid, service, action);
                    } catch (Exception ex) {
                        System.out.println("deleteMetadata ERROR:" + ex.getMessage());

                        Log.error("MetadataNotifierManager", "deleteMetadata " + action + " ERROR (uuid): " + uuid + " " + ex.getMessage());
                        ex.printStackTrace();


                        // mark metadata as not notified for current notifier service
                        try {
                            setMetadataNotifiedError(metadataId, uuid, service, action, ex.getMessage());
                        } catch (Exception ex2) {
                            Log.error("MetadataNotifierManager", "updateMetadata " + action + " ERROR (uuid): " + uuid + "notifier url " +
                                    notifierUrl + " " + ex2.getMessage());
                        }
                    }

                }

            } catch (Exception e) {
                Log.error("MetadataNotifierManager", "deleteTask ERROR (uuid): " + uuid + ", (id): " + metadataId
                        + " " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
