//=============================================================================
//===	Copyright (C) 2010 GeoNetwork
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

package org.fao.geonet.kernel.schema;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//=============================================================================

/**
 * Provides utility to load a new standard.
 * It was supporting simple schema requiring only
 * to be unzipped in the schema folder. This does
 * not work with new schema generation which also
 * declare a Bean and have a JAR file to be added
 * in the classloader. This require the webapp
 * to restart. TODO: Improve
 */
public class SchemaUtils {

    public Element addSchema(ServiceContext context, String schema, String fname, URL url, String uuid, SchemaManager scm) throws Exception {
        return processSchema(context, schema, fname, url, uuid, scm, true);
    }

    // --------------------------------------------------------------------------

    public Element updateSchema(ServiceContext context, String schema, String fname, URL url, String uuid, SchemaManager scm) throws Exception {
        return processSchema(context, schema, fname, url, uuid, scm, false);
    }

    // --------------------------------------------------------------------------

    private Element processSchema(ServiceContext context, String schema, String fname, URL url, String uuid, SchemaManager scm, boolean add) throws Exception {

        boolean deleteTempZip = false;

        // -- get the URL of schema zip archive from a metadata record if uuid set
        if (!("".equals(uuid))) {
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            DataManager dm = gc.getBean(DataManager.class);

            String id = dm.getMetadataId(uuid.toLowerCase());
            if (id == null) {
                throw new OperationAbortedEx("Metadata record with uuid " + uuid + " doesn't exist");
            }

            // -- check download permissions (should be ok since admin but...)
            try {
                Lib.resource.checkPrivilege(context, id, ReservedOperation.download);
            } catch (Exception e) {
                throw new OperationAbortedEx("Download access not available on metadata record with uuid " + uuid);
            }

            // -- get metadata
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            Element elMd = dm.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

            if (elMd == null) {
                throw new OperationAbortedEx("Metadata record " + uuid + " doesn't exist");
            }

            // -- transform record into brief version
            Element elBrief = dm.extractSummary(elMd);

            // -- find link using XPath and create URL for further processing
            XPath xp = XPath.newInstance("link[contains(@protocol,'metadata-schema')]");
            List<?> elems = xp.selectNodes(elBrief);
            try {
                url = getMetadataSchemaUrl(elems);
            } catch (MalformedURLException mu) {
                throw new OperationAbortedEx("Metadata schema URL link for metadata record " + uuid + " is malformed : " + mu.getMessage());
            }

            if (url == null) {
                throw new OperationAbortedEx("Unable to find metadata schema URL link for metadata record " + uuid);
            }
        }

        Path zipArchive;
        // -- get the schema zip archive from the net
        if (url != null) {
            XmlRequest strReq = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(url);
            zipArchive = Files.createTempDirectory("schema");
            deleteTempZip = true;

            // FIXME: add proxy credentials etc to strReq
            strReq.executeLarge(zipArchive);


        } else {
            zipArchive = IO.toPath(fname);
        }

        Element response = doSchema(context, scm, schema, zipArchive, add);
        if (deleteTempZip) IO.deleteFile(zipArchive, false, Geonet.SCHEMA_MANAGER);
        return response;
    }

    // --------------------------------------------------------------------------

    private Element doSchema(ServiceContext context, SchemaManager scm, String schema, Path zipArchive, boolean add) throws Exception {

        Element response = new Element("response");

        long fsize;
        if (Files.exists(zipArchive)) {
            fsize = Files.size(zipArchive);
        } else {
            throw new OperationAbortedEx("Zip Archive doesn't exist");
        }

        // -- check that the archive actually has something in it
        if (fsize == 0) {
            throw new OperationAbortedEx("Schema archive has zero size");
        }


        // -- supply the stream containing the schema zip archive to the schema
        // -- manager
        try (FileSystem zipFs = ZipUtil.openZipFs(zipArchive)) {
            if (add) {
                scm.addPluginSchema(context.getApplicationContext(), schema, zipFs);
            } else {
                scm.updatePluginSchema(context.getApplicationContext(), schema, zipFs);
            }
            response.setAttribute("status", "ok");
            response.setAttribute("message", "Schema " + schema + " has been added/updated");
        } catch (Exception e) {
            throw new OperationAbortedEx("Schema add/update failed: " + e.getMessage(), e);
        }

        return response;
    }

    // --------------------------------------------------------------------------

    private URL getMetadataSchemaUrl(List<?> elems) throws MalformedURLException {
        for (Object ob : elems) {
            if (ob instanceof Element) {
                Element elem = (Element) ob;
                String href = elem.getAttributeValue("href");
                return new URL(href);
            }
        }

        return null;
    }

}

// =============================================================================

