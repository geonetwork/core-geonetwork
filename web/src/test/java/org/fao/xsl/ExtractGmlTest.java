package org.fao.xsl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;



public class ExtractGmlTest
{

    private static final String BASE_XML_DIR = TransformationTestSupport.geonetworkWebapp.getAbsolutePath()+File.separator+"WEB-INF"+File.separator+"data"+File.separator+"config"+File.separator+"schema_plugins"+File.separator;
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
    public void testTranformFGDC() throws Exception
    {
        String pathToXsl = "fgdc-std/extract-gml.xsl";
        performTest(pathToXsl,"fgdc.xml");
    }
    @Test
    public void testTranformDublinCore() throws Exception
    {
        String pathToXsl = "dublin-core/extract-gml.xsl";
        performTest(pathToXsl, "dc.xml");
    }
    @Test
    public void testTranformISO19115() throws Exception
    {
        String pathToXsl = "iso19115/extract-gml.xsl";
        performTest(pathToXsl, "iso19115.xml");
    }
    @Test
    public void testTranformISO19139() throws Exception
    {
        String pathToXsl = "iso19139/extract-gml.xsl";
        performTest(pathToXsl, "bbox-19139.xml");
        performTest(pathToXsl, "boundingpolygon-19139.xml");
    }
    @Test
    public void testTranformISO19139_che() throws Exception
    {
        String pathToXsl = "iso19139.che/extract-gml.xsl";
        performTest(pathToXsl, "bbox-19139.xml");
        performTest(pathToXsl, "boundingpolygon-19139.xml");
    }

    @Test
    public void testTranformMixedBBoxPolygon() throws Exception
    {
        String pathToXsl = "iso19139/extract-gml.xsl";
        Element transform = TransformationTestSupport.transform(getClass(), BASE_XML_DIR+pathToXsl, "/data/extractgeoms/mixed-19139.xml");
        assertEquals("GeometryCollection", transform.getName());
        Iterator coordinates = transform.getDescendants(new Filter()
        {

            public boolean matches(Object arg0)
            {
                if (arg0 instanceof Element) {
                    Element elem = (Element) arg0;
                    return elem.getName().equalsIgnoreCase("coordinates");
                }
                return false;
            }
        });

        List<String> coords = toCoordList(coordinates);
        List<Integer> coordCount = new ArrayList<Integer>();
        for (String string : coords) {
            coordCount.add(string.split(",").length);
        }

        assertEquals(1, count(coordCount,4));
        assertEquals(1, count(coordCount,5));
        assertEquals(1, count(coordCount,6));
    }
    
    @Test
    public void testTranformMixedBBoxPolygonCHE() throws Exception
    {
        String pathToXsl = "iso19139.che/extract-gml.xsl";
        Element transform = TransformationTestSupport.transform(getClass(), BASE_XML_DIR+pathToXsl, "/data/extractgeoms/mixed-19139.xml");
        assertEquals("GeometryCollection", transform.getName());
        Iterator coordinates = transform.getDescendants(new Filter()
        {
            
            public boolean matches(Object arg0)
            {
                if (arg0 instanceof Element) {
                    Element elem = (Element) arg0;
                    return elem.getName().equalsIgnoreCase("coordinates");
                }
                return false;
            }
        });
        
        List<String> coords = toCoordList(coordinates);
        List<Integer> coordCount = new ArrayList<Integer>();
        for (String string : coords) {
            coordCount.add(string.split(",").length);
        }
        
        assertEquals(1, count(coordCount,4));
        assertEquals(1, count(coordCount,5));
        assertEquals(1, count(coordCount,6));
    }

    private int count(List<Integer> coordCount, int toCount)
    {
        int count=0;
        for (Integer integer : coordCount) {
            if( integer.intValue() == (toCount*2+2)){
                count++;
            }
        }
        return count;
    }
    private List<String> toCoordList(Iterator<Element> coordinates)
    {
        List<String> result = new ArrayList<String>();
        while(coordinates.hasNext()){
            Element n = coordinates.next();
            result.add(n.getText());
        }
        return result;
    }
    private void performTest(String pathToXsl, String testData) throws Exception, IOException,
            JDOMException
    {
        Element transform = TransformationTestSupport.transform(getClass(), BASE_XML_DIR+pathToXsl, "/data/extractgeoms/"+testData);
        assertEquals("GeometryCollection", transform.getName());
        Element coords = getCoordinates(transform);
        assertEquals("37,83,156,83,156,-3,37,-3,37,83",coords.getTextTrim().replaceAll(" ", ""));
    }
    private Element getCoordinates(Element transform)
    {
        Element polygon = getElement(transform, "Polygon");
        Element exterior = getElement(polygon, "exterior");
        Element ring = getElement(exterior, "LinearRing");
        Element coords = getElement(ring, "coordinates");
        return coords;
    }

    private Element getElement(Element transform, String name)
    {
        for (int i = 0; i < transform.getContentSize(); i++) {
            Content elem = transform.getContent(i);
            if( elem instanceof Element && ((Element)elem).getName().equals(name)){
                return (Element) elem;
            }
        }
        return null;
    }

}
