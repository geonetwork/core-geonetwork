package org.fao.geonet.kernel.diff;

import jeeves.utils.Log;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * Class to add information about an XML's differences to itself. The info is stored in attributes, which can be used
 * in the presentation layer to highlight changes.
 *
 * @author heikki doeleman
 */
public class DifferenceAnnotator {

    public enum DifferenceDirection {
        SOURCE,TARGET
    }

    private static final String INSERTED = "Inserted";
    private static final String DELETED = "Deleted";
    private static final String ATTRIBUTE_INSERTED = "AttributeInserted";
    private static final String ATTRIBUTE_DELETED = "AttributeDeleted";
    private static final String ATTRIBUTE_UPDATED = "AttributeUpdated";

    private static XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat().setTextMode(Format.TextMode.NORMALIZE));
    private static Namespace geonet = Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork");


    /**
     * TODO javadoc.
     *
     * @param xml
     * @param delta
     * @throws DiffException
     */
    public static Element addDelta(Element xml, Element delta, DifferenceDirection direction) throws DiffException {
        try {
            List differences = delta.getChildren();
            for(Iterator i = differences.iterator(); i.hasNext();) {
                Element difference = (Element) i.next();
                boolean isTextUpdate = false;
                // the difference is about a text node
                if(StringUtils.isNotEmpty(difference.getText())) {
                    // check if there is a delete corresponding to an insert, or vv
                    String pos = difference.getAttributeValue(DifferenceAttribute.pos.name());
                    for(Iterator j = differences.iterator(); j.hasNext();) {
                        Element d = (Element) j.next();

                        if(StringUtils.isNotEmpty(d.getText()) && d.getAttributeValue(DifferenceAttribute.pos.name()).equals(pos)) {
                            if(difference.getName().equals(DELETED) && d.getName().equals(INSERTED) ||
                               difference.getName().equals(INSERTED) && d.getName().equals(DELETED)) {
                               // corresponding one found: treat as update
                                isTextUpdate = true;
                                Log.debug(Geonet.DIFF, "isUpdate is true !");
                            }
                        }
                    }

                }
                xml = addDifference(xml, difference, direction, isTextUpdate);
                xml = addPairIdentifiers(xml, 0, 0, "");
            }
            return xml;
        }
        catch(Exception x) {
            Log.error(Geonet.DIFF, x.getMessage());
            x.printStackTrace();
            throw new DiffException(x.getMessage(), x);
        }
    }

    /**
     * TODO javadoc.
     *
     * @param xml
     * @param depth
     * @param breadth
     * @param path
     * @return
     */
    protected static Element addPairIdentifiers(Element xml, int depth, int breadth, String path) {
        path = path + depth + breadth;
        depth++;
        List children = xml.getChildren();
        for(int i = 0; i < children.size(); i++) {
            Object c = children.get(i);
            if(c instanceof Element) {
                Element  child = (Element) c;
                addPairIdentifiers(child, depth, i, path);
            }
        }

        boolean alreadyAnnotated = false;
        List attributes = xml.getAttributes();
        for(Object attributeO : attributes) {
            Attribute attribute = (Attribute) attributeO;
            if(DifferenceAttribute.exists(attribute.getName())) {
                alreadyAnnotated = true;
                break;
            }
        }
        //if(!alreadyAnnotated) {
        xml.setAttribute(new Attribute("class", "pair-"+path+"-"+depth+"-"+breadth, geonet));
        //}


        return xml;
    }


    /**
     * Converts jxydiff position to xpath locationpath.
     *
     * @param position
     * @return
     */
    private static String jxyDiffPos2XPATH(String position) {
        String locationPath = "";
        Scanner scanner = new Scanner(position).useDelimiter(":");
        // skip first position (matches document, not the root element)
        scanner.next();
        // construct locationpath
        while(scanner.hasNext()) {
            int next = scanner.nextInt();
            locationPath += "/node()[" + (next+1)+"]";
        }
        return locationPath;
    }

    /**
     * TODO javadoc.
     *
     * @param doc
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    private static Document ridNewLines(Document doc) throws JDOMException, IOException {
        String doc$ = outputter.outputString(doc);
        if(doc$.indexOf('\n') >= 0) {
            doc$.replaceAll("\n", "");
        }
        SAXBuilder sb = new SAXBuilder();
        doc = sb.build(new StringReader(doc$));
        return doc;
    }


    /**
     * TODO javadoc.
     *
     * @param locationPath
     * @param doc
     * @return
     * @throws JDOMException
     */
    protected static Object selectNodeToAnnotate(String locationPath, Document doc) throws JDOMException, DiffException, XPathExpressionException, IOException {

        XPath xpath = XPath.newInstance(locationPath);
        List selectedNodes = xpath.selectNodes(doc);

        if(selectedNodes.size() != 1) {
            return null;
            //throw new DiffException("Unexpected # of nodes selected: " + selectedNodes.size() + " using XPATH " + locationPath);
        }
        Object selected = selectedNodes.get(0);
        if(selected instanceof Element) {
            Log.debug(Geonet.DIFF, locationPath + " : " + ((Element)selected).getName());
        }
        else if(selected instanceof Text) {
            Log.debug(Geonet.DIFF, locationPath + " : " + ((Text)selected).getText());
        }
        else {
            Log.debug(Geonet.DIFF, "unhandled node type");
        }
        return selected;
    }

    /**
     * TODO javadoc.
     *
     * @param operation
     * @param position
     * @param oldPosition
     * @param direction
     * @return
     */
    private static String getLocationPath(String operation, String position, String oldPosition, DifferenceDirection direction, boolean isTextUpdate) {
        String path;
        if(operation.equals(ATTRIBUTE_UPDATED) && StringUtils.isNotEmpty(oldPosition) && direction == DifferenceDirection.SOURCE) {
            path = oldPosition;
        }
        else if(operation.equals(ATTRIBUTE_UPDATED) && direction == DifferenceDirection.TARGET) {
            path = position;
        }
        else {
            if(StringUtils.isNotEmpty(oldPosition)) {
                path = oldPosition;
            }
            else {
                path = position;
            }
        }
        String locationPath = jxyDiffPos2XPATH(path);
        if(isTextUpdate) {
            locationPath = locationPath.substring(0, locationPath.lastIndexOf('/'));
            locationPath += "/text()";
        }
        Log.debug(Geonet.DIFF, "locationpath : " + locationPath);
        return locationPath;
    }

    /**
     * TODO javadoc.
     *
     * @param element
     * @param position
     * @param oldPosition
     */
    private static void addDiffClass(Element element, String position, String oldPosition) {
        String className = "";
        if(StringUtils.isNotEmpty(position)) {
            className = position.replace(':', '-');
        }
        element.setAttribute("class", className);
    }

    /**
     * Adds markers to store information about a difference in an XML document.
     *
     * The differences can be of these types:
     *
     * Inserted                 -- an element or comment node was inserted
     * Deleted                  -- an element or comment node was deleted
     * Inserted update="yes"    -- a text node was updated
     * Deleted  update="yes"    -- a text node was updated
     * AttributeInserted        -- an attribute was inserted
     * AttributeDeleted         -- an attribute was deleted
     * AttributeUpdated         -- an attribute was updated
     *
     *
     * @param xml
     * @param difference
     * @throws DiffException
     */
    protected static Element addDifference(Element xml, Element difference, DifferenceDirection direction, boolean isTextUpdate) throws DiffException {
        try {
            if(Log.isDebugEnabled(Geonet.DIFF)) {
                Log.debug(Geonet.DIFF, "\n\n\n\nadding difference " + outputter.outputString(difference) + " in direction " + direction.name());
            }

            String operation = difference.getName();
            Attribute nameAttr = difference.getAttribute("name");

            if (nameAttr != null) {
                String nameAttrVal = nameAttr.getValue();
                if (nameAttrVal.endsWith("nilReason") || nameAttrVal.endsWith("codeList")) {
                    return xml;
                }
            }

            // do not apply insert info to source documents
            if(direction == DifferenceDirection.SOURCE && (operation.equals(INSERTED) || operation.equals(ATTRIBUTE_INSERTED))) {
                Log.debug(Geonet.DIFF, "*** not applying INSERT to SOURCE");
                return xml;
            }
            // do not apply delete info to target documents
            if(direction == DifferenceDirection.TARGET && (operation.equals(DELETED) || operation.equals(ATTRIBUTE_DELETED))) {
                    Log.debug(Geonet.DIFF, "*** not applying DELETE to TARGET");
                return xml;
            }

            String position = difference.getAttribute("pos").getValue();
            String oldPosition = null;
            if(difference.getAttribute(DifferenceAttribute.oldPos.name()) != null) {
                oldPosition = difference.getAttribute(DifferenceAttribute.oldPos.name()).getValue();
            }

            Document doc = new Document((Element)xml.clone());
            String locationPath = getLocationPath(operation, position, oldPosition, direction, isTextUpdate);

            //if(operation.equals(DELETED)) {
                //System.out.println("===========================================================\n"+locationPath+"\n"+Xml.getString(doc)+"\n==============================================");
            //}

            // remove newlines from doc
            doc = ridNewLines(doc);
            Object selected = selectNodeToAnnotate(locationPath, doc);

            // should never happen, but sometimes does happen
            // TODO fix that
            if(selected == null) {
                Log.error(Geonet.DIFF, "Selected 0 nodes for locationpath " + locationPath);
            }
            else {
                //
                // element node : add difference attributes
                //
                if(selected instanceof Element) {
                    Element selectedE = (Element) selected;
                    if(difference.getAttribute(DifferenceAttribute.updatedElement.name(), geonet) != null) {
                        selectedE.setAttribute(DifferenceAttribute.updatedElement.name(), difference.getAttributeValue(DifferenceAttribute.updatedElement.name(), geonet), geonet);
                    }
                    if(operation.equals(INSERTED)) {
                        selectedE.setAttribute(DifferenceAttribute.inserted.name(), "true", geonet);
                        if(StringUtils.isNotEmpty(oldPosition)) {
                            selectedE.setAttribute(DifferenceAttribute.oldPos.name(), oldPosition, geonet);
                        }
                    }
                    else if(operation.equals(DELETED)) {
                        selectedE.setAttribute(DifferenceAttribute.deleted.name(), "true", geonet);
                        if(StringUtils.isNotEmpty(oldPosition)) {
                            selectedE.setAttribute(DifferenceAttribute.oldPos.name(), oldPosition, geonet);
                        }
                    }
                    else if(operation.equals(ATTRIBUTE_INSERTED)) {
                        String attributeName = difference.getAttribute("name").getValue();
                        selectedE.setAttribute(DifferenceAttribute.insertedAttribute.name(), attributeName, geonet);
                        if(StringUtils.isNotEmpty(oldPosition)) {
                            selectedE.setAttribute(DifferenceAttribute.oldPos.name(), oldPosition, geonet);
                        }
                    }
                    else if(operation.equals(ATTRIBUTE_DELETED)) {
                        String attributeName = difference.getAttribute("name").getValue();
                        selectedE.setAttribute(DifferenceAttribute.deletedAttribute.name(), attributeName, geonet);
                        if(StringUtils.isNotEmpty(oldPosition)) {
                            selectedE.setAttribute(DifferenceAttribute.oldPos.name(), oldPosition, geonet);
                        }
                    }
                    else if(operation.equals(ATTRIBUTE_UPDATED)) {
                        String attributeName = difference.getAttribute("name").getValue();
                        selectedE.setAttribute(DifferenceAttribute.updatedAttribute.name(), attributeName, geonet);
                        if(StringUtils.isNotEmpty(oldPosition)) {
                            selectedE.setAttribute(DifferenceAttribute.oldPos.name(), oldPosition, geonet);
                        }
                        if(StringUtils.isNotEmpty(position) && direction == DifferenceDirection.TARGET) {
                            selectedE.setAttribute(DifferenceAttribute.oldPos.name(), position, geonet);
                        }

                        if(Log.isDebugEnabled(Geonet.DIFF)) {
                            Log.debug(Geonet.DIFF, "\n\n\n*** Direction: " + direction.name() + " *** Element after adding:\n" + Xml.getString(selectedE));
                        }
                    }
                    if(operation.equals(DELETED) && Log.isDebugEnabled(Geonet.DIFF)) {
                        Log.debug(Geonet.DIFF, "*** Element after adding:\n" + Xml.getString(selectedE));
                    }
                    addDiffClass(selectedE, position, oldPosition);
                }
                //
                // text node : add difference attributes to its parent
                //
                else if(selected instanceof Text) {
                    Text selectedT = (Text) selected;
                    String textPosition = locationPath.substring(locationPath.lastIndexOf('[')+1, locationPath.lastIndexOf(']'));
                    // go to text node parent
                    locationPath += "/..";
                    XPath xpath = XPath.newInstance(locationPath);
                    // remove newlines from doc
                    doc = ridNewLines(doc);
                    List selectedNodes = xpath.selectNodes(doc);
                    if(selectedNodes.size() != 1) {
                        //return xml;
                        throw new Exception("unexpected # of text node parents selected: " + selectedNodes.size() + " using XPATH " + locationPath);
                    }
                    Element textNodeParent = (Element) selectedNodes.get(0);
                    if(isTextUpdate) {
                        textNodeParent.setAttribute(DifferenceAttribute.updatedText.name(), textPosition, geonet);
                        if(StringUtils.isNotEmpty(oldPosition)) {
                            textNodeParent.setAttribute(DifferenceAttribute.oldPos.name(), oldPosition, geonet);
                        }
                    }
                    else if(operation.equals(INSERTED)) {
                        textNodeParent.setAttribute(DifferenceAttribute.insertedText.name(), textPosition, geonet);
                        if(StringUtils.isNotEmpty(oldPosition)) {
                            textNodeParent.setAttribute(DifferenceAttribute.oldPos.name(), oldPosition, geonet);
                        }
                    }
                    else if(operation.equals(DELETED)) {
                        textNodeParent.setAttribute(DifferenceAttribute.deletedText.name(), textPosition, geonet);
                        if(StringUtils.isNotEmpty(oldPosition)) {
                            textNodeParent.setAttribute(DifferenceAttribute.oldPos.name(), oldPosition, geonet);
                        }
                    }

                    addDiffClass(textNodeParent, position, oldPosition);

                    if(operation.equals(DELETED) && Log.isDebugEnabled(Geonet.DIFF)) {
                        Log.debug(Geonet.DIFF, "*** textnodeparent after adding:\n" + Xml.getString(textNodeParent));
                    }

                }
            }
            return (Element) doc.getRootElement().detach();
        }
        catch(Exception x) {
            Log.error(Geonet.DIFF, x.getMessage());
            x.printStackTrace();
            throw new DiffException(x.getMessage(), x);
        }
    }
}