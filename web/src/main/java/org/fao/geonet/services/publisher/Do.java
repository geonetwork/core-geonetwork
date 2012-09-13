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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

/**
 * Service to manage GeoServer dataset publication.
 * Dataset could be 
 * <ul>
 *   <li>ESRI Shapefile (zipped) POST or external</li>
 *   <li>PostGIS table</li>
 *   <li>GeoTiff (zip or not) POST or external</li>
 *   <li>ECW external</li>
 * </ul>
 * 
 * Shapefile must be zipped.
 * 
 * In case of ZIP compression, ZIP file base name must be equal to Shapefile or GeoTiff base name.
 * 
 * One Datastore, FeatureType, Layer and Style are created for a vector dataset (one to one relation).
 * One CoverageStore, Coverage, Layer are created for a raster dataset (one to one relation).
 * 
 * TODO : Support multi file publication
 * 
 */
public class Do implements Service {
	private static final String DB = "DB";

	private static final String VECTOR = "vector";

	private static final String RASTER = "raster";

	private static final String SUCCESS = "Success";

	private static final String EXCEPTION = "Exception";

	/**
	 * Module name
	 */
	public static final String MODULE = "geonetwork.GeoServerPublisher";

	/**
	 * XML document containing Geoserver node configuration defined in
	 * geoserver-nodes.xml
	 */
	private Element geoserverConfig;

	/**
	 * List of current known nodes
	 */
	private HashMap<String, GeoServerNode> geoserverNodes = new HashMap<String, GeoServerNode>();

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
	 * Load configuration file and register remote nodes. In order to register
	 * new nodes, restart is needed.
	 * 
	 */
	public void init(String appPath, ServiceConfig params) throws Exception {
		Log.createLogger(MODULE);

		// Load configuration
		String geoserverConfigFile = appPath
				+ params.getValue("configFile", "");

		Log.info(MODULE, "Using configuration: " + geoserverConfigFile);

		geoserverConfig = Xml.loadFile(geoserverConfigFile);
		if (geoserverConfig == null) {
			Log.error(MODULE, "Failed to load geoserver configuration file "
					+ geoserverConfigFile);
			return;
		}

		// Read configuration and register node
        if(Log.isDebugEnabled(MODULE))
            Log.debug(MODULE, "Start node registration");
		Collection<Element> nodes = geoserverConfig.getChildren("node");
		for (Element node : nodes) {
			// TODO : check mandatory values and reject node when relevant
			String id = node.getChildText("id");
			String name = node.getChildText("name");
            if(Log.isDebugEnabled(MODULE))
                Log.debug(MODULE, "  Register node:" + name);
			String url = node.getChildText("adminUrl");
			String namespacePrefix = node.getChildText("namespacePrefix");
			String namespaceUrl = node.getChildText("namespaceUrl");
            String user = node.getChildText("user");
			String password = node.getChildText("password");

			// sanitize data that will be returned when the list action is requested
			node.removeChild("user");
			node.removeChild("password");

			GeoServerNode g = new GeoServerNode(id, name, url, namespacePrefix,
					namespaceUrl, user, password);

			if (g != null)
				geoserverNodes.put(id, g);
		}
        if(Log.isDebugEnabled(MODULE))
            Log.debug(MODULE, "End node registration.");
	}

	/**
	 * List of action valid for publisher service
	 */
	private enum ACTION {
		/**
		 * Return list of nodes
		 */
		LIST, CREATE, UPDATE, DELETE, GET
	};

	/**
	 * Publish a dataset to a remote GeoServer node. Dataset could be a ZIP
	 * composed of Shapefile(s) or GeoTiff.
	 * 
	 * updataMetadataRecord, add or delete a online source link.
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		String baseUrl = gc.getSettingManager().getValue(Geonet.Settings.SERVER_PROTOCOL)
				+ "://" + gc.getSettingManager().getValue(Geonet.Settings.SERVER_HOST)
				+ ":" + gc.getSettingManager().getValue("system/server/port")
				+ context.getBaseUrl()
				+ "/srv/" + context.getLanguage() + "/";
		
		
		ACTION action = ACTION.valueOf(Util.getParam(params, "action"));
		if (action.equals(ACTION.LIST)) {
			return list();
		} else if (action.equals(ACTION.CREATE) || action.equals(ACTION.UPDATE)
				|| action.equals(ACTION.DELETE) || action.equals(ACTION.GET)) {

			// Check parameters
			String nodeId = Util.getParam(params, "nodeId");
			String metadataId = Util.getParam(params, "metadataId");
			String metadataUuid = Util.getParam(params, "metadataUuid", "");
			String metadataTitle = Util.getParam(params, "metadataTitle", "");
			GeoServerNode g = geoserverNodes.get(nodeId);
			if (g == null)
				throw new IllegalArgumentException(
						"Invalid node id "
								+ nodeId
								+ ". Can't find node id in current registered nodes. Use action=LIST parameter to retrieve the list of valid nodes.");

			GeoServerRest gs = new GeoServerRest(g.getUrl(), g.getUsername(), g.getUserpassword(), g.getNamespacePrefix(), baseUrl);

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
				
				return publishDbTable(action, gs, "postgis", host, port, user, password, db, table, "postgis", g.getNamespaceUrl(), metadataUuid, metadataTitle);
			} else {
			    if (file.startsWith("file://") || file.startsWith("http://")) {
			        return addExternalFile(action, gs, file, metadataUuid, metadataTitle);
			    } else {
			        // Get ZIP file from data directory
	                File dir = new File(Lib.resource
	                        .getDir(context, access, metadataId));
	                File f = new File(dir, file);
	                return addZipFile(action, gs, f, file, metadataUuid, metadataTitle);
			    }
			}
		}
		return null;
	}

	/**
	 * Register a database table in GeoServer
	 * 
	 * @param action
	 * @param g
	 * @param string
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 * @param db
	 * @param table
	 * @param dbType
	 * @param metadataUuid TODO
	 * @param metadataTitle TODO
	 * @return
	 */
	private Element publishDbTable(ACTION action, GeoServerRest g, String string,
			String host, String port, String user, String password, String db,
			String table, String dbType, String ns, String metadataUuid, String metadataTitle) {
		try {
			if (action.equals(ACTION.CREATE) || action.equals(ACTION.UPDATE)) {
				String report = "";
				// TODO : check datastore already exist
				if (!g.createDatabaseDatastore(db, host, port, db, user, password, dbType, ns))
					report += "Datastore: " + g.getStatus();
				if (!g.createFeatureType(db, table, true, metadataUuid, metadataTitle))
					report += "Feature type: " + g.getStatus();
//				Publication of Datastore and feature type may failed if already exist
//				if (!report.equals("")) {
//					setErrorCode(report);
//					return report(EXCEPTION, DB, getErrorCode());
//				}
			} else if (action.equals(ACTION.DELETE)) {
				String report = "";
				if (!g.deleteLayer(table))
					report += "Layer: " + g.getStatus();
//				Only remove the layer in such situation
//				if (!g.deleteFeatureType(db, table))
//					report += "Feature type: " + g.getStatus();
//				if (!g.deleteDatastore(db))
//					report += "Datastore: " + g.getStatus();

				if (!report.equals("")) {
					setErrorCode(report);
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
		}		return report(EXCEPTION, DB, getErrorCode());
	}

	/**
	 * Analyze ZIP file content and if valid, push the data
	 * to GeoServer.
	 * 
	 * @param context
	 * @param action
	 * @param metadataId
	 * @param gs
	 * @param file
	 * @param access
	 * @return
	 * @throws IOException
	 */
	private Element addZipFile(Do.ACTION action, GeoServerRest gs, File f, String file, String metadataUuid, String metadataTitle)
			throws IOException {
		if (f == null) {
			return report(EXCEPTION, null,
					"Could not find dataset file. Invalid zip file parameters: "
							+ file + ".");
		}

		// Handle multiple geofile.
		GeoFile gf = new GeoFile(f);

		Collection<String> rasterLayers, vectorLayers;

		try {
			vectorLayers = gf.getVectorLayers(true);
			if (vectorLayers.size() > 0) {
				if (publishVector(f, gs, action, metadataUuid, metadataTitle)) {
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
				if (publishRaster(f, gs, action, metadataUuid, metadataTitle)) {
					return report(SUCCESS, RASTER, getReport());
				} else {
					return report(EXCEPTION, RASTER, getErrorCode());
				}
			}
		} catch (IllegalArgumentException e) {
			return report(EXCEPTION, RASTER, e.getMessage());
		}

		if (vectorLayers.size() == 0 && rasterLayers.size() == 0) {
			return report(EXCEPTION, RASTER,
					"No vector or raster layers found in file (" + file
							+ ").");
		}
		return null;
	}

	private Element addExternalFile(Do.ACTION action, GeoServerRest gs, String file, String metadataUuid, String metadataTitle)
            throws IOException {
	    // TODO vector or raster file ? Currently GeoServer does not support RASTER for external
	    if (publishExternal(file, gs, action, metadataUuid, metadataTitle)) {
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

	private boolean publishVector(File f, GeoServerRest g, ACTION action, String metadataUuid, String metadataTitle) {

		String ds = f.getName();
		String dsName = ds.substring(0, ds.lastIndexOf("."));
		try {
			if (action.equals(ACTION.CREATE)) {
				g.createDatastore(dsName, f, true);
				g.createFeatureType(dsName, dsName, false, metadataUuid, metadataTitle);
			} else if (action.equals(ACTION.UPDATE)) {
				g.createDatastore(dsName, f, false);
				g.createFeatureType(dsName, dsName, false, metadataUuid, metadataTitle);
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

	private boolean publishExternal(String file, GeoServerRest g, ACTION action, String metadataUuid, String metadataTitle) {

        String dsName = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
        boolean isRaster = GeoFile.fileIsRASTER(file);
        Log.error(MODULE, "Publish external: " + dsName + ", Raster: " + isRaster);
        try {
            if (action.equals(ACTION.CREATE)) {
                if (isRaster) {
                	g.createCoverage(dsName, file, metadataUuid, metadataTitle);
                } else {
                    g.createDatastore(dsName, file, true);
                }
            } else if (action.equals(ACTION.UPDATE)) {
                if (isRaster) {
                	g.createCoverage(dsName, file, metadataUuid, metadataTitle);
                } else {
                    g.createDatastore(dsName, file, false);
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
	private boolean publishRaster(File f, GeoServerRest g, ACTION action, String metadataUuid, String metadataTitle) {
		String cs = f.getName();
		String csName = cs.substring(0, cs.lastIndexOf("."));
		try {
			if (action.equals(ACTION.CREATE)) {
				g.createCoverage(csName, f, metadataUuid, metadataTitle);
			} else if (action.equals(ACTION.UPDATE)) {
				g.createCoverage(csName, f, metadataUuid, metadataTitle);
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
	 * Return list of registered node
	 * 
	 * @return
	 */
	private Element list() {
		return geoserverConfig;
	}
}