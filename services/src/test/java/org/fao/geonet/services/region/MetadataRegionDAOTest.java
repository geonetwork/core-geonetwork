package org.fao.geonet.services.region;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MetadataRegionDAOTest extends AbstractServiceIntegrationTest {

    public static final ArrayList<Namespace> NAMESPACES = Lists.newArrayList(ISO19139Namespaces.GMD, Geonet.Namespaces.GML,
            Geonet.Namespaces.GEONET);
    private int metadataId;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private MetadataRegionDAO regionDAO;
    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        loginAsAdmin(this.context);
        String uuid = null;
        InputStream xmlStream = AbstractCoreIntegrationTest.class.getResourceAsStream("kernel/valid-metadata.iso19139.xml");
        Element xml = Xml.loadStream(xmlStream);
        final Element bbox = Xml.selectElement(xml, "*//gmd:EX_GeographicBoundingBox", NAMESPACES);
        final int i = bbox.getParentElement().indexOf(bbox);
        String polygonXmlString = "<gmd:EX_BoundingPolygon xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:xsi=\"http://www.w3"
                                  + ".org/2001/XMLSchema-instance\" xmlns:gml=\"http://www"
                                  + ".opengis.net/gml\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" >\n"
                           + "<gmd:extentTypeCode>\n"
                           + "<gco:Boolean>1</gco:Boolean>\n"
                           + "</gmd:extentTypeCode>\n"
                           + "<gmd:polygon>\n"
                           + "<gml:MultiSurface gml:id=\"N65ca5245da064e9bbeecc4c99f0eb54f\">\n"
                           + "<gml:surfaceMember>\n"
                           + "<gml:Polygon gml:id=\"N65ca5245da064e9bbeecc4c99f0eb54f.1\">\n"
                           + "<gml:exterior>\n"
                           + "<gml:LinearRing>\n"
                           + "<gml:posList>5.5 45.5 5.5 48.0 10.5 48.0 10.5 45.5 5.5 45.5</gml:posList>\n"
                           + "</gml:LinearRing>\n"
                           + "</gml:exterior>\n"
                           + "</gml:Polygon>\n"
                           + "</gml:surfaceMember>\n"
                           + "</gml:MultiSurface>\n"
                           + "</gmd:polygon>\n"
                           + "</gmd:EX_BoundingPolygon>";
        bbox.getParentElement().addContent(i, Xml.loadString(polygonXmlString, false));
        bbox.detach();

        xmlStream = new ByteArrayInputStream(Xml.getString(xml).getBytes(Constants.CHARSET));
        this.metadataId = importMetadataXML(this.context, uuid, xmlStream, MetadataType.METADATA, ReservedGroup.guest.getId(), uuid);
    }

    @Test
    public void testGetGeomEditId() throws Exception {
        final Element metadata = dataManager.getMetadata(this.context, "" + this.metadataId, true, false, false);

        final Element extentEl = Xml.selectElement(metadata, "*//gmd:EX_BoundingPolygon/geonet:element", NAMESPACES);
        final String geomRefId = extentEl.getAttributeValue("ref");
        Geometry geom = regionDAO.getGeom(this.context, "metadata:@id" + this.metadataId + ":" + geomRefId, false, "EPSG:4326");
        assertNotNull(geom);

        geom = regionDAO.getGeom(this.context, "metadata:@id" + this.metadataId + ":920934802934809238", false, "EPSG:4326");
        assertNull(geom);
    }

    @Test
    public void testGetGeomUUID() throws Exception {
        final Element metadata = dataManager.getMetadata(this.context, "" + this.metadataId, true, false, false);

        final Element extentEl = Xml.selectElement(metadata, "*//gmd:EX_BoundingPolygon/geonet:element", NAMESPACES);
        final String geomRefId = extentEl.getAttributeValue("ref");

        String uuid = dataManager.getMetadataUuid(String.valueOf(this.metadataId));
        Geometry geom = regionDAO.getGeom(this.context, "metadata:@uuid" + uuid + ":" + geomRefId, false, "EPSG:4326");
        assertNotNull(geom);

        geom = regionDAO.getGeom(this.context, "metadata:@uuid" + uuid + ":920934802934809238", false, "EPSG:4326");
        assertNull(geom);
    }

    @Test
    public void testGetGeomGmlId() throws Exception {
        final Element metadata = dataManager.getMetadata(this.context, "" + this.metadataId, true, false, false);

        final Element extentEl = Xml.selectElement(metadata, "*//gml:MultiSurface", NAMESPACES);
        final String geomRefId = extentEl.getAttributeValue("id", Geonet.Namespaces.GML);
        Geometry geom = regionDAO.getGeom(this.context, "metadata:@id" + this.metadataId + ":@gml" + geomRefId, false, "EPSG:4326");
        assertNotNull(geom);

        geom = regionDAO.getGeom(this.context, "metadata:@id" + this.metadataId + ":@gml920934802934809238", false, "EPSG:4326");
        assertNull(geom);
    }

    @Test
    public void testGetGeomXPath() throws Exception {
        final Element metadata = dataManager.getMetadata(this.context, "" + this.metadataId, true, false, false);

        final Element extentEl = Xml.selectElement(metadata, "*//gml:MultiSurface", NAMESPACES);
        final String xpath = Xml.getXPathExpr(extentEl);
        Geometry geom = regionDAO.getGeom(this.context, "metadata:@id" + this.metadataId + ":@xpath" + xpath, false, "EPSG:4326");
        assertNotNull(geom);

        geom = regionDAO.getGeom(this.context, "metadata:@id" + this.metadataId + ":@xpath920934802934809238", false, "EPSG:4326");
        assertNull(geom);
    }

    @Test
    public void testWrongMetadataId() throws Exception {
        final Element metadata = dataManager.getMetadata(this.context, "" + this.metadataId, true, false, false);

        final Element extentEl = Xml.selectElement(metadata, "*//gmd:EX_BoundingPolygon/geonet:element", NAMESPACES);
        final String geomRefId = extentEl.getAttributeValue("ref");

        Geometry geom = regionDAO.getGeom(this.context, "metadata:@id32342389:" + geomRefId, false, "EPSG:4326");
        assertNull(geom);

        geom = regionDAO.getGeom(this.context, "metadata:@uuid323423898023432:" + geomRefId, false, "EPSG:4326");
        assertNull(geom);

        geom = regionDAO.getGeom(this.context, "metadata:@fileId323423898023432:" + geomRefId, false, "EPSG:4326");
        assertNull(geom);

        geom = regionDAO.getGeom(this.context, "metadata:@fileId323423898023432", false, "EPSG:4326");
        assertNull(geom);

        geom = regionDAO.getGeom(this.context, "metadata:@id323423898023432", false, "EPSG:4326");
        assertNull(geom);

        geom = regionDAO.getGeom(this.context, "metadata:@uuid323423898023432", false, "EPSG:4326");
        assertNull(geom);

    }
}