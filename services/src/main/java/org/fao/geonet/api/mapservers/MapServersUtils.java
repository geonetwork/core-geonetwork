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

package org.fao.geonet.api.mapservers;

import org.fao.geonet.api.exception.GeoPublisherException;
import org.fao.geonet.utils.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Created by francois on 23/06/16.
 */
public class MapServersUtils {
    public static final String MODULE = "geonetwork.geoserver.publisher";

    /**
     * Register a database table in GeoServer
     *
     * @param metadataUuid  TODO
     * @param metadataTitle TODO
     */
    public static String publishDbTable(ACTION action, GeoServerRest g, String string,
                                        String host, String port, String user, String password, String db,
                                        String table, String dbType, String ns, String metadataUuid, String metadataTitle, String metadataAbstract) throws GeoPublisherException, IOException {
        if (action == ACTION.CREATE || action == ACTION.UPDATE) {
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
        } else if (action == ACTION.DELETE) {
            StringBuilder report = new StringBuilder();
            if (!g.deleteLayer(table))
                report.append("Layer: ").append(g.getStatus());
//				Only remove the layer in such situation
//				if (!g.deleteFeatureType(db, table))
//					report.append("Feature type: ").append(g.getStatus());
//				if (!g.deleteDatastore(db))
//					report.append("Datastore: ").append(g.getStatus());

            if (report.length() > 0) {
                throw new GeoPublisherException(String.format(
                    "Failed to unpublish database table. Status '%d', error is %s",
                    g.getStatus(), g.getResponse()
                ));
            }
        }

        if (g.getLayer(table)) {
            return String.format(
                "Database table '%s' is published in the mapserver",
                table
            );
        } else {
            throw new GeoPublisherException(String.format(
                "Database table not found. Status '%d'.",
                g.getStatus(), g.getResponse()
            ));
        }
    }

    /**
     * Analyze ZIP file content and if valid, push the data to GeoServer.
     *
     * @param file @return
     */
    public static String addZipFile(ACTION action, GeoServerRest gs, Path f, String file, String metadataUuid, String metadataTitle, String metadataAbstract)
        throws IOException, GeoPublisherException {
        if (f == null) {
            throw new GeoPublisherException(String.format(
                "File is null. Check parameters."
            ));
        }
        Collection<String> rasterLayers, vectorLayers;

        // Handle multiple geofile.
        try (GeoFile gf = new GeoFile(f)) {
            try {
                vectorLayers = gf.getVectorLayers(true);
                if (vectorLayers.size() > 0) {
                    if (publishVector(f, gf, gs, action, metadataUuid, metadataTitle, metadataAbstract)) {
                        if (action == ACTION.DELETE) {
                            return gs.getReport();
                        } else {
                            return String.format(
                                "Vector layer '%s' is published in the mapserver.",
                                f
                            );
                        }
                    } else {
                        throw new GeoPublisherException(String.format(
                            "%s %s",
                            gs.getReport(), gs.getErrorCode()
                        ));
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new GeoPublisherException(String.format(
                    "Failed to publish vector file '%s'. Error is %s",
                    f, e.getMessage()
                ));
            }

            try {
                rasterLayers = gf.getRasterLayers();
                if (rasterLayers.size() > 0) {
                    if (publishRaster(f, gs, action, metadataUuid, metadataTitle, metadataAbstract)) {
                        if (action == ACTION.DELETE) {
                            return gs.getReport();
                        } else {
                            return String.format(
                                "Raster layer '%s' is published in the mapserver.",
                                f
                            );
                        }
                    } else {
                        throw new GeoPublisherException(String.format(
                            "Failed to publish raster file '%s'. Error is %s",
                            f, gs.getErrorCode()
                        ));
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new GeoPublisherException(String.format(
                    "Failed to publish raster file '%s'. Error is %s",
                    f, e.getMessage()
                ));
            }
        }

        if (vectorLayers.size() == 0 && rasterLayers.size() == 0) {
            throw new GeoPublisherException(
                "No file defined for publication"
            );
        }
        return null;
    }

    public static String addExternalFile(ACTION action, GeoServerRest gs, String file, String metadataUuid, String metadataTitle, String metadataAbstract)
        throws IOException, GeoPublisherException {
        // TODO vector or raster file ? Currently GeoServer does not support RASTER for external
        if (publishExternal(file, gs, action, metadataUuid, metadataTitle, metadataAbstract)) {
            return String.format(
                "File '%s' is published in the mapserver.",
                file
            );
        } else {
            throw new GeoPublisherException(String.format(
                "File '%s' not found in mapserver. Try to publish it? Status code is %s.",
                file, gs.getErrorCode()
            ));
        }
    }

    public static boolean publishVector(Path f, GeoFile gf, GeoServerRest g, ACTION action, String metadataUuid, String metadataTitle, String metadataAbstract) {

        String ds = f.getFileName().toString();
        String dsName = ds.substring(0, ds.lastIndexOf("."));
        try {
            if (action == ACTION.CREATE) {
                g.createDatastore(dsName, f, gf.getFormat());
                if (gf.containsSld())
                    g.createStyle(g.getDefaultWorkspace(), dsName, gf.getSld());
                else
                    g.createStyle(g.getDefaultWorkspace(), dsName);
                g.createFeatureType(dsName, dsName, metadataUuid, metadataTitle, metadataAbstract);
            } else if (action == ACTION.UPDATE) {
                g.createDatastore(dsName, f, gf.getFormat());
                g.createFeatureType(dsName, dsName, metadataUuid, metadataTitle, metadataAbstract);
            } else if (action == ACTION.DELETE) {
                String report = "";
                if (!g.deleteLayer(dsName))
                    report += "Layer: " + g.getStatus();
                if (!g.deleteFeatureType(dsName, dsName))
                    report += "Feature type: " + g.getStatus();
                if (!g.deleteDatastore(dsName))
                    report += "Datastore: " + g.getStatus();

                if (!report.equals("")) {
                    g.setErrorCode(report);
                    return false;
                }
                g.setReport(String.format("" +
                        "Layer '%s' remove from mapserver.",
                    dsName
                ));
                return true;
            }
            if (g.getLayer(dsName)) {
                g.setReport(g.getResponse());
            } else {
                g.setErrorCode(String.format(
                    "Layer '%s' not found in mapserver. Try to publish it? Status code is %d.",
                    dsName, g.getStatus()
                ));
                return false;
            }
            return true;

        } catch (Exception e) {
            g.setErrorCode(e.getMessage());
            Log.error(MODULE, "Exception " + e.getMessage());
        }
        return false;
    }

    public static boolean publishExternal(String file, GeoServerRest g, ACTION action, String metadataUuid, String metadataTitle, String metadataAbstract) {

        String dsName = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
        boolean isRaster = GeoFile.fileIsRASTER(file);
        Log.error(MODULE, "Publish external: " + dsName + ", Raster: " + isRaster);
        try {
            if (action == ACTION.CREATE) {
                if (isRaster) {
                    g.createCoverage(dsName, file, metadataUuid, metadataTitle, metadataAbstract);
                } else {
                    g.createDatastore(dsName, file);
                    g.createStyle(dsName);
                }
            } else if (action == ACTION.UPDATE) {
                if (isRaster) {
                    g.createCoverage(dsName, file, metadataUuid, metadataTitle, metadataAbstract);
                } else {
                    g.createDatastore(dsName, file);
                }
            } else if (action == ACTION.DELETE) {
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
                    g.setErrorCode(report);
                    return false;
                }
            }
            if (g.getLayer(dsName)) {
                g.setReport(g.getResponse());
            } else {
                g.setErrorCode(g.getStatus() + "");
                return false;
            }
            return true;

        } catch (Exception e) {
            g.setErrorCode(e.getMessage());
            Log.error(MODULE, "Exception " + e.getMessage());
        }
        return false;
    }

    public static boolean publishRaster(Path f, GeoServerRest g, ACTION action, String metadataUuid, String metadataTitle, String metadataAbstract) {
        String cs = f.getFileName().toString();
        String csName = cs.substring(0, cs.lastIndexOf("."));
        try {
            if (action == ACTION.CREATE) {
                g.createCoverage(csName, f, metadataUuid, metadataTitle, metadataAbstract);
            } else if (action == ACTION.UPDATE) {
                g.createCoverage(csName, f, metadataUuid, metadataTitle, metadataAbstract);
            } else if (action == ACTION.DELETE) {
                String report = "";
                if (!g.deleteLayer(csName))
                    report += "Layer: " + g.getStatus();
                if (!g.deleteCoverage(csName, csName))
                    report += "Coverage: " + g.getStatus();
                if (!g.deleteCoverageStore(csName))
                    report += "Coveragestore: " + g.getStatus();

                if (!report.equals("")) {
                    g.setErrorCode(report);
                    return false;
                }
            }
            if (g.getLayer(csName)) {
                g.setReport(g.getResponse());
            } else {
                g.setErrorCode(g.getStatus() + "");
                return false;
            }
            return true;

        } catch (Exception e) {
            g.setErrorCode(e.getMessage());
            Log.error(MODULE, "Exception " + e.getMessage());
        }
        return false;
    }

    public enum ACTION {
        CREATE, UPDATE, DELETE, GET
    }
}
