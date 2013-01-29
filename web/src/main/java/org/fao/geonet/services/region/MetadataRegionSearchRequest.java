package org.fao.geonet.services.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.services.Utils;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.jdom.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class MetadataRegionSearchRequest extends Request {

    private String id;
    private String label;
    private ServiceContext context;
    private final Parser parser;
    private GeometryFactory factory;

    public MetadataRegionSearchRequest(ServiceContext context, Parser parser, GeometryFactory factory) {
        this.context = context;
        this.parser = parser;
        this.factory = factory;
    }

    @Override
    public Request label(String labelParam) {
        this.label=labelParam;
        return this;
    }

    @Override
    public Request categoryId(String categoryIdParam) {
        return this;
    }

    @Override
    public Request maxRecords(int maxRecordsParam) {
        return this;
    }

    @Override
    public Collection<Region> execute() throws Exception {
        if(label==null && id==null || (id!=null && !id.startsWith("metadata:")) ) {
            return Collections.emptySet();
        }
        List<Region> regions = new ArrayList<Region>();
        if(label != null) {
            loadAll(regions, label);
        } else if(id != null) {
            String [] parts = id.split(":", 3);
            String label = parts[1];
            String id = null;
            if(parts.length > 2) {
                id = parts[2];
                loadOnly(regions, label, id);
            } else {
                loadAll(regions, label);
            }
            if(regions.size()>1) {
                regions = Collections.singletonList(regions.get(0));
            }
        }
        return regions;
    }

    private void loadOnly(List<Region> regions, String fileId, String id) throws Exception {
        Element metadata = findMetadata(fileId);
        Iterator<?> iter = metadata.getDescendants();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof Element) {
                Element el = (Element) obj;
                Element geonet = el.getChild("element", Geonet.Namespaces.GEONET);
                if(geonet!= null && id.equals(geonet.getAttributeValue("ref"))) {
                    Iterator<?> extent = descentOrSelf(el);
                    if (extent.hasNext()) {
                        regions.add(parseRegion(fileId, (Element) extent.next()));
                        return;
                    }
                }
            }
        }
    }

    private void loadAll(List<Region> regions, String fileId) throws Exception {
        Element metadata = findMetadata(fileId);
        Iterator<?> extents = null;
        extents = descentOrSelf(metadata);
        while(extents.hasNext()) {
            Object object = extents.next();
            if(object instanceof Element) {
                regions.add(parseRegion(fileId, (Element) object));
            }
        }
    }

    private Iterator<?> descentOrSelf(Element metadata) {
        Iterator<?> extents;
        if(EXTENT_FINDER.matches(metadata)) {
            extents = Collections.singletonList(metadata).iterator();
        } else {
            extents = metadata.getDescendants(EXTENT_FINDER);
        }
        return extents;
    }

    private Region parseRegion(String fileId, Element extentObj) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        gc.getDataManager().getEditLib().removeEditingInfo(extentObj);

        String id = null;
        Geometry geometry = null;
        if ("polygon".equals(extentObj.getName())) {
            String gml = Xml.getString(extentObj);
            geometry = SpatialIndexWriter.parseGml(parser, gml);
        } else if ("EX_BoundingPolygon".equals(extentObj.getName())) {
            String gml = Xml.getString(extentObj.getChild("polygon", Geonet.Namespaces.GMD));
            geometry = SpatialIndexWriter.parseGml(parser, gml);
        } else if ("EX_GeographicBoundingBox".equals(extentObj.getName())) {
            double minx = Double.parseDouble(extentObj.getChildText("westBoundLongitude", Geonet.Namespaces.GMD));
            double maxx = Double.parseDouble(extentObj.getChildText("eastBoundLongitude", Geonet.Namespaces.GMD));
            double miny = Double.parseDouble(extentObj.getChildText("southBoundLatitude", Geonet.Namespaces.GMD));
            double maxy = Double.parseDouble(extentObj.getChildText("northBoundLatitude", Geonet.Namespaces.GMD));
            geometry = factory.toGeometry(new Envelope(minx, maxx, miny, maxy));
        }
        
        if (geometry != null) {
            Element element = extentObj.getChild("element", Geonet.Namespaces.GEONET);
            if (element != null) {
                id = element.getAttributeValue("ref");
            }
            return new MetadataRegion(fileId, id, geometry);
        } else {
            return null;
        }
    }

    private Element findMetadata(String fileId) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        
        String mdId = Utils.lookupMetadataIdFromFileId(gc, fileId);
        boolean withEditorValidationErrors = false;
        boolean keepXlinkAttributes = true;
        
        return gc.getDataManager().getMetadata(context, mdId, true, withEditorValidationErrors, keepXlinkAttributes);
    }

    @Override
    public Request id(String regionId) {
        this.id = regionId;
        return this;
    }
    private static final FindByNodeName EXTENT_FINDER = new FindByNodeName("EX_BoundingPolygon", "EX_GeographicBoundingBox", "polygon");
    private static final class FindByNodeName implements Filter {
        private static final long serialVersionUID = 1L;
        private String[] names;

        public FindByNodeName(String... names) {
            this.names = names;
        }
        @Override
        public boolean matches(Object obj) {
            if (obj instanceof Element) {
                Element el = (Element) obj;
                for (String name : this.names) {
                    if (el.getName().equals(name)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
    }

}
