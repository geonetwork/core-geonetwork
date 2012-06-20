package org.fao.geonet.kernel.search.spatial;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.fao.xsl.TransformationTestSupport;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.*;

import com.vividsolutions.jts.geom.Geometry;

public class ExtractGeometriesTest
{
    static Parser               PARSER       = new Parser(new org.geotools.gml3.GMLConfiguration());
	private static String transformer;

    @BeforeClass
    public static void setup() {
    	transformer = System.getProperty("javax.xml.transform.TransformerFactory");
    	System.setProperty("javax.xml.transform.TransformerFactory", "de.fzi.dbs.xml.transform.CachingTransformerFactory");
    }
    @AfterClass
    public static void teardown() {
    	if(transformer == null) {
    	System.getProperties().remove("javax.xml.transform.TransformerFactory");
    	} else {
    		System.setProperty("javax.xml.transform.TransformerFactory", transformer);
    	}
    }
    @Test
    public void textExtractGeometryIso1913BoundingPolygon() throws Exception
    {
        assertExtractGeometries("iso19139", "boundingpolygon-19139.xml", 1);
    }

    @Test
    public void textExtractGeometryIso19139Bbox() throws Exception
    {
        assertExtractGeometries("iso19139", "bbox-19139.xml", 1);
    }

    @Test
    public void textExtractGeometryDublinCore() throws Exception
    {
        assertExtractGeometries("dublin-core", "dc.xml", 1);
    }

    @Test
    public void textExtractGeometryFGDC() throws Exception
    {
        assertExtractGeometries("fgdc-std", "fgdc.xml", 1);
    }

    @Test
    public void textExtractGeometryIso19115() throws Exception
    {
        assertExtractGeometries("iso19115", "iso19115.xml", 1);
    }

    @Test
    public void textExtractGeometryIso1913CheBoundingPolygon() throws Exception
    {
        assertExtractGeometries("iso19139.che", "boundingpolygon-19139.xml", 1);
    }

    @Test
    public void textExtractGeometryIso19139CheBbox() throws Exception
    {
        assertExtractGeometries("iso19139.che", "bbox-19139.xml", 1);
    }
    
    @Test
    public void textExtractGeometryIso19139Service() throws Exception
    {
    	assertExtractGeometries("iso19139.che", "service-bbox-19139.xml", 1);
    }
    @Test
    public void textExtractManyMixed19139Che() throws Exception
    {
        assertExtractGeometries("iso19139.che", "many_extent_data.xml", 1);
    }

    private void assertExtractGeometries(String metadataType,
            String metadataFile, int numGeoms) throws IOException,
            JDOMException, Exception
    {
        Element xml = TransformationTestSupport.getXML(null, "/data/extractgeoms/"+metadataFile);
        Geometry geometries = SpatialIndexWriter.extractGeometriesFrom(
                TransformationTestSupport.geonetworkWebapp.getAbsolutePath()+File.separator+"WEB-INF"+File.separator+"data"+File.separator+"config"+File.separator+"schema_plugins"+File.separator+metadataType, xml, PARSER);
        assertNotNull(geometries);
    }

}
