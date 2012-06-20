package org.fao.geonet.services.gm03;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ISO19139CHEtoGM03 extends ISO19139CHEtoGM03Base {
    public ISO19139CHEtoGM03(File schemaLocation, String xslFilename) throws SAXException, TransformerConfigurationException {
        super(schemaLocation, xslFilename);
    }

    protected boolean wantIntermediate() {
        return true;
    }

    protected void flatten(Document source) throws FlattenerException {
        final Node start = getRootNode(source);
        flattenNode(start, null, start);
    }

    private void flattenNode(Node root, Node parent, Node cur) throws FlattenerException {
        if (cur.getNodeType() != Node.ELEMENT_NODE) return;

        final NamedNodeMap attrs = cur.getAttributes();

        Node backRef = findBackRef(cur);
        if (backRef != null) {
            replaceByInsertion(root, parent, cur, backRef);
        }

        if (attrs != null) {
            Node ref = attrs.getNamedItem("REF");
            if (ref != null && ref.getTextContent().equals("?")) {
                replaceByReference(root, parent, cur);
            }
        }

        final Iterator<Node> childsIter = new ChildIterator(cur);
        while (childsIter.hasNext()) {
            Node child = childsIter.next();
            flattenNode(root, cur, child);
        }
    }

    private void replaceByInsertion(Node root, Node parent, Node cur, Node dummyBackRef) throws FlattenerException {
        final Node tidAttr = parent.getAttributes().getNamedItem("TID");
        if (tidAttr == null) {
            //already moved...
            return;
        }
        final String tid = tidAttr.getTextContent();

        parent.removeChild(cur);
        removeEmptyStuff(parent);
        root.appendChild(cur);

        String backRefName = dummyBackRef.getAttributes().getNamedItem("name").getTextContent();
        String patchedName = backRefName;
        if( backRefName.equals("DQ_Qualitiy") ) patchedName="DQ_DataQuality";

        final Document doc = cur.getOwnerDocument();
        Node backRef = doc.createElementNS(NS, backRefName);
        final Attr ref = doc.createAttributeNS(null,"REF");
        ref.setTextContent(tid);
        backRef.getAttributes().setNamedItem(ref);

        cur.replaceChild(backRef, dummyBackRef);
    }

    private void removeEmptyStuff(Node parent) {
        ChildIterator it = new ChildIterator(parent);
        while (it.hasNext()) {
            Node node = it.next();
            if (node.getNodeType() == Node.TEXT_NODE && EMPTY.matcher(node.getTextContent()).matches()) {
                parent.removeChild(node);
            }
        }
    }

    private Node findBackRef(Node parent) throws FlattenerException {
        for (Node cur = parent.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
            if (cur.getNodeName().equals("BACK_REF")) {
                return cur;
            }
        }
        return null;
    }

    private void replaceByReference(Node root, Node parent, Node cur) throws FlattenerException {
        final NodeList childs = cur.getChildNodes();
        if (childs.getLength() < 1) {
            //cannot replace by a reference if there is no element in it
            return;
        }
        Node dest = childs.item(0);
        while (dest != null && dest.getNodeType() != Node.ELEMENT_NODE) {
            dest = dest.getNextSibling();
        }
        if (dest == null) {
            throw new FlattenerException("No child element found for flattening a reference): " + cur.getNodeName());
        }
        if (dest.getNodeName().equals("ERROR")) return;
        cur.removeChild(dest);

        if (dest.getAttributes() == null) {
            throw new FlattenerException("No attribute in element " + dest.getNodeName() + " from " + cur.getNodeName());
        }
        final Node tidTxt = dest.getAttributes().getNamedItem("TID");
        if (tidTxt == null) {
            throw new FlattenerException("No TID in element " + dest.getNodeName() + " from " + cur.getNodeName());
        }
        String tid = tidTxt.getTextContent();
        cur.getAttributes().getNamedItem("REF").setTextContent(tid);
        removeEmptyStuff(cur);

        root.appendChild(dest);

        //must flatten the child that we just moved.
        flattenNode(root, root, dest);
    }

    protected void removeDuplicates(Document doc) throws FlattenerException {
        final Node root = getRootNode(doc);

        //have to do that in more that one pass, since two nodes can become identical
        //once they reference the same node
        while (true) {
            boolean found = false;
            UnDuplifier unDuplifier = new org.fao.geonet.services.gm03.UnDuplifier();
            ChildIterator it = new ChildIterator(root);
            while (it.hasNext()) {
                Node cur = it.next();
                found = unDuplifier.addNode(root, cur) || found;
            }
            if (found) {
                unDuplifier.translateRefs(root);
            } else {
                break;
            }
        }
    }

    protected String getGmlCoordinateNs() {
        return NS;
    }

    private Node getRootNode(Document doc) throws FlattenerException {
        NodeList root = doc.getElementsByTagNameNS(NS, "GM03_2Comprehensive.Comprehensive");
        if (root.getLength() != 1) {
            throw new FlattenerException("Not 1 and only 1 GM03_2Comprehensive.Comprehensive");
        }
        final Node start = root.item(0);
        return start;
    }

    protected Element createCoordinate(Document doc, String[] vals) {
        Element coordNode = doc.createElementNS(NS, "COORD");
        for (int k = 0; k < vals.length; k++) {
            String val = vals[k];
            Element valNode = doc.createElementNS(NS, "C" + (k + 1));
            valNode.setTextContent(val);
            coordNode.appendChild(valNode);
        }
        return coordNode;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, FlattenerException, TransformerException {
        final String xslFilename = args[0];
        final String schemaFilename = args[1];
        final String[] xmlFilenames = (String[]) ArrayUtils.subarray(args, 2, args.length);

        File schemaLocation = null;
        if (!schemaFilename.equalsIgnoreCase("no")) {
            schemaLocation = new File(schemaFilename);
        }
        ISO19139CHEtoGM03 converter = new ISO19139CHEtoGM03(schemaLocation, xslFilename);

        converter.convert(xmlFilenames, "ISO19139CHEtoGM03.main");
    }

}
