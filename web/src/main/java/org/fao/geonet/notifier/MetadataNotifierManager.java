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

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

import java.util.*;


/**
 *
 * Manages notification of metadata changes.
 *
 * @author jose garcia
 *
 */
public class MetadataNotifierManager {
    private DataManager dataMan;
    private MetadataNotifierClient client;

    private List<Element> notifierServices = new ArrayList<Element>();
    private boolean loadNotifierServices = true;

    public MetadataNotifierManager(DataManager dataMan) throws Exception {
        this.dataMan = dataMan;
        this.client = new MetadataNotifierClient();
    }

    public void setLoadNotifierServices(boolean loadNotifierServices) {
        this.loadNotifierServices = loadNotifierServices;
    }

    /**
     * Updates all unregistered metadata.
     *
     * @throws MetadataNotifierException
     */
    public void updateMetadataBatch(Dbms dbms, GeonetContext gc) throws MetadataNotifierException {
        if(Log.isDebugEnabled("MetadataNotifierManager"))
            Log.debug("MetadataNotifierManager", "updateMetadata unregistered");

        loadNotifiers(dbms);

        for (Element service : notifierServices) {
            String notifierId = service.getChild("id").getText();
            String notifierUrl = service.getChild("url").getText();
            String username = service.getChild("username").getText();
            String password = service.getChild("password").getText();

            try {
                Map<String, Element> unregisteredMetadata =  dataMan.getUnnotifiedMetadata(dbms, notifierId);

                // process metadata
                for (Map.Entry<String, Element> entry : unregisteredMetadata.entrySet()) {
                    String uuid = entry.getKey();
                    Element result = entry.getValue();

                    String id = result.getChildText("id");
                    String metadataString = result.getChildText("data");

                    // Update/insert notification
                    client.webUpdate(notifierUrl, username, password, metadataString, uuid, gc);

                    // mark metadata as notified for current notifier service
                    dataMan.setMetadataNotified(id, uuid, notifierId, false, dbms);

                }

                Map<String, Element> unregisteredMetadataToDelete =  dataMan.getUnnotifiedMetadataToDelete(dbms, notifierId);

                // process metadata
                for (Map.Entry<String, Element> entry : unregisteredMetadataToDelete.entrySet()) {
                    String uuid = entry.getKey();
                    Element result = entry.getValue();

                    String id = result.getChildText("id");

                    // Delete notification
                    client.webDelete(notifierUrl, username, password, uuid, gc);

                    // mark metadata as notified for current notifier service
                    dataMan.setMetadataNotified(id, uuid, notifierId, true, dbms);
                }

            }
            catch (Exception ex) {
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
     * @param gc       GeoNetwork context
     * @throws MetadataNotifierException
     */
    public void updateMetadata(Element ISO19139, String id, String uuid, Dbms dbms, GeonetContext gc) throws MetadataNotifierException {
        Timer t = new Timer();
        t.schedule(new UpdateTask(ISO19139, id, uuid, dbms, gc), 10);
    }

    /**
     * Deletes a metadata record.
     *
     * @param uuid
     * @param gc   GeoNetwork context
     * @throws MetadataNotifierException
     */
    public void deleteMetadata(String id, String uuid, Dbms dbms, GeonetContext gc) throws MetadataNotifierException {
        Timer t = new Timer();
        t.schedule(new DeleteTask(id, uuid, dbms, gc), 10);
    }


    private void loadNotifiers(Dbms dbms) throws MetadataNotifierException {
        if (loadNotifierServices) {
            try {
                notifierServices = dataMan.retrieveNotifierServices(dbms);
                loadNotifierServices = false;
            }
            catch (Exception ex) {
                Log.error("MetadataNotifierManager", "loadNotifiers: " + ex.getMessage());
                ex.printStackTrace();
                throw new MetadataNotifierException(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("serial")
    static final class MetadataNotifierException extends Exception {
        public MetadataNotifierException(String newMessage) {
            super(newMessage);
        }
    }

    class UpdateTask extends TimerTask {
        private GeonetContext gc;
        private Dbms dbms;
        private String id;
        private Element ISO19139;
        private String uuid;

        UpdateTask(Element ISO19139, String id, String uuid, Dbms dbms, GeonetContext gc) {
            this.gc = gc;
            this.id = id;
            this.uuid = uuid;
            this.dbms = dbms;
            this.ISO19139 = ISO19139;
        }

        public void run() {
            try {
                String metadataString = Xml.getString(ISO19139);
                if(Log.isDebugEnabled("MetadataNotifierManager"))
                    Log.debug("MetadataNotifierManager", "updateMetadata before (uuid): " + uuid);

                loadNotifiers(dbms);

                for (Element service : notifierServices) {
                    String notifierId = service.getChild("id").getText();
                    String notifierUrl = service.getChild("url").getText();
                    String username = service.getChild("username").getText();
                    String password = service.getChild("password").getText();

                    // Catch individual errors
                    try {
                        client.webUpdate(notifierUrl, username, password, metadataString, uuid, gc);
                        if(Log.isDebugEnabled("MetadataNotifierManager"))
                            Log.debug("MetadataNotifierManager", "updateMetadata (uuid): " + uuid);

                        // mark metadata as notified for current notifier service
                        dataMan.setMetadataNotified(id, uuid, notifierId, false, dbms);

                    }
                    catch (Exception ex) {
                        Log.error("MetadataNotifierManager", "updateMetadata ERROR (uuid): " + uuid + "notifier url " +
                                notifierUrl + " " + ex.getMessage());
                        ex.printStackTrace();

                        // mark metadata as not notified for current notifier service
                        try {
                            dataMan.setMetadataNotifiedError(id, uuid, notifierId, false, ex.getMessage(), dbms);
                        }
                        catch (Exception ex2) {
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


    class DeleteTask extends TimerTask {
        private GeonetContext gc;
        private Dbms dbms;
        private String id;
        private String uuid;

        DeleteTask(String id, String uuid, Dbms dbms, GeonetContext gc) {
            this.gc = gc;
            this.id = id;
            this.uuid = uuid;
            this.dbms = dbms;
        }

        public void run() {
            try {
                loadNotifiers(dbms);

                for (Element service : notifierServices) {
                    String notifierId = service.getChild("id").getText();
                    String notifierUrl = service.getChild("url").getText();
                    String username = service.getChild("username").getText();
                    String password = service.getChild("password").getText();

                    try {
                        if(Log.isDebugEnabled("MetadataNotifierManager"))
                            Log.debug("MetadataNotifierManager", "deleteMetadata before (uuid): " + uuid);
                        client.webDelete(notifierUrl, username, password, uuid, gc);
                        if(Log.isDebugEnabled("MetadataNotifierManager"))
                            Log.debug("MetadataNotifierManager", "deleteMetadata (uuid): " + uuid);

                        System.out.println("deleteMetadata (id): " + id + " notifier id: " + notifierId);

                        // mark metadata as notified for current notifier service
                        dataMan.setMetadataNotified(id, uuid, notifierId, true, dbms);
                    }
                    catch (Exception ex) {
                        System.out.println("deleteMetadata ERROR:" + ex.getMessage());

                        Log.error("MetadataNotifierManager", "deleteMetadata ERROR (uuid): " + uuid + " " + ex.getMessage());
                        ex.printStackTrace();


                        // mark metadata as not notified for current notifier service
                        try {
                            dataMan.setMetadataNotifiedError(id, uuid, notifierId, true, ex.getMessage(), dbms);
                        }
                        catch (Exception ex2) {
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
}
