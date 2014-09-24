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

package org.fao.geonet.kernel.reusable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import jeeves.xlink.XLink;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.services.extent.Add;
import org.fao.geonet.services.extent.ExtentHelper;
import org.fao.geonet.services.extent.ExtentHelper.ExtentTypeCode;
import org.fao.geonet.services.extent.ExtentManager;
import org.fao.geonet.services.extent.Get.Format;
import org.fao.geonet.services.extent.Source;
import org.fao.geonet.services.extent.Source.FeatureType;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.util.XslUtil;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.xml.Parser;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.Within;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.apache.lucene.search.WildcardQuery.WILDCARD_STRING;
import static org.fao.geonet.kernel.reusable.Utils.addChild;
import static org.fao.geonet.kernel.reusable.Utils.gml2Conf;
import static org.fao.geonet.kernel.reusable.Utils.gml3Conf;
import static org.fao.geonet.util.LangUtils.FieldType.STRING;

public final class ExtentsStrategy extends ReplacementStrategy {

    public static final ElementFinder BBOX_FINDER = new ElementFinder("EX_GeographicBoundingBox",
            XslUtil.GMD_NAMESPACE, "geographicElement");
    public static final ElementFinder POLYGON_FINDER = new ElementFinder("EX_BoundingPolygon", XslUtil.GMD_NAMESPACE,
            "geographicElement");
    public static final ElementFinder GEO_ID_FINDER = new ElementFinder("geographicIdentifier", XslUtil.GMD_NAMESPACE,
            "*");

    public static final Envelope DEFAULT_BBOX = new Envelope(5.5, 10.5, 45.5, 48);
    public static final String NON_VALIDATED_TYPE = "gn:non_validated";
    public static final String XLINK_TYPE = "gn:xlinks";

    //private final String _baseURL;
    private final ExtentManager _extentMan;

    private final String _gmlConvertStyleSheet;
    private final String _currentLocale;
    private final String _appPath;
    private final String _flattenStyleSheet;


    public ExtentsStrategy(String baseURL, String appDir, ExtentManager extentMan, String currentLocale) {
        this._gmlConvertStyleSheet = appDir + "xsl/reusable-object-gml-convert.xsl";
        this._flattenStyleSheet = appDir + "xsl/reusable-object-snippet-flatten.xsl";
//        this._baseURL = baseURL;
        this._appPath = appDir;
        this._extentMan = extentMan;
        _currentLocale = currentLocale;
    }

    public Pair<Collection<Element>, Boolean> find(Element placeholder, Element originalElem, String defaultMetadataLang)
            throws Exception {
        if (XLink.isXLink(originalElem))
            return NULL;

        try {
            Result parts = split(originalElem, false);

            if (parts.geographic.isEmpty()) {
                return NULL;
            }

            Collection<Element> results = new ArrayList<Element>();
            for (Info geomInfo : parts.geographic) {

                Geometry geometry = geomInfo.geom;
                Format format = geomInfo.format;

                if (geometry == null) {
                    continue;
                }

                FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
                Collection<Source> wfss = _extentMan.getSources().values();
                SEARCH: for (Source wfs : wfss) {
                    Collection<FeatureType> types = wfs.getFeatureTypes();
                    for (FeatureType featureType : types) {
                        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = featureType.getFeatureSource();
                        Name geomAtt = featureSource.getSchema().getGeometryDescriptor().getName();
                        PropertyName property = filterFactory.property(geomAtt);


                        Geometry transformedGeom;
                        if(geometry.getSRID() == 21781) {
                            transformedGeom = geometry;
                        } else {
                            transformedGeom = JTS.transform(geometry, ExtentHelper.WGS84_TO_CH03);
                            transformedGeom.setSRID(21781);
                        }

                        Literal literal = filterFactory.literal(transformedGeom.convexHull().buffer(3000));
                        Within filter = filterFactory.within(property, literal);

                        final String[] properties = {featureType.idColumn, featureType.descColumn, geomAtt.getLocalPart()};
                        Query query = featureType.createQuery(filter, properties);
                        FeatureIterator<SimpleFeature> features = featureSource.getFeatures(query).features();
                        try {
                            while (features.hasNext()) {
                                SimpleFeature next = features.next();

                                final Geometry ch03LoadedGeom = (Geometry) next.getDefaultGeometry();
                                ch03LoadedGeom.setSRID(21781);

                                final Geometry transformedLoadedGeom;
                                if(geometry.getSRID() == 21781){
                                    transformedLoadedGeom = ch03LoadedGeom;
                                } else {
                                    transformedLoadedGeom = ExtentHelper.reducePrecision(
                                            JTS.transform(ch03LoadedGeom, ExtentHelper.CH03_TO_WGS84)
                                            ,ExtentHelper.COORD_DIGITS);
                                    transformedLoadedGeom.setSRID(4326);
                                }
                                if (matchingGeom(geometry, transformedLoadedGeom)
                                        || (format == Format.GMD_BBOX && matchingGeom(geometry, transformedLoadedGeom.getEnvelope()))) {
                                    geomInfo.original.detach();
                                    boolean validated = !featureType.typename.equals(NON_VALIDATED_TYPE);
                                    String idString = idAttributeToIdString(featureType, next);

                                    boolean extentTypeCode = geomInfo.inclusion == ExtentTypeCode.INCLUDE;

                                    Namespace prefix = prefix(placeholder, originalElem);
                                    results.add(xlinkIt(format, wfs, featureType, idString, validated, extentTypeCode,
                                    		new Element(originalElem.getName(),prefix)));
                                    break SEARCH;
                                }
                            }

                        } catch (NoSuchElementException e) {
                            Log.error("Reusable Objects", "Error reading feature type: " + featureType.typename);
                        } finally {
                            features.close();
                        }
                    }
                }
            }

            @SuppressWarnings("rawtypes")
            Iterator polygonIter = originalElem.getDescendants(new ElementFinder("polygon", XslUtil.GMD_NAMESPACE,
                    "EX_BoundingPolygon"));
            if (results.isEmpty()) {
                return NULL;
            } else {
                boolean finished = true;
                if (polygonIter.hasNext()) {
                    results.add(originalElem);
                    finished = false;
                }
                return Pair.read(results, finished);
            }
        } catch (NoSuchElementException e) {
            Log.error("Reusable Objects", "Unable to match: " + Xml.getString(originalElem));
        } catch (NumberFormatException e) {
            Log.error("Reusable Objects", "Unable to match: " + Xml.getString(originalElem));
        }
        return NULL;
    }

    private boolean matchingGeom(Geometry geom1, Geometry geom2) {
        if (geom1 == null || geom2 == null)
            return false;



        try {
            if(geom1.getSRID() == 21781 && geom2.getSRID() != 21781) {
                geom2 = JTS.transform(geom2,ExtentHelper.WGS84_TO_CH03);
            } else if(geom2.getSRID() == 21781 && geom1.getSRID() != 21781) {
                geom2 = JTS.transform(geom2,ExtentHelper.CH03_TO_WGS84);
            }
        } catch (Exception e) {
            return false;
        }

        // If the geoms are multigeoms with just 1 subgeom extract the subgeom
        // and compare
        // it. This handles the case where a MultiPolygon with 1 polygon might
        // == a Polygon
        // except that they will not match because they are not both polygons

        Geometry featureGeom = flatten(geom2);
        Geometry flattenedNewGeom = flatten(geom1);
        return featureGeom.buffer(ExtentHelper.tolerance()).covers(flattenedNewGeom) && flattenedNewGeom.buffer(ExtentHelper.tolerance()).covers(featureGeom);
    }

    private Geometry flatten(Geometry geometry) {
        Geometry flattened = geometry.buffer(0);
        if (geometry.getNumGeometries() == 1) {
            flattened = geometry.getGeometryN(0);
        }
        flattened.normalize();
        return flattened;
    }

    private Namespace prefix(Element placeholder, Element originalElement) {
        Element parentElement = placeholder.getParentElement();

        if (parentElement.getName().contains("SV_ServiceIdentification")) {
            return XslUtil.SRV_NAMESPACE;
        } else {
            return originalElement.getNamespace();
        }
    }

    private String idAttributeToIdString(FeatureType featureType, SimpleFeature feature) {

        final Object attribute = feature.getAttribute(featureType.idColumn);
        if (attribute instanceof Number) {
            Number id = (Number) attribute;
            String idString = String.valueOf(id.intValue());
            return idString;
        } else if (attribute instanceof String) {
            return attribute.toString();
        } else {
            throw new IllegalArgumentException("Id must be string or number.  Number is preferred");
        }
    }

    @SuppressWarnings("rawtypes")
    public Collection<Element> add(Element placeholder, Element originalElem, Dbms dbms, String metadataLang)
            throws Exception {
        Result extents = split(originalElem, false);

        if (extents.geographic.isEmpty()) {
            return Collections.emptySet();
        }

        String description = findDesc(originalElem, metadataLang, _appPath);

        Collection<Element> results = new ArrayList<Element>();
        Namespace prefix = prefix(placeholder, originalElem);

        List<Geometry> distinctGeoms = new ArrayList<Geometry>();

        for (Info e : extents.geographic) {
            results.addAll(parseAndCreateReusable(extents, e, dbms, metadataLang, description, prefix, originalElem, distinctGeoms));
        }

        if (results.isEmpty()) {
            Geometry geom = new GeometryFactory().toGeometry(DEFAULT_BBOX);
            Iterator polygons = originalElem.getDescendants(POLYGON_FINDER);
            Format format = polygons.hasNext() ? Format.GMD_COMPLETE : Format.GMD_BBOX;
            if (polygons.hasNext())
                ((Content) polygons.next()).detach();
            else {
                Iterator bbox = originalElem.getDescendants(BBOX_FINDER);
                if (bbox.hasNext())
                    ((Content) bbox.next()).detach();
            }
            results.addAll(createReusable(extents, new Info(originalElem, (Element) originalElem.clone()),
                    metadataLang, description, geom, format, prefix, originalElem));
        }

        // if extent.other is not empty add originalElement so that
        // the non-geographic extents are not lost
        if (!extents.other.isEmpty())
            results.add(originalElem);

        return results;
    }

    /**
     *
     * @param extents
     *            All extents being added
     * @param e
     *            the extent to be added by this call
     * @param originalElem 
     * @param distinctGeoms
     *            collection that contains all geoms added before e. This method
     *            will add to this collection
     */
    private Collection<Element> parseAndCreateReusable(Result extents, Info e, Dbms dbms, String metadataLang,
            String desc, Namespace prefix, Element originalElem, List<Geometry> distinctGeoms) throws Exception {
        Geometry geometries = e.geom;
        Format format = e.format;

        if (geometries == null) {
            return Collections.emptySet();
        }

        for (Geometry g : distinctGeoms) {
            if (matchingGeom(g, geometries)) {
                return Collections.emptySet();
            }
        }

        distinctGeoms.add(geometries);

        return createReusable(extents, e, metadataLang, desc, geometries, format, prefix, originalElem);
    }

    private Collection<Element> createReusable(Result extents, Info e, String metadataLang, String desc, Geometry geom,
            Format format, Namespace prefix, Element originalElem) throws Exception {

        e.original.detach();
        String geoId = findGeoId(extents, e, metadataLang);

        boolean extentTypeCode = e.inclusion == ExtentTypeCode.INCLUDE;

        Source wfs = _extentMan.getSource();
        FeatureType featureType = wfs.getFeatureType(NON_VALIDATED_TYPE);
        FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) featureType
                .getFeatureSource();

        String id = new Add().add(null, geoId, desc, "WGS84(DD)", featureType, store, geom, e.showNative);

        return Collections.singleton(xlinkIt(format, wfs, featureType, id, false, extentTypeCode, new Element(originalElem.getName(), prefix)));
    }

    private String findGeoId(Result extents, Info e, String metadataLang) throws Exception {
        int index = extents.all.indexOf(e);

        int distance = -1;
        Info geoIdElem = null;
        for (Info geoId : extents.geoId) {
            int gIndex = extents.all.indexOf(geoId);
            int newDist = index - gIndex;
            if (distance < 0 || newDist > -1 && distance > newDist) {
                geoIdElem = geoId;
                distance = newDist;
            }
        }
        if (geoIdElem == null && !extents.geoId.isEmpty()) {
            geoIdElem = extents.geoId.get(extents.geoId.size() - 1);
        } else if (geoIdElem == null) {
            geoIdElem = e;
        }

        Element xml = Xml.transform((Element) geoIdElem.copy.clone(), _flattenStyleSheet);
        String geoId = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("geoId"), STRING);

        return geoId == null? "" : geoId;
    }

    /**
     * Splits the elements into geographic and non-geographic extents
     * (description are ignored).
     *
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Result split(Element originalElem, boolean detach) throws Exception {
        Result result = new Result();

        List<Element> exExtents = new ArrayList<Element>(originalElem.getChildren());

        for (Element exExtent : exExtents) {
            Iterator bboxIter = originalElem.getDescendants(BBOX_FINDER);
            Iterator polygonIter = originalElem.getDescendants(POLYGON_FINDER);

            ArrayList<Info> infos = new ArrayList<Info>();

            Iterator geoIdIter = exExtent.getDescendants(GEO_ID_FINDER);
            Element geoId = null;
            if(geoIdIter.hasNext()) {
                geoId = (Element) geoIdIter.next();
            }
            if(!polygonIter.hasNext() && bboxIter.hasNext()) {
                // BBoxes are exploded into multiple reusable objects
                while(bboxIter.hasNext()) {
                    Element toAdd = wrapWithExtentParentElems((Element) bboxIter.next());
                    if(geoId != null) {
                        toAdd.addContent((Content) geoId.clone());
                    }
                    Info info = new Info(exExtent, toAdd);
                    infos.add(info);
                }
            } else {
                // Polygons are merged together into one reusable object
                Element toAdd = wrapWithExtentParentElems(exExtent);
                if(geoId != null && !toAdd.getDescendants(GEO_ID_FINDER).hasNext()) {
                    toAdd.addContent((Content) geoId.clone());
                }
                Info info = new Info(exExtent, toAdd);
                infos.add(info);
            }
            
            for (Info info : infos) {
                if (info.format != null) {
                    if (detach) {
                        exExtent.detach();
                    }
                    
                    result.all.add(info);
                    result.geographic.add(info);
                } else if (exExtent.getName().equals("description")) {
                    // ignore desc
                } else if (info.original.getDescendants(GEO_ID_FINDER).hasNext()) {
                    result.all.add(info);
                    result.geoId.add(info);
                } else {
                    result.all.add(info);
                    result.other.add(info);
                }
            }
        }



        result.removeDuplicates();

        return result;
    }

    private Element wrapWithExtentParentElems(Element elem) {
        Element exExtent;
        if(elem.getName() == "EX_Extent") {
            exExtent = (Element) elem.clone();
        } else {
            exExtent = new Element("EX_Extent",XslUtil.GMD_NAMESPACE);
            
            Element geographicElem = new Element("geographicElement", XslUtil.GMD_NAMESPACE);
            geographicElem.addContent((Element) elem.clone());
            exExtent.addContent(geographicElem);
        }
        Element extent = new Element("extent", XslUtil.GMD_NAMESPACE);

        extent.addContent(exExtent);

        return extent;

    }

    public Element find(UserSession session, boolean validated) throws Exception {
        FeatureType featureType;
        if (validated) {
            featureType = _extentMan.getSource().getFeatureType(XLINK_TYPE);
        } else {
            featureType = _extentMan.getSource().getFeatureType(NON_VALIDATED_TYPE);
        }

        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = featureType.getFeatureSource();

        String[] properties = { featureType.idColumn, featureType.descColumn, featureType.geoIdColumn };
        Query query = featureType.createQuery(properties);
        FeatureIterator<SimpleFeature> features = featureSource.getFeatures(query).features();

        try {
            Element extents = new Element(REPORT_ROOT);
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                Element e = new Element(REPORT_ELEMENT);

                String id = idAttributeToIdString(featureType, feature);
                String typeName = validated ? XLINK_TYPE : NON_VALIDATED_TYPE;
                String url = XLink.LOCAL_PROTOCOL+"extent.edit?closeOnSave&crs=EPSG:21781&wfs=default&typename="
                        + typeName + "&id=" + id;

                addChild(e, REPORT_URL, url);
                addChild(e, REPORT_ID, id);
                addChild(e, REPORT_TYPE, "extent");
                addChild(e, REPORT_XLINK, createXlinkHref(id, session, featureType.typename) + "*");

                Object att = feature.getAttribute(featureType.descColumn);
                String desc = "No description";

                if (att != null) {
                    String descAt = ExtentHelper.decodeDescription(att.toString());
                    desc = LangUtils.getTranslation(descAt, _currentLocale);
                }

                att = feature.getAttribute(featureType.geoIdColumn);
                if (att != null) {
                    String geoIdAt = ExtentHelper.decodeDescription(att.toString());
                    String geoId = LangUtils.getTranslation(geoIdAt, _currentLocale);
                    desc = desc + " &lt;" + geoId + "&gt;";
                }
                addChild(e, REPORT_DESC, desc);
                addChild(e, REPORT_SEARCH, id+desc);

                extents.addContent(e);
            }
            return extents;
        } finally {
            features.close();
        }

    }

    public void performDelete(String[] ids, Dbms dbms, UserSession session, String featureTypeName) throws Exception {

        FeatureType from;
        from = findFeatureType(featureTypeName);
        FeatureStore<SimpleFeatureType, SimpleFeature> fromSource = (FeatureStore<SimpleFeatureType, SimpleFeature>) from
                .getFeatureSource();

        fromSource.removeFeatures(createFilter(ids, from));
    }

    private FeatureType findFeatureType(String featureTypeName) {
        FeatureType from;
        if (featureTypeName == null) {
            from = _extentMan.getSource().getFeatureType(NON_VALIDATED_TYPE);
        } else {
            from = _extentMan.getSource().getFeatureType(featureTypeName);
        }
        return from;
    }

    public String createXlinkHref(String id, UserSession session, String featureTypeName) {
        Source wfs = _extentMan.getSource();
        FeatureType featureType = findFeatureType(featureTypeName);

        return baseHref(id, wfs.wfsId, featureType.typename) + "&";
    }

    public String updateHrefId(String oldHref, String id, UserSession session) {
        String xlinkhref = oldHref.replace(NON_VALIDATED_TYPE, XLINK_TYPE);
        String updatedId = xlinkhref.replaceFirst("([?&])id=[^&]+", "$1id=" + id);
        return updatedId;
    }

    public Map<String, String> markAsValidated(String[] ids, Dbms dbms, UserSession session) throws Exception {
        FeatureType dest = _extentMan.getSource().getFeatureType(XLINK_TYPE);

        FeatureType from = _extentMan.getSource().getFeatureType(NON_VALIDATED_TYPE);
        FeatureStore<SimpleFeatureType, SimpleFeature> fromSource = (FeatureStore<SimpleFeatureType, SimpleFeature>) from
                .getFeatureSource();
        FeatureStore<SimpleFeatureType, SimpleFeature> destSource = (FeatureStore<SimpleFeatureType, SimpleFeature>) dest
                .getFeatureSource();

        org.opengis.filter.Filter filter = createFilter(ids, from);
        Query query = new Query(from.pgTypeName, filter);
        FeatureIterator<SimpleFeature> features = fromSource.getFeatures(query).features();
        Map<String, String> idMap = new HashMap<String, String>();

        try {
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = FeatureCollections.newCollection();

            int base = Integer.valueOf(ExtentHelper.findNextId(destSource, dest));
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                SimpleFeature newFeature = SimpleFeatureBuilder.template(destSource.getSchema(), null);

                Number id = (Number) feature.getAttribute(from.idColumn);

                Object desc = decodeEncode(from.descColumn, feature);
                Object search = decodeEncode(from.searchColumn, feature);
                Object geoId = decodeEncode(from.geoIdColumn, feature);
                Object geom = feature.getDefaultGeometry();

                String newId = String.valueOf(base++);
                idMap.put(String.valueOf(id.intValue()), newId);

                newFeature.setAttribute(dest.idColumn, newId);
                newFeature.setAttribute(dest.descColumn, desc);
                newFeature.setAttribute(dest.geoIdColumn, geoId);
                newFeature.setAttribute(dest.searchColumn, search);
                newFeature.setAttribute(dest.showNativeColumn, feature.getAttribute(dest.showNativeColumn));
                newFeature.setDefaultGeometry(geom);

                featureCollection.add(newFeature);

                Element e = new Element("id");
                e.setText(feature.getAttribute(from.idColumn).toString());
            }

            destSource.addFeatures(featureCollection);
            fromSource.removeFeatures(filter);

        } finally {
            features.close();
        }

        return idMap;
    }

    private String decodeEncode(String column, SimpleFeature feature) {
        Object attribute = feature.getAttribute(column);
        if (attribute == null)
            return "";
        String decoded = ExtentHelper.decodeDescription(attribute.toString());
        return ExtentHelper.encodeDescription(decoded);
    }

    private org.opengis.filter.Filter createFilter(String[] ids, FeatureType from) {
        FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        List<org.opengis.filter.Filter> idFilters = new ArrayList<org.opengis.filter.Filter>();
        for (String id : ids) {
            PropertyIsEqualTo equals = filterFactory.equals(filterFactory.property(from.idColumn),
                    filterFactory.literal(id));
            idFilters.add(equals);
        }

        org.opengis.filter.Filter filter = filterFactory.or(idFilters);
        return filter;
    }

    public static String baseHref(String id, String wfs, String typename) {
        return  MessageFormat.format(XLink.LOCAL_PROTOCOL+"xml.extent.get?id={0}&wfs={1}&typename={2}",id,wfs,typename);
    }
    private Element xlinkIt(Format format, Source wfs, FeatureType featureType, String id, boolean validated,
            boolean include, Element xlinkElement) {
        String xlink = baseHref(id,wfs.wfsId, featureType.typename)+"&format="+format+"&extentTypeCode="+String.valueOf(include);

        xlinkElement.removeContent();
        xlinkElement.setAttribute(XLink.HREF, xlink, XLink.NAMESPACE_XLINK);

        if (!validated) {
        	xlinkElement.setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE, XLink.NAMESPACE_XLINK);
        }
        xlinkElement.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        return xlinkElement;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Extent parseGeometry (Element originalElem) throws Exception {
        Iterator bboxIter = originalElem.getDescendants(BBOX_FINDER);
        Iterator polygonIter = originalElem.getDescendants(POLYGON_FINDER);
        Multimap<Boolean, Polygon> results = ArrayListMultimap.create();

        GeometryFactory fac = new GeometryFactory();
        Format format;

        if (polygonIter.hasNext()) {
            format = Format.GMD_COMPLETE;
        }
         /*if (polygonIter.hasNext() && bboxIter.hasNext()) {
            format = Format.GMD_COMPLETE;
        }
            CHTopo doesn't currently want polygons so we will only have complete and bbox
            else if (polygonIter.hasNext()) {
            format = Format.GMD_POLYGON;
        } */else if(bboxIter.hasNext()) {
            format = Format.GMD_BBOX;
        } else {
            format = null;
        }

        while (polygonIter.hasNext()) {
            Element polygonBBoxElem = (Element) polygonIter.next();
            Element polygonElem = polygonBBoxElem.getChild("polygon", XslUtil.GMD_NAMESPACE);
            if (polygonElem == null) {
                continue;
            }
            Element geomElemBeforeTransform = Utils.nextElement(polygonElem.getChildren().iterator());
            Element geomElem = Xml.transform((Element) geomElemBeforeTransform.clone(), _gmlConvertStyleSheet);
            String geomXML = Xml.getString(geomElem);
            Parser parser = null;
            // Element geographicElement = geographicElement(polygonElem);
            Geometry geometry = null;
            try {
                parser = new Parser(gml3Conf);
                parser.setValidating(true);
                parser.setStrict(false);
                geometry = (Geometry) parser.parse(new StringReader(geomXML));
            } catch (Exception e) {
                try {
                    parser = new Parser(gml2Conf);
                    parser.setValidating(true);
                    parser.setStrict(false);
                    geometry = (Geometry) parser.parse(new StringReader(geomXML));
                } catch (Exception e2) {
                    StringBuilder builder = new StringBuilder("GML is not legal: \n" + geomXML);
                    builder.append("\n");
                    int i = 0;
                    for (Object error : parser.getValidationErrors()) {
                        i++;
                        builder.append(i);
                        builder.append(": ");
                        builder.append(error);
                        builder.append("\n");
                    }

                    Log.error("reusableObjectReplace", builder.toString());

                }
            }
            
            if(geometry != null) {
                geometry.setSRID(4326);
                ExtentHelper.reducePrecision(geometry, ExtentHelper.COORD_DIGITS);
                if (geometry instanceof Polygon) {
                    results.put(inclusion(polygonBBoxElem),(Polygon) geometry);
                } else if (geometry instanceof MultiPolygon) {
                    boolean inclusion = inclusion(polygonBBoxElem);
                    MultiPolygon mp = (MultiPolygon) geometry;
                    for (int i = 0; i < mp.getNumGeometries(); i++) {
                        Polygon poly = (Polygon) mp.getGeometryN(i);
                        results.put(inclusion,poly);
                    }
                }
            }
        }

        boolean showNative = false;

        if (results.isEmpty()) {
            while(bboxIter.hasNext()) {
                Element bboxElem = (Element) bboxIter.next();

                int srid = ExtentHelper.bboxSrid(bboxElem);

                showNative = srid == 21781;

                double minx = ExtentHelper.decimal(bboxElem.getChild("westBoundLongitude", XslUtil.GMD_NAMESPACE));
                double maxx = ExtentHelper.decimal(bboxElem.getChild("eastBoundLongitude", XslUtil.GMD_NAMESPACE));
                double miny = ExtentHelper.decimal(bboxElem.getChild("southBoundLatitude", XslUtil.GMD_NAMESPACE));
                double maxy = ExtentHelper.decimal(bboxElem.getChild("northBoundLatitude", XslUtil.GMD_NAMESPACE));

                Envelope env = new Envelope(minx, maxx, miny, maxy);
    
                Geometry envGeom = fac.toGeometry(env);
                envGeom.setSRID(srid);
                results.put(inclusion(bboxElem),(Polygon) envGeom);

            }
        }

        MultiPolygon inclusion = null;
        if (!results.get(true).isEmpty()) {
            inclusion = ExtentHelper.joinPolygons(fac, results.get(true));
        }
        MultiPolygon exclusion = null;
        if (!results.get(false).isEmpty()) {
            exclusion = ExtentHelper.joinPolygons(fac, results.get(false));
        }

        Extent result;
        if(inclusion == null && exclusion == null) {
            result = new Extent(ExtentTypeCode.INCLUDE, null, format, showNative);
        }else if (inclusion == null && exclusion != null) {
            result = new Extent(ExtentTypeCode.EXCLUDE, exclusion,format, showNative);
        } else if (inclusion != null && exclusion == null) {
            result = new Extent(ExtentTypeCode.INCLUDE,inclusion,format, showNative);
        } else {
            Pair<ExtentTypeCode, MultiPolygon> diff = ExtentHelper.diff(fac, inclusion, exclusion);
            result = new Extent(diff.one(), diff.two(),format, showNative);
        }
        
        return result;
    }


    @SuppressWarnings("rawtypes")
    private String findDesc(Element originalElem, String metadataLang, String appPath) throws Exception {
        Element descElem = null;
        Iterator idIter = originalElem.getDescendants(new ElementFinder("description", XslUtil.GMD_NAMESPACE, "*"));

        if (idIter.hasNext()) {
            Element id = (Element) idIter.next();
            descElem = id;
        } else {
            idIter = originalElem.getDescendants(new ElementFinder("MD_Identifier", XslUtil.GMD_NAMESPACE,
                    "geographicIdentifier"));
            if (idIter.hasNext()) {
                Element id = (Element) idIter.next();
                idIter = id.getDescendants(new ElementFinder("CharacterString", XslUtil.GCO_NAMESPACE, "code"));
                if (idIter.hasNext()) {
                    id = (Element) idIter.next();
                    descElem = id.getParentElement();
                }
            }
        }

        String desc;

        if (descElem != null) {
            desc = LangUtils.toInternalMultilingual(metadataLang, appPath, descElem, STRING);
        } else {
            desc = null;
        }

        if (desc == null) {
            Iterator iter = originalElem.getDescendants(new Filter() {

                private static final long serialVersionUID = 6963411494396760932L;

                public boolean matches(Object arg0) {
                    if (arg0 instanceof Element) {
                        return gmlId(arg0) != null;
                    }
                    return false;
                }

            });

            if (iter.hasNext()) {
                desc = gmlId(iter.next());
            }
        }

        if (desc == null) {
            desc = UUID.randomUUID().toString();
        }

        return desc;
    }

    private boolean inclusion(Element polygonBBoxElem) {
        Element code = polygonBBoxElem.getChild("extentTypeCode", XslUtil.GMD_NAMESPACE);
        if(code == null) {
            return true;
        }
        String bool = code.getChildTextNormalize("Boolean", XslUtil.GCO_NAMESPACE);
        
        return bool == null || !(bool.equalsIgnoreCase("false") || bool.equalsIgnoreCase("0"));
    }

    private String gmlId(Object arg0) {
        Element e = (Element) arg0;
        Attribute gmlId = e.getAttribute("id", Namespace.getNamespace("gml", "http://www.opengis.net/gml"));
        if (gmlId != null && gmlId.getValue().trim().length() > 0) {
            return gmlId.getValue().trim();
        }
        gmlId = e.getAttribute("id");
        if (gmlId != null && gmlId.getValue().trim().length() > 0) {
            return gmlId.getValue().trim();
        }
        return null;
    }

    public Collection<Element> updateObject(Element xlink, Dbms dbms, String metadataLang) throws Exception {
        Extent parseResult = parseGeometry(xlink);

        MultiPolygon geom = parseResult.geom;

        if (geom == null) {
            return Collections.emptySet();
        }
        
        String desc = ExtentHelper.encodeDescription(findDesc(xlink, metadataLang, _appPath));
        String id = Utils.extractUrlParam(xlink, "id");
        String typename = Utils.extractUrlParam(xlink, "typename");

        Element xml = Xml.transform((Element) xlink.clone(), _flattenStyleSheet);

        String geoId = ExtentHelper.encodeDescription(LangUtils.toInternalMultilingual(metadataLang, _appPath,
                xml.getChild("geoId"), STRING));

        String extentTypeCode = parseResult.inclusion == ExtentTypeCode.INCLUDE ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
        extentTypeCode = extentTypeCode.toLowerCase();

        String showNative = parseResult.showNative?"y":"n";

        String href = xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);
        href = href.substring(0, href.indexOf("extentTypeCode=")) + "extentTypeCode=" + extentTypeCode;
        xlink.setAttribute(XLink.HREF, href, XLink.NAMESPACE_XLINK);

        Source wfs = _extentMan.getSource();
        FeatureType featureType = wfs.getFeatureType(typename);
        FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) featureType
                .getFeatureSource();

        AttributeDescriptor[] attributes = new AttributeDescriptor[] {
                store.getSchema().getDescriptor(featureType.descColumn),
                store.getSchema().getDescriptor(featureType.searchColumn),
                store.getSchema().getDescriptor(featureType.geoIdColumn),
                store.getSchema().getDescriptor(featureType.showNativeColumn),
                store.getSchema().getGeometryDescriptor() };
        String search = ExtentHelper.encodeDescription(ExtentHelper.reduceDesc(desc) + ExtentHelper.reduceDesc(geoId));
        Object[] values = new Object[] { desc, search, geoId, showNative, null };

        String srid = geom.getSRID() == 21781?"EPSG:21781":"WGS84(DD)";

        values[4] = ExtentHelper.prepareGeometry(srid, featureType, geom, store.getSchema());
        org.opengis.filter.Filter filter = createFilter(new String[] { id }, featureType);
        store.modifyFeatures(attributes, values, filter);

        Result extent = split(xlink, false);

        if (!extent.other.isEmpty()) {
            return elems(extent.other);
        }
        return Collections.emptySet();
    }

    private Collection<Element> elems(List<Info> other) {
        Collection<Element> e = new ArrayList<Element>();
        for (Info info : other) {
            e.add(info.copy);
        }
        return e;
    }

    public boolean isValidated(Dbms dbms, String href) {
        return !href.contains("typename=" + NON_VALIDATED_TYPE);
    }

    @Override
    public String toString() {
        return "Reusable Extent";
    }

    class Info {
        /**
         * Not the full extent object. The element that is the basis for the
         * copy. A Detach will remove it from the 'real' metadata
         */
        final Element original;
        /** A full extent object that contains the copy of the original */
        final Element copy;

        final Geometry geom;
        final Format format;
        final ExtentTypeCode inclusion;
        final boolean showNative;

        public Info(Element original, Element copy) throws Exception {
            super();
            this.original = original;
            this.copy = copy;
            Extent parseResult;
            try {
                parseResult = parseGeometry(this.copy);
            } catch (NumberFormatException err) {
                GeometryFactory fac = new GeometryFactory();
                MultiPolygon result = fac.createMultiPolygon(new Polygon[]{(Polygon) fac.toGeometry(DEFAULT_BBOX)});
                parseResult = new Extent(ExtentTypeCode.INCLUDE, result, Format.GMD_BBOX, false);
            }

            geom = parseResult.geom;
            this.showNative = parseResult.showNative;

            this.format = parseResult.format;
            this.inclusion = parseResult.inclusion;
        }
    }

    class Result {
        final List<Info> all = new ArrayList<Info>();
        final List<Info> geographic = new ArrayList<Info>();
        final List<Info> geoId = new ArrayList<Info>();
        final List<Info> other = new ArrayList<Info>();

        @Override
        public String toString() {
            return "Result [geoId=" + geoId + ", geographic=" + geographic + ", other=" + other + "]";
        }

        public void removeDuplicates() {
            ArrayList<Info> copy = new ArrayList<Info>(geographic);
            Collections.sort(copy, new Comparator<Info>() {
                public int compare(Info o1, Info o2) {

                    int result1 = assignValue(o1.format);
                    int result2 = assignValue(o1.format);
                    return result1 - result2;
                }

                private int assignValue(Format format) {
                    switch (format) {
                    case GMD_BBOX:
                        return 0;
                    default:
                        return 1;
                    }
                }
            });

            OUTER: for (int i = 0; i < copy.size(); i++) {
                Info current = copy.get(i);
                for (int j = 0; j < i; j++) {
                    Info previous = copy.get(j);
                    if (current.inclusion != previous.inclusion) {
                        continue;
                    }

                    if (matchingGeom(previous.geom, current.geom)) {
                        remove(current);
                        continue OUTER;
                    }

                    if (matchingBBOX(previous, current)) {
                        remove(current);
                        continue OUTER;
                    }
                }
            }
        }

        private boolean matchingBBOX(Info previous, Info current) {
            final Geometry prevEnv = previous.geom.getEnvelope();
            prevEnv.setSRID(previous.geom.getSRID());
            final Geometry currEnv = current.geom.getEnvelope();
            currEnv.setSRID(current.geom.getSRID());

            return current.format == Format.GMD_BBOX && current.geom != null && previous.geom != null
                    && matchingGeom(prevEnv, currEnv);
        }

        private void remove(Info info) {
            all.remove(info);
            geographic.remove(info);
        }

    }

    private static final class Extent {
        final ExtentTypeCode inclusion;
        final MultiPolygon geom;
        final Format format;
        final boolean showNative;

        public Extent(ExtentTypeCode inclusion, MultiPolygon geom, Format format, boolean showNative) {
            this.inclusion = inclusion;
            this.geom = geom;
            this.format = format;
            this.showNative = showNative;
        }
    }

    @Override
    public String[] getInvalidXlinkLuceneField() {
        return new String[]{"invalid_xlink_extent"};
    }
    
    @Override
    public String[] getValidXlinkLuceneField() {
    	return new String[]{"valid_xlink_extent"};
    }

    @Override
    public String createAsNeeded(String href, UserSession session) throws Exception {

       String startId = Utils.id(href);
        if(startId==null) return href;

        try {
            Double.parseDouble(startId);

            // assume id exists
            return href;
        } catch (NumberFormatException e) {
            Source wfs = _extentMan.getSource();
            FeatureType featureType = wfs.getFeatureType(NON_VALIDATED_TYPE);
            FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) featureType
                    .getFeatureSource();

            Geometry geom = new GeometryFactory().toGeometry(DEFAULT_BBOX);
            String id = new Add().add(null, "", "", "WGS84(DD)", featureType, store, geom, false);

            return href.replaceFirst("&id=[^&]+", "&id=" + id);
        }
    }

    @Override
    public org.apache.lucene.search.Query createFindMetadataQuery(String field, String concreteId, boolean isValidated) {
        String typename = isValidated ? XLINK_TYPE : NON_VALIDATED_TYPE;
        WildcardQuery query = new WildcardQuery(new Term(field, WILDCARD_STRING + "id=" + concreteId + WILDCARD_STRING +
                                                                  "typename=" + typename + WILDCARD_STRING));
        WildcardQuery query2 = new WildcardQuery(new Term(field, WILDCARD_STRING + "typename=" + typename + WILDCARD_STRING +
                                                                 "id=" + concreteId + WILDCARD_STRING));

        final BooleanQuery finalQuery = new BooleanQuery();
        finalQuery.add(query, BooleanClause.Occur.SHOULD);
        finalQuery.add(query2, BooleanClause.Occur.SHOULD);
        return finalQuery;
    }
}
