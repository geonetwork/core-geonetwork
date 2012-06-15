package org.fao.geonet.services.gm03;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ISO19139CHEtoGM03Base {
    protected static final String NS = "http://www.interlis.ch/INTERLIS2.3";
    protected static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    protected static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    protected static final Pattern EMPTY = Pattern.compile("^[\\s\n\r]*$");
    protected final Schema schema;
    protected Transformer xslt;

    public ISO19139CHEtoGM03Base(File schemaLocation, String xslFilename) throws SAXException, TransformerConfigurationException {
        if (schemaLocation != null) {
            schema = SCHEMA_FACTORY.newSchema(schemaLocation);
        } else {
            schema = null;
        }
        xslt = TRANSFORMER_FACTORY.newTransformer(new StreamSource(xslFilename));
    }

    protected void convert(String[] xmlFilenames, String group) {
        for (int i = 0; i < xmlFilenames.length; i++) {
            String xmlFilename = xmlFilenames[i];
            try {
                convert(xmlFilename, group);
            } catch (Throwable e) {
                System.out.println("Error while converting " + xmlFilename);
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public Document convert(Document domIn) throws TransformerException, FlattenerException, IOException, SAXException {
        final Source source = new DOMSource(domIn);

        // TODO make the group the group of the metadata being transformed.  But this could cause bugs so I am leaving it now
        final Document doc = doTransform("geocat.ch", source, null);

        validate(domIn.getDocumentURI(), doc);
        return doc;
    }

    private Document doTransform(String group, final Source source, String intermediateFile) throws TransformerException, FlattenerException, IOException
    {
        DOMResult transformed = new DOMResult();
        xslt.setParameter("group", group);
        xslt.transform(source, transformed);
        final Document doc = (Document) transformed.getNode();

        if (wantIntermediate() && intermediateFile!=null) {
            OutputStream outputStream = new FileOutputStream(intermediateFile);
            saveDom(doc, outputStream);
        }

        flatten(doc);
        convertCoordinates(doc);
        removeDuplicates(doc);
        return doc;
    }

    public void convert(String xmlFilename, String group) throws FlattenerException, IOException, TransformerException, SAXException {
        File xmlFile = new File(xmlFilename);
        String parent = xmlFile.getParent();
        if (parent == null) parent = ".";

        final Source source = new StreamSource(xmlFilename);
        Document doc = doTransform(group, source, parent + "/intermediate_" + xmlFile.getName());

        final String resultFilename = parent + "/result_" + xmlFile.getName();
        OutputStream outputStream = new FileOutputStream(resultFilename);
        saveDom(doc, outputStream);
        validate(resultFilename, doc);

    }

    public void convert(Source source, String group, OutputStream outputStream) throws FlattenerException, IOException, TransformerException, SAXException {
        
        Document doc = doTransform(group, source, null);
        
        saveDom(doc, outputStream);
        
    }

    protected abstract boolean wantIntermediate();

    protected abstract void flatten(Document source) throws FlattenerException;

    protected abstract void removeDuplicates(Document doc) throws FlattenerException;

    /**
     * Will replace all the gml:coordinates with a set of COORD with C1 and C2 childs.
     */
    private void convertCoordinates(Document doc) {
        while (true) {
            NodeList toConvert = doc.getElementsByTagNameNS(getGmlCoordinateNs(), "GML_COORDINATES");
            if (toConvert.getLength() == 0) break;
            Node cur = toConvert.item(0);
            convertCoordinates(doc, cur);
        }
    }

    protected abstract String getGmlCoordinateNs();

    /**
     * Will replace a gml:coordinates with a set of COORD with C1 and C2 childs.
     */
    protected void convertCoordinates(Document doc, Node gmlCoordinates) {
        Node parent = gmlCoordinates.getParentNode();

        String cs = gmlCoordinates.getAttributes().getNamedItem("cs").getTextContent();
        String decimal = gmlCoordinates.getAttributes().getNamedItem("decimal").getTextContent();
        String ts = gmlCoordinates.getAttributes().getNamedItem("ts").getTextContent();
        String raw = gmlCoordinates.getTextContent();

        String[] coords = raw.split(Pattern.quote(ts));
        for (int j = 0; j < coords.length; j++) {
            String coord = coords[j];
            String[] vals = coord.split(Pattern.quote(cs));
            if (vals.length >= 2) {
                for (int i = 0; i < vals.length; i++) {
                    vals[i] = vals[i].replace(decimal, ".");
                }
                Element coordNode = createCoordinate(doc, vals);
                parent.insertBefore(coordNode, gmlCoordinates);
            }
        }

        parent.removeChild(gmlCoordinates);
    }

    protected abstract Element createCoordinate(Document doc, String[] vals);

    protected void validate(String xmlFilename, Document doc) throws IOException, SAXException {
        if (schema != null) {
            Validator validator = schema.newValidator();
            final TranslateAndValidate.MyErrorHandler errorHandler = new org.fao.geonet.services.gm03.TranslateAndValidate.MyErrorHandler(xmlFilename);
            validator.setErrorHandler(errorHandler);

            Source validationSource = new DOMSource(doc);

            validator.validate(validationSource);
            //errorHandler.printError(System.out);
            errorHandler.throwErrors();
        }
    }

    private static void saveDom(Document node, OutputStream outputStream) throws IOException {
        try {
            OutputFormat format = new OutputFormat();
            format.setIndent(2);
            format.setIndenting(true);
            format.setPreserveSpace(false);

            XMLSerializer serializer = new XMLSerializer();
            serializer.setOutputFormat(format);
            serializer.setOutputByteStream(outputStream);
            serializer.serialize(node);
        } finally {
            outputStream.close();
        }
    }

    public static class FlattenerException extends Exception {
        public FlattenerException(String message) {
            super(message);
        }
    }

    /**
     * A DOM child node iterator that takes a snapshot of the list of child nodes
     * at construction. It is therefore not impacted by the modifications while iterating
     */
    protected class ChildIterator implements Iterator<Node> {
        private final List<Node> childs;
        private int pos = 0;

        public ChildIterator(Node root) {
            childs = new ArrayList<Node>();
            for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
                childs.add(child);
            }
        }

        public boolean hasNext() {
            return pos < childs.size();
        }

        public Node next() {
            return childs.get(pos++);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
