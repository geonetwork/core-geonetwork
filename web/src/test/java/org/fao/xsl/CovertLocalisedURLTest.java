package org.fao.xsl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeves.utils.Xml;

import org.jdom.Element;
import org.jdom.filter.Filter;
import org.junit.Test;



public class CovertLocalisedURLTest
{
    @Test
    public void convert() throws Exception{
        String pathToXsl = TransformationTestSupport.geonetworkWebapp+"/xsl/iso-internal-multilingual-conversion-url.xsl";
        String testData = "/data/localised-url.xml";
        Element data = TransformationTestSupport.transform(getClass(), pathToXsl, testData );


        assertEquals(1, data.getChildren("EN").size());
        assertEquals(1, data.getChildren("FR").size());
        assertEquals(1, data.getChildren("IT").size());
        assertEquals(1, data.getChildren("DE").size());

        assertEquals("http://camptocamp.com/main",data.getChild("EN").getTextNormalize());
        assertEquals("http://camptocamp.com/fr",data.getChild("FR").getTextNormalize());
        assertEquals("http://camptocamp.com/de",data.getChild("DE").getTextNormalize());
        assertEquals("http://camptocamp.com/it",data.getChild("IT").getTextNormalize());

        data.setName("description");

        Element isoData = Xml.transform(data, pathToXsl);

        Map<String, String> langMap = findUrls(isoData);

        assertEquals(4, langMap.size());
        assertEquals("http://camptocamp.com/main", langMap.get("#EN"));
        assertEquals("http://camptocamp.com/fr", langMap.get("#FR"));
        assertEquals("http://camptocamp.com/de", langMap.get("#DE"));
        assertEquals("http://camptocamp.com/it", langMap.get("#IT"));

        Element noTranslation = Xml.loadString(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<description>http://camptocamp.com/main</description>", false);

        isoData = Xml.transform(noTranslation, pathToXsl);

        langMap = findUrls(isoData);

        assertEquals(1, langMap.size());
        assertEquals(1, isoData.getChildren().size());
        assertEquals("http://camptocamp.com/main", langMap.get("#EN"));

        Element duplicateTranslation = Xml.loadString(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<description><EN>EN</EN><EN>EN2</EN></description>", false);

        isoData = Xml.transform(duplicateTranslation, pathToXsl);
        Iterator textGroups = isoData.getDescendants(new Filter()
        {
            private static final long serialVersionUID = 1L;

            public boolean matches(Object arg0)
            {
                if (arg0 instanceof Element) {
                    Element elem = (Element) arg0;
                    return elem.getName().equals("URLGroup");
                }
                return false;
            }
        });
        
        assertTrue(textGroups.hasNext());
        textGroups.next();
        assertFalse(textGroups.hasNext());
        
    }

    private Map<String, String> findUrls(Element isoData)
    {
        @SuppressWarnings("unchecked")
        Iterator<Element> urls = isoData.getDescendants(new Filter()
        {
            private static final long serialVersionUID = 1L;

            public boolean matches(Object arg0)
            {
                if (arg0 instanceof Element) {
                    Element elem = (Element) arg0;
                    return elem.getName().equals("LocalisedURL");
                }
                return false;
            }
        });

        Map<String, String> langMap = new HashMap<String, String>();
        while( urls.hasNext()){
            Element n = urls.next();
            langMap.put(n.getAttributeValue("locale"), n.getTextNormalize());
        }
        return langMap;
    }

    @Test
    public void xmlUserGet() throws Exception
    {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp+"/xsl/shared-user/user-xml.xsl";
        String testData = "/data/xml.user.get.xml";
        Element data = TransformationTestSupport.transform(getClass(), pathToXsl, testData );
        System.out.println(Xml.getString(data));

        Map<String, String> langMap = findUrls(data);

        assertEquals(5, langMap.size());

        assertEquals("http://camptocamp.com/en", langMap.get("#EN"));
        assertEquals("http://camptocamp.com/fr", langMap.get("#FR"));
        assertEquals("http://camptocamp.com/de", langMap.get("#DE"));
        assertEquals("http://camptocamp.com/it", langMap.get("#IT"));
        assertEquals("http://camptocamp.com/rm", langMap.get("#RM"));

    }
}
