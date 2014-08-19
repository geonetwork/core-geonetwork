//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.extent;

import com.google.common.base.Functions;
import com.vividsolutions.jts.geom.Geometry;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.xlink.Processor;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.reusable.ExtentsStrategy;
import org.fao.geonet.kernel.reusable.MetadataRecord;
import org.fao.geonet.kernel.reusable.Utils;
import org.fao.geonet.services.extent.Add.Format;
import org.fao.geonet.services.extent.Source.FeatureType;
import org.fao.geonet.util.LangUtils;
import org.geotools.data.FeatureStore;
import org.geotools.util.logging.Logging;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;

import static org.fao.geonet.services.extent.ExtentHelper.DESC;
import static org.fao.geonet.services.extent.ExtentHelper.FORMAT;
import static org.fao.geonet.services.extent.ExtentHelper.GEOM;
import static org.fao.geonet.services.extent.ExtentHelper.GEO_ID;
import static org.fao.geonet.services.extent.ExtentHelper.ID;
import static org.fao.geonet.services.extent.ExtentHelper.SOURCE;
import static org.fao.geonet.services.extent.ExtentHelper.TYPENAME;

/**
 * Service for updating extent information
 * 
 * @author jeichar
 */
public class Update implements Service
{

    public void init(String appPath, ServiceConfig params) throws Exception
    {
    }

    public Element exec(Element params, ServiceContext context) throws Exception
    {

        final GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        final ExtentManager extentMan = gc.getExtentManager();

        final String id = Util.getParamText(params, ID);
        final String wfsParam = Util.getParamText(params, SOURCE);
        final String typename = Util.getParamText(params, TYPENAME);
        final String geomParam = Util.getParamText(params, GEOM);
        final String desc = LangUtils.createDescFromParams(params, DESC);
        final String geoId = LangUtils.createDescFromParams(params, GEO_ID);
        final String requestCrsCode = Util.getParamText(params, ExtentHelper.CRS_PARAM);

        final Source wfs = extentMan.getSource(wfsParam);
        final FeatureType featureType = wfs.getFeatureType(typename);
        if (featureType == null) {
            return ExtentHelper.error(typename + " does not exist, acceptable types are: " + wfs.listModifiable());
        }

        if (requestCrsCode == null) {
            return ExtentHelper.error("the " + ExtentHelper.CRS_PARAM + " parameter is required");
        }

        if (!featureType.isModifiable()) {
            return ExtentHelper.error(typename + " is not a modifiable type, modifiable types are: "
                    + wfs.listModifiable());
        }
        final FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) featureType
                .getFeatureSource();

        if (!Add.idExists(store, id, featureType)) {
            return ExtentHelper.error("The id " + id + " does not exist, perhaps you want to use the add service");
        }

        final java.util.List<Object> newValues = new ArrayList<Object>();
        final java.util.List<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>();
        final java.util.List<Element> changes = new ArrayList<Element>();

        Geometry geometry = null;
        if (geomParam != null) {
            final Format format = Format.lookup(Util.getParamText(params, FORMAT));
            final GeometryDescriptor descriptor = store.getSchema().getGeometryDescriptor();
            geometry = format.parse(geomParam);

            geometry = ExtentHelper.prepareGeometry(requestCrsCode, featureType, geometry, featureType
                    .getFeatureSource().getSchema());
            // geometry.setSRID(4326);
            newValues.add(geometry);
            attributes.add(descriptor);
            final Element change = new Element("change");
            change.setText("Attribute " + descriptor.getLocalName() + " updated to " + geomParam);
            changes.add(change);
        }

        String searchAt = "";
        if (desc != null) {

            String encodeDescription = ExtentHelper.encodeDescription(desc);
            newValues.add(encodeDescription);
            searchAt += ExtentHelper.encodeDescription(ExtentHelper.reduceDesc(desc));
            final AttributeDescriptor descriptor = store.getSchema().getDescriptor(featureType.descColumn);
            attributes.add(descriptor);

            final Element change = new Element("change");
            change.setText("Attribute " + descriptor.getLocalName() + " updated to " + desc);
            changes.add(change);

        }

        if (geoId != null) {
            String encodedGeoId = ExtentHelper.encodeDescription(geoId);
            newValues.add(encodedGeoId);

            searchAt += ExtentHelper.encodeDescription(ExtentHelper.reduceDesc(geoId));

            final AttributeDescriptor descriptor = store.getSchema().getDescriptor(featureType.geoIdColumn);
            attributes.add(descriptor);

            final Element change = new Element("change");
            change.setText("Attribute " + descriptor.getLocalName() + " updated to " + geoId);
            changes.add(change);
        }

        newValues.add(searchAt);
        final AttributeDescriptor searchDescriptor = store.getSchema().getDescriptor(featureType.searchColumn);
        attributes.add(searchDescriptor);

        if (attributes.isEmpty()) {
            return ExtentHelper.error("No updates were requested.  One or both of geom and " + DESC
                    + " must be defined for an update");
        }
        Logging.getLogger("org.geotools.data.communication").setLevel(Level.FINEST);
        store.modifyFeatures(attributes.toArray(new AttributeDescriptor[attributes.size()]), newValues.toArray(),
                featureType.createFilter(id));

        Processor.uncacheXLinkUri(ExtentsStrategy.baseHref(id,wfs.wfsId,featureType.typename));

        final Element responseElem = new Element("success");
        responseElem.setText("Updated features with id= " + id);
        responseElem.addContent(changes);

        final ExtentsStrategy strategy = new ExtentsStrategy (context.getBaseUrl(), context.getAppPath(),
                ExtentManager.getInstance(), context.getLanguage());
        ArrayList<String> fields = new ArrayList<String>();

        fields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));
        fields.addAll(Arrays.asList(strategy.getValidXlinkLuceneField()));
        final Set<MetadataRecord> referencingMetadata = Utils.getReferencingMetadata(context, strategy, fields, id, null, false,
                Functions.<String>identity());

        DataManager dm = gc.getDataManager();

        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        for (MetadataRecord metadataRecord : referencingMetadata) {
            dm.indexMetadata(dbms, metadataRecord.id, true, context, false, false, true);
        }


        return responseElem;
    }

}
