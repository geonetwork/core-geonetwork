package org.fao.xsl;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeves.utils.Xml;

import org.fao.geonet.util.LangUtils;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.junit.Test;



public class CovertLocalisedCharstringTest
{
    @Test
    public void convert() throws Exception{
        String pathToXsl = TransformationTestSupport.geonetworkWebapp+"/xsl/iso-internal-multilingual-conversion.xsl";
        String testData = "/data/localised-charstring.xml";
        Element data = TransformationTestSupport.transform(getClass(), pathToXsl, testData );

        System.out.println(Xml.getString(data));
        
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

        Map<String, String> langMap = findLocalizedString(isoData);

        assertEquals(4, langMap.size());
        assertEquals("http://camptocamp.com/main", langMap.get("#EN"));
        assertEquals("http://camptocamp.com/fr", langMap.get("#FR"));
        assertEquals("http://camptocamp.com/de", langMap.get("#DE"));
        assertEquals("http://camptocamp.com/it", langMap.get("#IT"));

        Element noTranslation = Xml.loadString(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<description>http://camptocamp.com/main</description>", false);

        isoData = Xml.transform(noTranslation, pathToXsl);

        langMap = findLocalizedString(isoData);

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
                    return elem.getName().equals("textGroup");
                }
                return false;
            }
        });
        
        assertTrue(textGroups.hasNext());
        textGroups.next();
        assertFalse(textGroups.hasNext());
    }

    private Map<String, String> findLocalizedString(Element isoData)
    {
        @SuppressWarnings("unchecked")
        Iterator<Element> urls = isoData.getDescendants(new Filter()
        {
            private static final long serialVersionUID = 1L;

            public boolean matches(Object arg0)
            {
                if (arg0 instanceof Element) {
                    Element elem = (Element) arg0;
                    return elem.getName().equals("LocalisedCharacterString");
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

        Map<String, String> langMap = findLocalizedString(data.getChild("organisationAcronym", Namespace.getNamespace("http://www.geocat.ch/2008/che")));

        assertEquals(5, langMap.size());

        assertEquals("ana en", langMap.get("#EN"));
        assertEquals("ana fr", langMap.get("#FR"));
        assertEquals("ana de", langMap.get("#DE"));
        assertEquals("ana it", langMap.get("#IT"));
        assertEquals("ana rm", langMap.get("#RM"));

    }
}
