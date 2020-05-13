//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.wfsfeatures;

import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import org.apache.jcs.access.exception.CacheException;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester.FragmentParams;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester.HarvestSummary;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlElementReader;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import javax.xml.stream.FactoryConfigurationError;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

//=============================================================================

/**
 * A WfsFeaturesHarvester is able to harvest metadata fragments from the GetFeature response of an
 * OGC WFS. The fragments that are obtained from the WFS are saved into the GeoNetwork database as
 * subtemplates. The editor will offer these during the edit session for use when creating metadata
 * records.
 *
 * <pre>
 * <nodes>
 *  <node type="wfsfeatures" id="300">
 *    <site>
 *      <name>TEST</name>
 *      <uuid>c1da2928-c866-49fd-adde-466fe36d3508</uuid>
 *      <account>
 *        <use>true</use>
 *        <username />
 *        <password />
 *      </account>
 *      <url>http://localhost:8080/deegree/wfs</url>
 *      <query><wfs getfeature query ..../></query>
 *      <outputSchema>iso19139</outputSchema>
 *      <stylesheet>transform_response.xsl</stylesheet>
 *      <streamFeatures>false</streamFeatures>
 *      <createSubtemplates>true</createSubtemplates>
 *      <templateId>3</templateId>
 *      <icon>wfs.gif</icon>
 *    </site>
 *    <options>
 *      <every>90</every>
 *      <oneRunOnly>false</oneRunOnly>
 *      <status>active</status>
 *      <lang>eng</lang>
 *      <recordsCategory></recordsCategory>
 *    </options>
 *    <privileges>
 *      <group id="1">
 *        <operation name="view" />
 *      </group>
 *    </privileges>
 *    <categories>
 *      <category id="3" />
 *    </categories>
 *    <info>
 *      <lastRun>2007-12-05T16:17:20</lastRun>
 *      <running>false</running>
 *    </info>
 *  </node>
 * </nodes>
 * </pre>
 *
 * @author sppigot
 */
class Harvester implements IHarvester<HarvestResult> {
    private final AtomicBoolean cancelMonitor;


    //---------------------------------------------------------------------------
    private Logger log;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    private ServiceContext context;
    private WfsFeaturesParams params;
    private IMetadataManager metadataManager;
    private SchemaManager schemaMan;
    private HarvestResult result;
    private UUIDMapper localUuids;
    private String metadataGetService;


    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------
    private Path stylesheetDirectory;
    private Map<String, Object> ssParams = new HashMap<String, Object>();
    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private List<HarvestError> errors = new LinkedList<HarvestError>();
    /**
     * Constructor
     *
     * @param context Jeeves context
     * @param params  harvesting configuration for the node
     * @return null
     */
    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, WfsFeaturesParams params) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;

        result = new HarvestResult();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        metadataManager = gc.getBean(IMetadataManager.class);
        schemaMan = gc.getBean(SchemaManager.class);
        SettingInfo si = context.getBean(SettingInfo.class);
        String siteUrl = si.getSiteUrl() + context.getBaseUrl();
        metadataGetService = "local://"+context.getNodeId()+"/api/records/";
        ssParams.put("siteUrl", siteUrl);
    }

    /**
     * Start the harvesting of fragments from the WFS node.
     *
     *
     *
     * <?xml version="1.0" encoding="utf-8"?> <wfs:GetFeature xmlns:ogc="http://www.opengis.net/ogc"
     * xmlns:gml="http://www.opengis.net/gml" xmlns:wfs="http://www.opengis.net/wfs"
     * outputFormat="text/xml; subtype=gml/3.1.1"> <wfs:Query xmlns:app="http://www.deegree.org/app"
     * typeName="app:list_parcel_property"> <wfs:PropertyName>app:cadastral_id</wfs:PropertyName>
     * <wfs:PropertyName>app:property_id</wfs:PropertyName> <wfs:PropertyName>app:owner_name</wfs:PropertyName>
     * <wfs:PropertyName>app:addressline1</wfs:PropertyName> <wfs:PropertyName>app:addressline2</wfs:PropertyName>
     * <wfs:PropertyName>app:addressline3</wfs:PropertyName> <wfs:PropertyName>app:addressline4</wfs:PropertyName>
     * <wfs:PropertyName>app:database_access_date</wfs:PropertyName> <wfs:PropertyName>app:GEOM</wfs:PropertyName>
     * <ogc:Filter> <ogc:DWithin xmlns:gml='http://www.opengis.net/gml' > <ogc:PropertyName
     * xmlns:app="http://www.deegree.org/app">app:GEOM</ogc:PropertyName> <gml:Polygon>
     * <gml:outerBoundaryIs> <gml:LinearRing> <gml:coordinates cs="," decimal="." ts="
     * ">506964.28,5413897.046 507051.215,5413999.211 507039.46,5414009.211 507012.503,5414032.146
     * 506902.284,5413943.883 506964.28,5413897.046</gml:coordinates> </gml:LinearRing>
     * </gml:outerBoundaryIs> </gml:Polygon> <ogc:Distance>500.0</ogc:Distance> </ogc:DWithin>
     * </ogc:Filter> </wfs:Query> </wfs:GetFeature>
     */
    public HarvestResult harvest(Logger log) throws Exception {

        this.log = log;
        log.info("Retrieving metadata fragments for : " + params.getName());

        //--- collect all existing metadata uuids before we update
        final IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
        localUuids = new UUIDMapper(metadataRepository, params.getUuid());

        //--- parse the xml query from the string - TODO: default should be
        //--- get everything
        Element wfsQuery = null;

        log.info("Parsing query :\n" + params.query);
        try {
            wfsQuery = Xml.loadString(params.query, false);
        } catch (JDOMException e) {
            errors.add(new HarvestError(context, e));
            throw new BadParameterEx("GetFeature Query failed to parse\n", params.query);
        }

        //--- harvest metadata and subtemplates from fragments using generic fragment harvester
        FragmentHarvester fragmentHarvester = new FragmentHarvester(cancelMonitor, log, context, getFragmentHarvesterParams());

        if (params.streamFeatures) {
            harvestFeatures(wfsQuery, fragmentHarvester);
        } else {
            harvestResponse(wfsQuery, fragmentHarvester);
        }

        return result;
    }

    /**
     * Harvest fragments from the response document
     */

    private void harvestResponse(Element xmlQuery,
                                 FragmentHarvester fragmentHarvester) throws IOException,
        JDOMException, MalformedURLException, BadXmlResponseEx, Exception {

        //--- post the query to the remote site
        Element xml = Xml.loadFile(new URL(params.url), xmlQuery);

        if (xml == null) {
            throw new BadXmlResponseEx("No response or problem getting response from " + params.url + ":\n" + Xml.getString(xmlQuery));
        }

        //--- apply stylesheet from output schema - stylesheet can be optional
        //--- in case the server can do XSL transformations for us (eg. deegree
        //--- 2.2)
        stylesheetDirectory = schemaMan.getSchemaDir(params.outputSchema).resolve(Geonet.Path.WFS_STYLESHEETS);
        if (!params.stylesheet.trim().equals("")) {
            xml = Xml.transform(xml, stylesheetDirectory.resolve(params.stylesheet), ssParams);
        }

        log.info("Applying " + stylesheetDirectory + "/" + params.stylesheet);
        harvest(xml, fragmentHarvester);
    }

    /**
     * Harvest fragments by applying a stylesheet to each feature as it is received (reduces memory
     * usage for large documents)
     */

    private void harvestFeatures(Element xmlQuery, FragmentHarvester fragmentHarvester)
        throws FactoryConfigurationError, Exception {
        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(params.url);
        req.setRequest(xmlQuery);
        Lib.net.setupProxy(context, req);

        Path tempFile = Files.createTempFile("temp-", ".xml");

        // Read response into temporary file
        req.executeLarge(tempFile);

        List<Namespace> namespaces = new ArrayList<Namespace>();
        namespaces.add(Namespace.getNamespace("gml", "http://www.opengis.net/gml"));

        XmlElementReader reader;
        try (InputStream fin = IO.newInputStream(tempFile)) {
            reader = new XmlElementReader(fin, "gml:featureMembers/*", namespaces);
        }

        if (cancelMonitor.get()) {
            return;
        }

        if (!reader.hasNext()) {
            namespaces.add(Namespace.getNamespace("wfs", "http://www.opengis.net/wfs"));
            try (InputStream fin = IO.newInputStream(tempFile)) {
                reader = new XmlElementReader(fin, "wfs:FeatureCollection/gml:featureMember", namespaces);

            }
        }

        while (reader.hasNext()) {
            if (cancelMonitor.get()) {
                return;
            }

            stylesheetDirectory = schemaMan.getSchemaDir(params.outputSchema).resolve(Geonet.Path.WFS_STYLESHEETS);
            Element records = Xml.transform(reader.next(), stylesheetDirectory.resolve(params.stylesheet), ssParams);

            harvest(records, fragmentHarvester);
        }
    }

    /**
     * Harvest fragments from the element passed
     */

    private void harvest(Element xml, FragmentHarvester fragmentHarvester)
        throws Exception {

        HarvestSummary fragmentResult = fragmentHarvester.harvest(xml, params.url);

        deleteOrphanedMetadata(fragmentResult.updatedMetadata);

        result.fragmentsReturned += fragmentResult.fragmentsReturned;
        result.fragmentsUnknownSchema += fragmentResult.fragmentsUnknownSchema;
        result.subtemplatesAdded += fragmentResult.fragmentsAdded;
        result.fragmentsMatched += fragmentResult.fragmentsMatched;
        result.recordsBuilt += fragmentResult.recordsBuilt;
        result.recordsUpdated += fragmentResult.recordsUpdated;
        result.subtemplatesUpdated += fragmentResult.fragmentsUpdated;

        result.totalMetadata = result.subtemplatesAdded + result.addedMetadata;
        result.originalMetadata = result.fragmentsReturned;
    }

    /**
     * Remove old metadata and subtemplates and uncache any subtemplates that are left over after
     * the update.
     */

    public void deleteOrphanedMetadata(Set<String> updatedMetadata) throws Exception {
        if (log.isDebugEnabled())
            log.debug("  - Removing orphaned metadata records and fragments after update");

        for (String uuid : localUuids.getUUIDs()) {
            try {
                String isTemplate = localUuids.getTemplate(uuid);
                if (isTemplate.equals("s")) {
                    Processor.uncacheXLinkUri(metadataGetService + uuid);
                }

                if (!updatedMetadata.contains(uuid)) {
                    String id = localUuids.getID(uuid);
                    metadataManager.deleteMetadata(context, id);

                    if (isTemplate.equals("s")) {
                        result.subtemplatesRemoved++;
                    } else {
                        result.locallyRemoved++;
                    }
                }
            } catch (CacheException e) {
                HarvestError error = new HarvestError(context, e);
                this.errors.add(error);
            } catch (Exception e) {
                HarvestError error = new HarvestError(context, e);
                this.errors.add(error);
            }
        }

        if (result.subtemplatesRemoved + result.locallyRemoved > 0) {
            metadataManager.flush();
        }
    }

    /**
     * Get generic fragment harvesting parameters from metadata fragment harvesting parameters
     */

    private FragmentParams getFragmentHarvesterParams() {
        FragmentParams fragmentParams = new FragmentHarvester.FragmentParams();
        fragmentParams.categories = params.getCategories();
        fragmentParams.createSubtemplates = params.createSubtemplates;
        fragmentParams.outputSchema = params.outputSchema;
        fragmentParams.isoCategory = params.recordsCategory;
        fragmentParams.privileges = params.getPrivileges();
        fragmentParams.templateId = params.templateId;
        fragmentParams.uuid = params.getUuid();
        fragmentParams.owner = params.getOwnerId();
        return fragmentParams;
    }

    public List<HarvestError> getErrors() {
        return errors;
    }
}
