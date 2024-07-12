//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.component.csw;

import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.AbstractOperation;
import org.fao.geonet.kernel.csw.services.getrecords.SearchController;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

//=============================================================================

/**
 * TODO OGC 07045: - TYPENAME - Zero or one (Optional) Default action is to describe all types known
 * to server - Optional. Must support “gmd:MD_Metadata”.
 */
@Component(CatalogService.BEAN_PREFIX + GetRecordById.NAME)
public class GetRecordById extends AbstractOperation implements CatalogService {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    static final String NAME = "GetRecordById";

    @Autowired
    private SearchController searchController;
    @Autowired
    private CatalogConfiguration catalogConfig;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    public GetRecordById(ApplicationContext applicationContext) {

    }


    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public String getName() {
        return NAME;
    }

    //---------------------------------------------------------------------------

    public Element execute(Element request, ServiceContext context) throws CatalogException {
        checkService(request);
        checkVersion(request);
        //-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
        checkOutputFormat(request);
        String outSchema = OutputSchema.parse(request.getAttributeValue("outputSchema"), schemaManager);
        //--------------------------------------------------------

        ElementSetName setName = getElementSetName(request, ElementSetName.SUMMARY);

        Element response = new Element(getName() + "Response", Csw.NAMESPACE_CSW);

        @SuppressWarnings("unchecked")
        Iterator<Element> ids = request.getChildren("Id", Csw.NAMESPACE_CSW).iterator();

        if (!ids.hasNext())
            throw new MissingParameterValueEx("id");

        try {
            while (ids.hasNext()) {
                String uuid = ids.next().getText();
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                String id = gc.getBean(DataManager.class).getMetadataId(uuid);

                // Metadata not found, search for next ids
                if (id == null)
                    continue;
                //throw new InvalidParameterValueEx("uuid", "Can't find metadata with uuid "+uuid);

                // Check if the current user has access
                // to the requested MD
                Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

                final String displayLanguage = context.getLanguage();
                Element md = searchController.retrieveMetadata(context, id, setName, outSchema, null, null, ResultType.RESULTS,null,
                    displayLanguage, true);

                if (md != null) {
                    final Map<String, GetRecordByIdMetadataTransformer> transformers = context.getApplicationContext()
                        .getBeansOfType(GetRecordByIdMetadataTransformer.class);
                    for (GetRecordByIdMetadataTransformer transformer : transformers.values()) {
                        final Optional<Element> transformedMd = transformer.apply(context, md, outSchema);
                        if (transformedMd.isPresent()) {
                            md = transformedMd.get();
                        }
                    }

                    response.addContent(md);

                    if (catalogConfig.isIncreasePopularity()) {
                        gc.getBean(DataManager.class).increasePopularity(context, id);
                    }
                }
            }
        } catch (Exception e) {
            context.error("Raised : " + e);
            context.error(" (C) Stacktrace is\n" + Util.getStackTrace(e));
            throw new NoApplicableCodeEx(e.toString());
        }
        return response;
    }

    //---------------------------------------------------------------------------

    public Element adaptGetRequest(Map<String, String> params) {
        String service = params.get("service");
        String version = params.get("version");
        String elemSetName = params.get("elementsetname");
        String ids = params.get("id");

        //-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
        String outputFormat = params.get("outputformat");
        String outputSchema = params.get("outputschema");
        //--------------------------------------------------------

        Element request = new Element(getName(), Csw.NAMESPACE_CSW);

        setAttrib(request, "service", service);
        setAttrib(request, "version", version);

        //-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
        setAttrib(request, "outputFormat", outputFormat);
        setAttrib(request, "outputSchema", outputSchema);
        //--------------------------------------------------------

        fill(request, "Id", ids);

        addElement(request, "ElementSetName", elemSetName);

        return request;
    }

    //---------------------------------------------------------------------------

    public Element retrieveValues(String parameterName) throws CatalogException {
        // TODO
        return null;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    //-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
    private void checkOutputFormat(Element request) throws InvalidParameterValueEx {
        String format = request.getAttributeValue("outputFormat");

        if (format == null)
            return;

        if (!format.equals("application/xml"))
            throw new InvalidParameterValueEx("outputFormat", format);
    }
}
