//=============================================================================
//===    Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geonet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.XmlRequest;

/**
 * BaseGeoNetworkHarvester is an abstract base class that provides core functionality for
 * implementing harvesters specifically designed to interact with GeoNetwork systems.
 * It facilitates handling configurations, managing remote sources, and processing
 * records during the harvesting process.
 *
 * @param <P> A type parameter extending BaseGeonetParams, representing the specific
 *            configuration or parameters utilized by the harvester.
 */
public abstract class BaseGeoNetworkHarvester<P extends BaseGeonetParams> {
    protected final AtomicBoolean cancelMonitor;
    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    protected final List<HarvestError> errors;
    protected Logger log;
    protected P params;
    protected ServiceContext context;

    /**
     * Constructor for the BaseGeoNetworkHarvester class, initializing
     * the essential components required for harvesting operations.
     *
     * @param cancelMonitor an AtomicBoolean used to monitor cancellation requests during the harvesting process
     * @param log a Logger instance used for logging messages and errors
     * @param context the ServiceContext providing access to system-level resources and services
     * @param params the parameters used to configure the harvester, specific to its implementation
     * @param errors a list of HarvestError objects used to collect errors encountered during the harvesting process
     */
    public BaseGeoNetworkHarvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, P params, List<HarvestError> errors) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;
        this.errors = errors;
    }

    /**
     * Updates the sources based on the given records and remote sources map. This method aligns source logos
     * and updates local sources by saving changes to the source repository. If the source is unrecognized,
     * it assigns a default unknown logo.
     *
     * @param records a sorted set of RecordInfo objects, representing metadata records that have been fetched
     *                or updated during the harvesting process
     * @param remoteSources a map containing source UUIDs as keys and corresponding Source objects as values,
     *                      representing the sources retrieved from the remote server
     * @throws MalformedURLException if an error occurs while retrieving or handling source logos from a malformed URL
     */
    protected void updateSources(SortedSet<RecordInfo> records,
                                 Map<String, Source> remoteSources) throws MalformedURLException {
        log.info("Aligning source logos from for : " + params.getName());

        //--- collect all different sources that have been harvested
        Set<String> sources = new HashSet<>();

        for (RecordInfo ri : records) {
            sources.add(ri.source);
        }

        //--- update local sources and retrieve logos (if the case)
        String siteId = context.getBean(SettingManager.class).getSiteId();
        final Resources resources = context.getBean(Resources.class);

        for (String sourceUuid : sources) {
            if (!siteId.equals(sourceUuid)) {
                Source source = remoteSources.get(sourceUuid);

                if (source != null) {
                    retrieveLogo(context, resources, params.host, sourceUuid);
                } else {
                    String sourceName = "(unknown)";
                    source = new Source(sourceUuid, sourceName, new HashMap<>(), SourceType.harvester);
                    resources.copyUnknownLogo(context, sourceUuid);
                }

                context.getBean(SourceRepository.class).save(source);
            }
        }
    }

    /**
     * Retrieves a logo file from a specified URL and saves it locally. If the logo cannot
     * be retrieved, a default unknown logo is used instead.
     *
     * @param context the {@link ServiceContext} instance providing access to system-level resources and services
     * @param resources the {@link Resources} object used to locate and manipulate the logos directory
     * @param url the base URL from which the logo file is retrieved
     * @param uuid the unique identifier corresponding to the logo, typically used as the logo's filename
     * @throws MalformedURLException if the constructed URL for the logo retrieval or proxy setup is malformed
     */
    protected void retrieveLogo(ServiceContext context, final Resources resources, String url, String uuid) throws MalformedURLException {
        String logo = uuid + ".gif";
        String baseUrl = url;
        if (!new URL(baseUrl).getPath().endsWith("/")) {
            // Needed to make it work when harvesting from a GN deployed at ROOT ("/")
            baseUrl += "/";
        }
        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(baseUrl));
        Lib.net.setupProxy(context, req);
        req.setAddress(req.getAddress() + "images/logos/" + logo);

        final Path logoDir = resources.locateLogosDir(context);

        try {
            resources.createImageFromReq(context, logoDir, logo, req);
        } catch (IOException e) {
            context.warning("Cannot retrieve logo file from : " + url);
            context.warning("  (C) Logo  : " + logo);
            context.warning("  (C) Excep : " + e.getMessage());

            resources.copyUnknownLogo(context, uuid);
        }
    }
}
