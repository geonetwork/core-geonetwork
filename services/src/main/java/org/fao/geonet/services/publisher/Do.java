//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.services.publisher;


import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.api.mapservers.GeoFile;
import org.fao.geonet.api.mapservers.GeoServerNode;
import org.fao.geonet.api.mapservers.GeoServerRest;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.domain.MapServer;
import org.fao.geonet.repository.MapServerRepository;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;

/**
 * Service to manage GeoServer dataset publication. Dataset could be <ul> <li>ESRI Shapefile
 * (zipped) POST or external</li> <li>PostGIS table</li> <li>GeoTiff (zip or not) POST or
 * external</li> <li>ECW external</li> </ul>
 *
 * Shapefile must be zipped.
 *
 * In case of ZIP compression, ZIP file base name must be equal to Shapefile or GeoTiff base name.
 *
 * One Datastore, FeatureType, Layer and Style are created for a vector dataset (one to one
 * relation). One CoverageStore, Coverage, Layer are created for a raster dataset (one to one
 * relation).
 *
 * TODO : Support multi file publication
 */
@Deprecated
public class Do implements Service {
    /**
     * Module name
     */
    public static final String MODULE = "geonetwork.GeoServerPublisher";
    private static final String DB = "DB";
    private static final String VECTOR = "vector";
    private static final String RASTER = "raster";
    private static final String SUCCESS = "Success";
    private static final String EXCEPTION = "Exception";
    /**
     * List of current known nodes
     */
    private HashMap<Integer, GeoServerNode> geoserverNodes = new HashMap<Integer, GeoServerNode>();

    /**
     * Error code received when publishing
     */
    private String errorCode = "";

    /**
     * Report return by a read action
     */
    private Element report = null;

    private Element getReport() {
        return report;
    }

    private void setReport(Element report) {
        this.report = report;
    }

    private String getErrorCode() {
        return errorCode;
    }

    private void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Load configuration file and register remote nodes. In order to register new nodes, restart is
     * needed.
     */
    public void init(Path appPath, ServiceConfig params) throws Exception {
        Log.createLogger(MODULE);
    }

    /**
     * Publish a dataset to a remote GeoServer node. Dataset could be a ZIP composed of Shapefile(s)
     * or GeoTiff.
     *
     * updataMetadataRecord, add or delete a online source link.
     */
    public Element exec(Element params, ServiceContext context)
        throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        MapServerRepository repo = context.getBean(MapServerRepository.class);
        SettingManager settingsManager = gc.getBean(SettingManager.class);
        String baseUrl = settingsManager.getSiteURL(context);


        ACTION action = ACTION.valueOf(Util.getParam(params, "action"));
        if (action.equals(ACTION.LIST)) {
            return loadDbConfiguration(context);
        } else if (action.equals(ACTION.ADD_NODE)) {
            MapServer m = new MapServer()
                .setName(Util.getParam(params, "name", ""))
                .setDescription(Util.getParam(params, "description", ""))
                .setConfigurl(Util.getParam(params, "configurl", ""))
                .setWmsurl(Util.getParam(params, "wmsurl", ""))
                .setWfsurl(Util.getParam(params, "wfsurl", ""))
                .setWcsurl(Util.getParam(params, "wcsurl", ""))
                .setStylerurl(Util.getParam(params, "stylerurl", ""))
                .setUsername(Util.getParam(params, "username", ""))
                .setPassword(Util.getParam(params, "password", ""))
                .setNamespace(Util.getParam(params, "namespace", ""))
                .setNamespacePrefix(Util.getParam(params, "namespaceprefix", ""))
                .setPushStyleInWorkspace(Util.getParam(params, "pushstyleinworkspace", false));
            context.getBean(MapServerRepository.class).save(m);
            return new Element(action.toString())
                .setText("ok")
                .setAttribute("id", String.valueOf(m.getId()));
        } else if (action.equals(ACTION.REMOVE_NODE)) {
            MapServer m = repo.findOneById(Util.getParam(params, "id"));
            if (m != null) {
                repo.delete(m);
            }
            return new Element(action.toString()).setText("ok");
        } else if (action.equals(ACTION.UPDATE_NODE)) {
            MapServer m = repo.findOneById(Util.getParam(params, "id"));
            if (m != null) {
                m.setName(Util.getParam(params, "name", ""))
                    .setDescription(Util.getParam(params, "description", ""))
                    .setConfigurl(Util.getParam(params, "configurl", ""))
                    .setWmsurl(Util.getParam(params, "wmsurl", ""))
                    .setWfsurl(Util.getParam(params, "wfsurl", ""))
                    .setWcsurl(Util.getParam(params, "wcsurl", ""))
                    .setStylerurl(Util.getParam(params, "stylerurl", ""))
                    .setNamespace(Util.getParam(params, "namespace", ""))
                    .setNamespacePrefix(Util.getParam(params, "namespaceprefix", ""))
                    .setPushStyleInWorkspace(Util.getParam(params, "pushstyleinworkspace", false));
                repo.save(m);
            }
            return new Element(action.toString()).setText("ok");
        } else if (action.equals(ACTION.UPDATE_NODE_ACCOUNT)) {
            MapServer m = repo.findOneById(Util.getParam(params, "id"));
            if (m != null) {
                m.setUsername(Util.getParam(params, "username", ""))
                    .setPassword(Util.getParam(params, "password", ""));
                repo.save(m);
            }
            return new Element(action.toString()).setText("ok");
        } else if (action.equals(ACTION.CREATE) || action.equals(ACTION.UPDATE)
            || action.equals(ACTION.DELETE) || action.equals(ACTION.GET)) {

            // Check parameters
            String nodeId = Util.getParam(params, "nodeId");
            String metadataId = Util.getParam(params, "metadataId");
            String metadataUuid = Util.getParam(params, "metadataUuid", "");
            // purge \\n from metadataTitle - geoserver prefers layer titles on a single line
            String metadataTitle = Util.getParam(params, "metadataTitle", "").replace("\\n", "");
            // unescape \\n from metadataAbstract so they're properly sent to geoserver
            String metadataAbstract = Util.getParam(params, "metadataAbstract", "").replace("\\n", "\n");
            MapServer m = repo.findOneById(nodeId);
            GeoServerNode g = new GeoServerNode(m);

            final GeonetHttpRequestFactory requestFactory = context.getBean(GeonetHttpRequestFactory.class);
            GeoServerRest gs = new GeoServerRest(requestFactory, g.getUrl(),
                g.getUsername(), g.getUserpassword(),
                g.getNamespacePrefix(), baseUrl, settingsManager.getNodeURL(), m.pushStyleInWorkspace());

            String file = Util.getParam(params, "file");
            String access = Util.getParam(params, "access");

            //jdbc:postgresql://host:port/user:password@database#table
            if (file.startsWith("jdbc:postgresql")) {
                String[] values = file.split("/");

                String[] serverInfo = values[2].split(":");
                String host = serverInfo[0];
                String port = serverInfo[1];

                String[] dbUserInfo = values[3].split("@");

                String[] userInfo = dbUserInfo[0].split(":");
                String user = userInfo[0];
                String password = userInfo[1];

                String[] dbInfo = dbUserInfo[1].split("#");
                String db = dbInfo[0];
                String table = dbInfo[1];

                return publishDbTable(action, gs, "postgis", host, port, user, password, db, table, "postgis", g.getNamespaceUrl(), metadataUuid, metadataTitle, metadataAbstract);
            } else {
                if (file.startsWith("file://") || file.startsWith("http://")) {
                    return addExternalFile(action, gs, file, metadataUuid, metadataTitle, metadataAbstract);
                } else {
                    FilePathChecker.verify(file);

                    // Get ZIP file from data directory
                    final Store store = context.getBean("resourceStore", Store.class);
                    try (Store.ResourceHolder resource = store.getResource(context, metadataUuid, file)) {
                        return addZipFile(action, gs, resource.getPath(), file, metadataUuid, metadataTitle, metadataAbstract);
                    }
                }
            }
        }
        return null;
    }

    private Element loadDbConfiguration(ServiceContext context) {
        final java.util.List<MapServer> mapservers =
            context.getBean(MapServerRepository.class)
                .findAll();
        geoserverNodes.clear();
        Element geoserverConfig = new Element("nodes");
        for (MapServer m : mapservers) {
            GeoServerNode g = new GeoServerNode(m);

            if (g != null) {
                geoserverNodes.put(m.getId(), g);

                Element node = new Element("node");
                node.addContent(new Element("id").setText(m.getId() + ""));
                node.addContent(new Element("name").setText(m.getName()));
                node.addContent(new Element("description").setText(m.getDescription()));
                node.addContent(new Element("namespacePrefix").setText(m.getNamespacePrefix()));
                node.addContent(new Element("namespaceUrl").setText(m.getNamespace()));
                node.addContent(new Element("adminUrl").setText(m.getConfigurl()));
                node.addContent(new Element("wmsUrl").setText(m.getWmsurl()));
                node.addContent(new Element("wfsUrl").setText(m.getWfsurl()));
                node.addContent(new Element("wcsUrl").setText(m.getWcsurl()));
                node.addContent(new Element("stylerUrl").setText(m.getStylerurl()));
                if (m.pushStyleInWorkspace())
                    node.addContent(new Element("pushStyleInWorkspace").setText("true"));
                else
                    node.addContent(new Element("pushStyleInWorkspace").setText("false"));
                geoserverConfig.addContent(node);
            }
        }
        return geoserverConfig;
    }

    /**
     * Register a database table in GeoServer
     *
     * @param metadataUuid  TODO
     * @param metadataTitle TODO
     */
    private Element publishDbTable(ACTION action, GeoServerRest g, String string,
                                   String host, String port, String user, String password, String db,
                                   String table, String dbType, String ns, String metadataUuid, String metadataTitle, String metadataAbstract) {
        try {
            if (action.equals(ACTION.CREATE) || action.equals(ACTION.UPDATE)) {
                StringBuilder report = new StringBuilder();
                // TODO : check datastore already exist
                if (!g.createDatabaseDatastore(db, host, port, db, user, password, dbType, ns))
                    report.append("Datastore: ").append(g.getStatus());
                if (!g.createFeatureType(db, table, metadataUuid, metadataTitle, metadataAbstract))
                    report.append("Feature type: ").append(g.getStatus());
                if (!g.createStyle(db, table))
                    report.append("Style: ").append(g.getStatus());
//				Publication of Datastore and feature type may failed if already exist
//				if (report.length() > 0) {
//					setErrorCode(report.toString());
//					return report(EXCEPTION, DB, getErrorCode());
//				}
            } else if (action.equals(ACTION.DELETE)) {
                StringBuilder report = new StringBuilder();
                if (!g.deleteLayer(table))
                    report.append("Layer: ").append(g.getStatus());
//				Only remove the layer in such situation
//				if (!g.deleteFeatureType(db, table))
//					report.append("Feature type: ").append(g.getStatus());
//				if (!g.deleteDatastore(db))
//					report.append("Datastore: ").append(g.getStatus());

                if (report.length() > 0) {
                    setErrorCode(report.toString());
                    return report(EXCEPTION, DB, getErrorCode());
                }
            }

            if (g.getLayer(table)) {
                setReport(Xml.loadString(g.getResponse(), false));
                return report(SUCCESS, DB, getReport());
            } else {
                setErrorCode(g.getStatus() + "");
                return report(EXCEPTION, DB, getErrorCode());
            }

        } catch (Exception e) {
            setErrorCode(e.getMessage());
            Log.error(MODULE, "Exception " + e.getMessage());
        }
        return report(EXCEPTION, DB, getErrorCode());
    }

    ;

    /**
     * Analyze ZIP file content and if valid, push the data to GeoServer.
     *
     * @param file @return
     */
    private Element addZipFile(ACTION action, GeoServerRest gs, Path f, String file, String metadataUuid, String metadataTitle, String metadataAbstract)
        throws IOException {
        if (f == null) {
            return report(EXCEPTION, null,
                "Could not find dataset file. Invalid zip file parameters: "
                    + file + ".");
        }
        Collection<String> rasterLayers, vectorLayers;

        // Handle multiple geofile.
        try (GeoFile gf = new GeoFile(f)) {


            try {
                vectorLayers = gf.getVectorLayers(true);
                if (vectorLayers.size() > 0) {
                    if (publishVector(f, gf, gs, action, metadataUuid, metadataTitle, metadataAbstract)) {
                        return report(SUCCESS, VECTOR, getReport());
                    } else {
                        return report(EXCEPTION, VECTOR, getErrorCode());
                    }
                }
            } catch (IllegalArgumentException e) {
                return report(EXCEPTION, VECTOR, e.getMessage());
            }

            try {
                rasterLayers = gf.getRasterLayers();
                if (rasterLayers.size() > 0) {
                    if (publishRaster(f, gs, action, metadataUuid, metadataTitle, metadataAbstract)) {
                        return report(SUCCESS, RASTER, getReport());
                    } else {
                        return report(EXCEPTION, RASTER, getErrorCode());
                    }
                }
            } catch (IllegalArgumentException e) {
                return report(EXCEPTION, RASTER, e.getMessage());
            }
        }

        if (vectorLayers.size() == 0 && rasterLayers.size() == 0) {
            return report(EXCEPTION, RASTER,
                "No vector or raster layers found in file (" + file
                    + ").");
        }
        return null;
    }

    private Element addExternalFile(ACTION action, GeoServerRest gs, String file, String metadataUuid, String metadataTitle, String metadataAbstract)
        throws IOException {
        // TODO vector or raster file ? Currently GeoServer does not support RASTER for external
        if (publishExternal(file, gs, action, metadataUuid, metadataTitle, metadataAbstract)) {
            return report(SUCCESS, VECTOR, getReport());
        } else {
            return report(EXCEPTION, VECTOR, getErrorCode());
        }
    }

    private Element report(String name, String type, String msg) {
        Element report = new Element(name);
        if (type != null)
            report.setAttribute("type", type);
        report.setAttribute("status", msg);
        return report;
    }

    private Element report(String name, String type, Element msg) {
        Element report = new Element(name);
        if (type != null)
            report.setAttribute("type", type);
        report.addContent(msg);
        return report;
    }

    private boolean publishVector(Path f, GeoFile gf, GeoServerRest g, ACTION action, String metadataUuid, String metadataTitle, String metadataAbstract) {

        String ds = f.getFileName().toString();
        String dsName = ds.substring(0, ds.lastIndexOf("."));
        try {
            if (action.equals(ACTION.CREATE)) {
                g.createDatastore(dsName, f);
                if (gf.containsSld())
                    g.createStyle(g.getDefaultWorkspace(), dsName, gf.getSld());
                else
                    g.createStyle(g.getDefaultWorkspace(), dsName);
                g.createFeatureType(dsName, dsName, metadataUuid, metadataTitle, metadataAbstract);
            } else if (action.equals(ACTION.UPDATE)) {
                g.createDatastore(dsName, f);
                g.createFeatureType(dsName, dsName, metadataUuid, metadataTitle, metadataAbstract);
            } else if (action.equals(ACTION.DELETE)) {
                String report = "";
                if (!g.deleteLayer(dsName))
                    report += "Layer: " + g.getStatus();
                if (!g.deleteFeatureType(dsName, dsName))
                    report += "Feature type: " + g.getStatus();
                if (!g.deleteDatastore(dsName))
                    report += "Datastore: " + g.getStatus();

                if (!report.equals("")) {
                    setErrorCode(report);
                    return false;
                }
            }
            if (g.getLayer(dsName)) {
                setReport(Xml.loadString(g.getResponse(), false));
            } else {
                setErrorCode(g.getStatus() + "");
                return false;
            }
            return true;

        } catch (Exception e) {
            setErrorCode(e.getMessage());
            Log.error(MODULE, "Exception " + e.getMessage());
        }
        return false;
    }

    private boolean publishExternal(String file, GeoServerRest g, ACTION action, String metadataUuid, String metadataTitle, String metadataAbstract) {

        String dsName = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
        boolean isRaster = GeoFile.fileIsRASTER(file);
        Log.error(MODULE, "Publish external: " + dsName + ", Raster: " + isRaster);
        try {
            if (action.equals(ACTION.CREATE)) {
                if (isRaster) {
                    g.createCoverage(dsName, file, metadataUuid, metadataTitle, metadataAbstract);
                } else {
                    g.createDatastore(dsName, file);
                    g.createStyle(dsName);
                }
            } else if (action.equals(ACTION.UPDATE)) {
                if (isRaster) {
                    g.createCoverage(dsName, file, metadataUuid, metadataTitle, metadataAbstract);
                } else {
                    g.createDatastore(dsName, file);
                }
            } else if (action.equals(ACTION.DELETE)) {
                String report = "";
                if (!g.deleteLayer(dsName))
                    report += "Layer: " + g.getStatus();
                if (isRaster) {

                } else {
                    if (!g.deleteFeatureType(dsName, dsName))
                        report += "Feature type: " + g.getStatus();
                    if (!g.deleteDatastore(dsName))
                        report += "Datastore: " + g.getStatus();
                }
                if (!report.equals("")) {
                    setErrorCode(report);
                    return false;
                }
            }
            if (g.getLayer(dsName)) {
                setReport(Xml.loadString(g.getResponse(), false));
            } else {
                setErrorCode(g.getStatus() + "");
                return false;
            }
            return true;

        } catch (Exception e) {
            setErrorCode(e.getMessage());
            Log.error(MODULE, "Exception " + e.getMessage());
        }
        return false;
    }

    private boolean publishRaster(Path f, GeoServerRest g, ACTION action, String metadataUuid, String metadataTitle, String metadataAbstract) {
        String cs = f.getFileName().toString();
        String csName = cs.substring(0, cs.lastIndexOf("."));
        try {
            if (action.equals(ACTION.CREATE)) {
                g.createCoverage(csName, f, metadataUuid, metadataTitle, metadataAbstract);
            } else if (action.equals(ACTION.UPDATE)) {
                g.createCoverage(csName, f, metadataUuid, metadataTitle, metadataAbstract);
            } else if (action.equals(ACTION.DELETE)) {
                String report = "";
                if (!g.deleteLayer(csName))
                    report += "Layer: " + g.getStatus();
                if (!g.deleteCoverage(csName, csName))
                    report += "Coverage: " + g.getStatus();
                if (!g.deleteCoverageStore(csName))
                    report += "Coveragestore: " + g.getStatus();

                if (!report.equals("")) {
                    setErrorCode(report);
                    return false;
                }
            }
            if (g.getLayer(csName)) {
                setReport(Xml.loadString(g.getResponse(), false));
            } else {
                setErrorCode(g.getStatus() + "");
                return false;
            }
            return true;

        } catch (Exception e) {
            setErrorCode(e.getMessage());
            Log.error(MODULE, "Exception " + e.getMessage());
        }
        return false;
    }

    /**
     * List of action valid for publisher service
     */
    private enum ACTION {
        /**
         * Return list of nodes
         */
        LIST, CREATE, UPDATE, DELETE, GET,
        ADD_NODE, REMOVE_NODE, UPDATE_NODE,
        UPDATE_NODE_ACCOUNT
    }
}
