//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;

import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;

//=============================================================================

/**
 * Read elements matching a simple path expression from a document stream Doesn't load the whole
 * document into memory (uses StAX to read the elements returning them as JDOM elements).  For use
 * processing large documents
 */

public class XmlElementReader implements Iterator<Element> {

    /**
     * Map that contains conversion from textual attribute types StAX uses, to int values JDOM
     * uses.
     */

    final static HashMap<String, Integer> attrTypes = new HashMap<String, Integer>(32);

    static {
        attrTypes.put("CDATA", Integer.valueOf(org.jdom.Attribute.CDATA_TYPE));
        attrTypes.put("cdata", Integer.valueOf(org.jdom.Attribute.CDATA_TYPE));
        attrTypes.put("ID", Integer.valueOf(org.jdom.Attribute.ID_TYPE));
        attrTypes.put("id", Integer.valueOf(org.jdom.Attribute.ID_TYPE));
        attrTypes.put("IDREF", Integer.valueOf(org.jdom.Attribute.IDREF_TYPE));
        attrTypes.put("idref", Integer.valueOf(org.jdom.Attribute.IDREF_TYPE));
        attrTypes.put("IDREFS", Integer.valueOf(org.jdom.Attribute.IDREFS_TYPE));
        attrTypes.put("idrefs", Integer.valueOf(org.jdom.Attribute.IDREFS_TYPE));
        attrTypes.put("ENTITY", Integer.valueOf(org.jdom.Attribute.ENTITY_TYPE));
        attrTypes.put("entity", Integer.valueOf(org.jdom.Attribute.ENTITY_TYPE));
        attrTypes.put("ENTITIES", Integer.valueOf(org.jdom.Attribute.ENTITIES_TYPE));
        attrTypes.put("entities", Integer.valueOf(org.jdom.Attribute.ENTITIES_TYPE));
        attrTypes.put("NMTOKEN", Integer.valueOf(org.jdom.Attribute.NMTOKEN_TYPE));
        attrTypes.put("nmtoken", Integer.valueOf(org.jdom.Attribute.NMTOKEN_TYPE));
        attrTypes.put("NMTOKENS", Integer.valueOf(org.jdom.Attribute.NMTOKENS_TYPE));
        attrTypes.put("nmtokens", Integer.valueOf(org.jdom.Attribute.NMTOKENS_TYPE));
        attrTypes.put("NOTATION", Integer.valueOf(org.jdom.Attribute.NOTATION_TYPE));
        attrTypes.put("notation", Integer.valueOf(org.jdom.Attribute.NOTATION_TYPE));
        attrTypes.put("ENUMERATED", Integer.valueOf(org.jdom.Attribute.ENUMERATED_TYPE));
        attrTypes.put("enumerated", Integer.valueOf(org.jdom.Attribute.ENUMERATED_TYPE));
    }

    private XMLEventReader reader;
    private XMLEvent currentEvent;

    //---------------------------------------------------------------------------
    //
    // Constructor
    //
    // @param source		XML document as an input stream
    // @param path 			simple path expression for matching element paths (e.g gml:featureMembers/*) (not an XPath!)
    // @param namespaces	namespace prefixes used in path
    //
    private Stack<QName> elementNameStack;

    //---------------------------------------------------------------------------
    private MatchExpression matchExpression;

    //---------------------------------------------------------------------------

    public XmlElementReader(InputStream source, String path, List<org.jdom.Namespace> namespaces) throws XMLStreamException, FactoryConfigurationError {
        this.reader = XMLInputFactory.newInstance().createXMLEventReader(source);
        this.matchExpression = new MatchExpression(path, namespaces);
        this.elementNameStack = new Stack<QName>();

        // find the first element with matching path
        findNext();
    }

    //---------------------------------------------------------------------------

    /**
     * Translate a StAX attribute type into a JDOM attribute type
     */

    private static int resolveAttrType(String typeStr) {
        if (typeStr != null && typeStr.length() > 0) {
            Integer I = (Integer) attrTypes.get(typeStr);

            if (I != null) {
                return I.intValue();
            }
        }

        return org.jdom.Attribute.UNDECLARED_TYPE;
    }

    //---------------------------------------------------------------------------

    /**
     * Check if there are any more elements of the required type to be read from the stream
     */

    public boolean hasNext() {
        return reader.hasNext();
    }

    //---------------------------------------------------------------------------

    /**
     * Get the next element of the required type from the stream
     */

    public Element next() {
        if (!hasNext()) throw new NoSuchElementException();

        Element result = readElement();
        findNext();

        return result;
    }

    //---------------------------------------------------------------------------

    /**
     * Remove element for the stream is not implemented
     */

    public void remove() {
        // not implemented
    }

    //---------------------------------------------------------------------------

    /**
     * Read events from the StAX reader until an element is found that matches the match expression
     * or the end of the stream is encountered
     */

    private void findNext() {
        while (reader.hasNext()) {
            currentEvent = (XMLEvent) reader.next();

            // update element path
            if (currentEvent.getEventType() == XMLStreamConstants.START_ELEMENT) {
                elementNameStack.push(currentEvent.asStartElement().getName());
            } else if (currentEvent.getEventType() == XMLStreamConstants.END_ELEMENT) {
                elementNameStack.pop();
            }

            // check for match
            if (currentEvent.getEventType() == XMLEvent.START_ELEMENT && matchExpression.matches(elementNameStack.toArray(new QName[0]))) {
                elementNameStack.pop();
                return;
            }
        }
    }

    /**
     * Read an element from the stream processing all events for the element
     */

    private Element readElement() {
        // Create the element
        Element element = createElement(currentEvent);

        // Add element content
        currentEvent = (XMLEvent) reader.next();

        while (currentEvent.getEventType() != XMLEvent.END_ELEMENT) {

            switch (currentEvent.getEventType()) {
                case XMLStreamConstants.CDATA:
                    element.addContent(new CDATA(currentEvent.asCharacters().getData()));
                    break;

                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CHARACTERS:
                    element.addContent(new Text(currentEvent.asCharacters().getData()));
                    break;

                case XMLStreamConstants.COMMENT:
                    element.addContent(new Comment(currentEvent.asCharacters().getData()));
                    break;

                case XMLStreamConstants.ENTITY_REFERENCE:
                    EntityReference er = (EntityReference) currentEvent;
                    element.addContent(new EntityRef(er.getName(), er.getDeclaration().getPublicId(), er.getDeclaration().getSystemId()));
                    break;

                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    javax.xml.stream.events.ProcessingInstruction pi = (javax.xml.stream.events.ProcessingInstruction) currentEvent;
                    element.addContent(new ProcessingInstruction(pi.getTarget(), pi.getData()));
                    break;

                case XMLStreamConstants.START_ELEMENT:

                    element.addContent(readElement());
                    break;
                default:
                    throw new IllegalStateException("Not a recognized value");
            }

            currentEvent = (XMLEvent) reader.next();
        }

        return element;
    }

    /**
     * Create an element from a start element event
     */

    private Element createElement(XMLEvent event) {
        Element result = null;

        String nsURI = event.asStartElement().getName().getNamespaceURI();
        String elemPrefix = event.asStartElement().getName().getPrefix();
        String ln = event.asStartElement().getName().getLocalPart();

        if (nsURI == null || nsURI.length() == 0) {
            if (elemPrefix == null || elemPrefix.length() == 0) {
                result = new Element(ln);
            } else {
                result = new Element(ln, elemPrefix, "");
            }
        } else {
            result = new Element(ln, elemPrefix, nsURI);
        }

        @SuppressWarnings("unchecked")
        Iterator<Namespace> nss = event.asStartElement().getNamespaces();

        while (nss.hasNext()) {
            Namespace ns = nss.next();
            String prefix = ns.getPrefix();

            // JDOM has special handling for element's "own" ns:
            if (prefix.equals(elemPrefix)) {
                ; // already set by when it was constructed...
            } else {
                result.addNamespaceDeclaration(org.jdom.Namespace.getNamespace(prefix, ns.getNamespaceURI()));
            }
        }

        @SuppressWarnings("unchecked")
        Iterator<Attribute> atts = event.asStartElement().getAttributes();

        while (atts.hasNext()) {
            Attribute att = atts.next();
            String prefix = att.getName().getPrefix();
            org.jdom.Namespace ns;

            if (prefix == null || prefix.length() == 0) {
                ns = org.jdom.Namespace.NO_NAMESPACE;
            } else {
                ns = result.getNamespace(prefix);
            }

            org.jdom.Attribute attr = new org.jdom.Attribute(att.getName().getLocalPart(), att.getValue(), resolveAttrType(att.getDTDType()), ns);
            result.setAttribute(attr);
        }

        return result;
    }

    //---------------------------------------------------------------------------

    /**
     * Class encapsulating matching elements against a simple path expression Could be generalised
     * to allow different matching algorithms
     */

    private static class MatchExpression {
        private boolean absolute;
        private NameTest[] tests;

        //---------------------------------------------------------------------------

        /**
         * Convert a simple path expression into its component tests
         */

        public MatchExpression(String path, List<org.jdom.Namespace> namespaces) {
            if (path.startsWith("/")) {
                absolute = true;
                path = path.replaceFirst("/", "");
            } else {
                absolute = false;
            }

            String[] nameTests = path.split("/");
            List<NameTest> tests = new ArrayList<NameTest>();

            for (String nameTest : nameTests) {
                tests.add(NameTest.newTest(nameTest, namespaces));
            }

            this.tests = (NameTest[]) tests.toArray(new NameTest[0]);
        }

        public boolean matches(QName[] names) {
            if (absolute && names.length != tests.length) {
                return false;
            }

            if (tests.length > names.length) {
                return false;
            }

            int i = names.length - 1;
            int j = tests.length - 1;

            while (i >= 0 && j >= 0) {
                if (!tests[j].matches(names[i])) return false;
                i--;
                j--;
            }

            return true;
        }
    }

    /******************************************************************************************
     * Element name tests
     ******************************************************************************************/

    /**
     * Abstract class for name tests also includes static factory method for creating name tests
     */

    private static abstract class NameTest {
        public static NameTest newTest(String nameTest, List<org.jdom.Namespace> namespaces) {
            if (nameTest.equals("*")) {
                return new WildcardTest();
            } else {
                return new EqualityTest(nameTest, namespaces);
            }
        }

        public abstract boolean matches(QName name);
    }

    /**
     * Wildcard test - any name matches
     */

    private static class WildcardTest extends NameTest {
        public WildcardTest() {
        }

        ;

        public boolean matches(QName name) {
            return true;
        }
    }

    /**
     * Equality test - local name and namespace if any must match
     */

    private static class EqualityTest extends NameTest {
        private QName name;

        public EqualityTest(String nameTest, List<org.jdom.Namespace> namespaces) {
            String[] nameParts = nameTest.split(":");

            if (nameParts.length == 1) {
                name = new QName(nameParts[0]);
            } else {
                getQName(namespaces, nameParts[0], nameParts[1]);
            }
        }

        private void getQName(List<org.jdom.Namespace> namespaces, String prefix, String localName) {
            for (org.jdom.Namespace namespace : namespaces) {
                if (prefix.equals(namespace.getPrefix())) {
                    name = new QName(namespace.getURI(), localName, prefix);
                    return;
                }
            }

            throw new IllegalArgumentException("Namespace prefix not defined");
        }

        public boolean matches(QName name) {
            return this.name.equals(name);
        }
    }

}
