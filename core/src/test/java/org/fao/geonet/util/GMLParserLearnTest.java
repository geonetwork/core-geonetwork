package org.fao.geonet.util;

import org.fao.geonet.utils.Xml;
import org.geotools.xsd.Parser;
import org.jdom.JDOMException;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GML;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class GMLParserLearnTest {

    static final String TO_PARSE;

    static {
        URL resource = GMLParserLearnTest.class.getResource("../kernel/multilingual-metadata.xml");
        String toParse;
        try {
            toParse = Xml.getString(Xml.selectElement( Xml.loadStream(resource.openStream()), ".//gmd:polygon", Arrays.asList(GMD, GCO, GML)));
        } catch (Exception e) {
            toParse = null;
            e.printStackTrace();
        }
        TO_PARSE = toParse;
    }


    @Test
    public void geotoolsGmlParserNotThreadSafe() throws IOException, JDOMException, ParserConfigurationException, SAXException, InterruptedException {
        final Parser parser = GMLParsers.createGML();
        parser.parse(new StringReader(TO_PARSE));

        IntStream range = IntStream.rangeClosed(1, 20);
        List<Object> failedParseNew = range.parallel().mapToObj(GMLParserLearnTest::parseCreateNewParser).filter(x -> x == null).collect(Collectors.toList());
        assertEquals(0, failedParseNew.size());

        range = IntStream.rangeClosed(1, 20);
        List<Object> failedParseReuse = range.parallel().mapToObj(GMLParserLearnTest::parseResuseParser).filter(x -> x == null).collect(Collectors.toList());
        assertNotEquals(0, failedParseReuse.size());

    }

    static private Object parseCreateNewParser(int inc)  {
        Parser parser = GMLParsers.createGML();
        try {
            return parser.parse(new StringReader(TO_PARSE));
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }

    }

    static private Parser parserToReuse = GMLParsers.createGML();

    static private Object parseResuseParser(int inc)  {
        try {
            return parserToReuse.parse(new StringReader(TO_PARSE));
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }

    }
}
