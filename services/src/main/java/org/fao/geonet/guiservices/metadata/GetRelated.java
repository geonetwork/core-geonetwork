//=============================================================================
//===    Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.guiservices.metadata;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.RelatedMetadata;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.Utils;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

/**
 * Perform a search and return all children metadata record for current record.
 *
 * In some cases, related records found : <ul> <li>could not be readable by current user.</li>
 * <li>could not be visible by current user.</li> </ul> so results depend on user privileges for
 * related records.
 *
 * Parameters: <ul> <li>type: online|thumbnail|service|dataset|parent|children|source|fcat|siblings|associated|related|null
 * (ie. all)</li> <li>from: start record</li> <li>to: end record (default 1000)</li> <li>id or uuid:
 * could be optional if call in Jeeves service forward call. In that case geonet:info/uuid is
 * used.</li> </ul>
 *
 * Relations are usually defined in records using ISO19139 or ISO19115-3 standards or profiles.
 * Therefore, some other schema plugin may also support association of resources like Dublin Core
 * using isPartOf element. In all types of association, the target document may be in a different
 * schema (eg. ISO19110 for feature catalog, Dublin core for cross reference to a document, SensorML
 * for a sensor description, ...).
 *
 * 3 types of relations may be used: <ul> <li>Relation to a metadata record stored in the metadata
 * document to be analyzed. In that case, the XML document is filtered by method defined in the
 * SchemaPlugin specific bean. eg. parent/child relation.</li> <li>Relation to specific resources
 * (eg. online source) stored in the metadata document. In that case, the XML document is filtered
 * by the schema/process/extract-relation.xsl.</li> <li>References stored in the target document.
 * eg. the link to a dataset is defined in the service metadata record. In that case, the search is
 * made in the index.</li> </ul>
 *
 * Note about each type of associations: <h3>online</h3> List of online resources (see
 * <schema>/process/extract-relations.xsl for details).
 *
 * <h3>thumbnail</h3> List of thumbnails (see <schema>/process/extract-relations.xsl for details).
 *
 * <h3>service</h3> Only apply to ISO19139, ISO19115-3 and ISO profiles. Search for all records
 * having an operatesOn element pointing to the requested metadata record UUID (see indexing to know
 * how operatesOn element is indexed).
 *
 * <h3>parent</h3> Only apply to ISO19139, ISO19115-3 and ISO profiles. Get the parentIdentifier
 * from the requested metadata record
 *
 * <h3>children</h3> Only apply to ISO19139, ISO19115-3 and ISO profiles. Search for all records
 * having an parentUuid element pointing to the requested metadata record UUID (see indexing to know
 * how parent/child relation is indexed).
 *
 *
 * <h3>dataset</h3> Only apply to ISO19139, ISO19115-3 and ISO profiles. Get all records defined in
 * operatesOn element (the current metadata is supposed to be a service metadata in that case).
 *
 * <h3>source</h3> Only apply to ISO19139, ISO19115-3 and ISO profiles. Get all records defined in
 * source element (in data quality section).
 *
 * <h3>hassource</h3> Only apply to ISO19139, ISO19115-3 and ISO profiles. Get all records where
 * this record is defined has source (in data quality section).
 *
 * <h3>fcat</h3> Only apply to ISO19110, ISO19139, ISO19115-3 and ISO profiles. Get all records
 * defined in featureCatalogueCitation.
 *
 * <h3>siblings</h3> Only apply to ISO19139, ISO19115-3 and ISO profiles. Get all aggregationInfo
 * records. This relation provides information about association type and initiative type.
 *
 * <h3>associated</h3> Only apply to ISO19139, ISO19115-3 and ISO profiles. Search for all records
 * having an agg_associated field pointing to the requested metadata record (inverse direction of
 * siblings). This relation does not inform about association type and initiative type.
 *
 * <h3>related</h3> (deprecated) Use to link ISO19110 and ISO19139 record using database table.
 *
 * @see org.fao.geonet.kernel.schema.SchemaPlugin for more details on how specific schema plugin
 * implement relations extraction.
 */
@Controller
@Qualifier("getRelated")
public class GetRelated implements Service, RelatedMetadata {

    private static int maxRecords = 1000;

    public void init(Path appPath, ServiceConfig config) throws Exception {
    }

    /**
     * @param type List of comma or "|" separated types
     */
    @RequestMapping(value = "/{portal}/{lang}/xml.relation")
    public HttpEntity<byte[]> exec(@PathVariable String lang,
                                   @RequestParam(required = false) Integer id,
                                   @RequestParam(required = false) String uuid,
                                   @RequestParam(defaultValue = "") String type,
                                   @RequestParam(defaultValue = "1") int from,
                                   @RequestParam(defaultValue = "-1") int to,
                                   boolean fast,
                                   HttpServletRequest request) throws Exception {
        if (to < 0) {
            to = maxRecords;
        }
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);
        GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);
        MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);

        final ServiceContext context = serviceManager.createServiceContext("xml.relation", lang, request);

        AbstractMetadata md;
        if (id != null) {
            md = metadataRepository.findOne(id);

            if (md == null) {
                throw new IllegalArgumentException("No Metadata found with id " + id);
            }
        } else {
            md = metadataRepository.findOneByUuid(uuid);

            if (md == null) {
                throw new IllegalArgumentException("No Metadata found with uuid " + uuid);
            }
        }
        id = md.getId();
        uuid = md.getUuid();

        Element raw = new Element("root").addContent(Arrays.asList(
            new Element("gui").addContent(Arrays.asList(
                new Element("language").setText(lang),
                new Element("url").setText(context.getBaseUrl())
            )),
            getRelated(context, id, uuid, type, from, to, fast)
        ));
        Path relatedXsl = dataDirectory.getWebappDir().resolve("xslt/services/metadata/relation.xsl");

        final Element transform = Xml.transform(raw, relatedXsl);
        final Set<String> acceptContentType = Sets.newHashSet(Iterators.forEnumeration(request.getHeaders("Accept")));

        byte[] response;
        String contentType;
        if (acceptsType(acceptContentType, "json")) {
            response = Xml.getJSON(transform).getBytes(Constants.CHARSET);
            contentType = "application/json";
        } else if (acceptContentType.isEmpty() ||
            acceptsType(acceptContentType, "xml") ||
            acceptContentType.contains("*/*") ||
            acceptContentType.contains("text/plain")) {
            response = Xml.getString(transform).getBytes(Constants.CHARSET);
            contentType = "application/xml";
        } else {
            throw new IllegalArgumentException(acceptContentType + " is not supported");
        }

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", contentType);

        return new HttpEntity<>(response, headers);
    }

    private boolean acceptsType(Set<String> acceptContentType, String toCheck) {
        for (String acceptable : acceptContentType) {
            if (acceptable.contains(toCheck)) {
                return true;
            }
        }
        return false;
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        String type = Util.getParam(params, "type", "");
        String fast = Util.getParam(params, "fast", "true");
        int from = Util.getParam(params, "from", 1);
        int to = Util.getParam(params, "to", maxRecords);


        Element info = params.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        int iId;
        String uuid;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        if (info == null) {
            String mdId = Utils.getIdentifierFromParameters(params, context);
            if (mdId == null)
                throw new MetadataNotFoundEx("Metadata not found.");

            uuid = dm.getMetadataUuid(mdId);
            if (uuid == null)
                throw new MetadataNotFoundEx("Metadata not found.");

            iId = Integer.parseInt(mdId);
        } else {
            uuid = info.getChildText(Params.UUID);
            iId = Integer.parseInt(info.getChildText(Params.ID));
        }

        return getRelated(context, iId, uuid, type, from, to, Boolean.parseBoolean(fast));
    }

    @Override
    public Element getRelated(ServiceContext context, int iId, String uuid, String type, int from_, int to_, boolean fast_) throws Exception {
        throw new RuntimeException("Not supported. Use /api/records/<uuid>/related.");
//        return MetadataUtils.getRelated(context, iId, uuid, type, from_, to_, fast_);
    }
}
