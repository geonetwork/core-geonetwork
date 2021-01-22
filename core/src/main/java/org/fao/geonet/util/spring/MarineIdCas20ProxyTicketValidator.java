package org.fao.geonet.util.spring;

import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MarineIdCas20ProxyTicketValidator extends Cas20ProxyTicketValidator {

    public MarineIdCas20ProxyTicketValidator(String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    /**
     * Basically copy-pasted from the original Cas20ServiceTicketValidator class,
     * adapted to use our custom XML handler.
     *
     * @param xml the XML spec returned by the CAS MarineID server.
     * @return a hashmap with the extra attributes.
     */
    @Override
    protected Map<String, Object> extractCustomAttributes(String xml) {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        try {
            final SAXParser saxParser = spf.newSAXParser();
            final XMLReader xmlReader = saxParser.getXMLReader();
            final MarineIdCasAttributeHandler handler = new MarineIdCasAttributeHandler();
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(new StringReader(xml)));
            return handler.getAttributes();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private class MarineIdCasAttributeHandler extends DefaultHandler {

        private Map<String, Object> attributes;

        @Override
        public void startDocument() throws SAXException {
            this.attributes = new HashMap<String, Object>();
        }

        @Override
        public void startElement(final String namespaceURI, final String localName, final String qName,
                                 final Attributes attributes) throws SAXException {
            if ("attribute".equals(localName)) {
                String attrName = attributes.getValue("name");
                String attrValue = attributes.getValue("value");
                this.attributes.put(attrName, attrValue);
            }
        }

        public Map<String, Object> getAttributes() {
            return this.attributes;
        }
    }
}
