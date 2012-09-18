package org.fao.geonet.kernel.diff;

import fr.loria.ecoo.so6.xml.xydiff.XyDiff;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.util.xml.Namespace;
import org.fao.geonet.util.xml.NamespaceNormalizer;
import org.fao.geonet.util.xml.NormalizedNamespaceMap;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class to generate a representation of the difference between metadata.
 *
 * @author heikki doeleman
 */
public class MetadataDifference {
    private static XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat().setTextMode(Format.TextMode.NORMALIZE));

    private static String stripStyleSheet = "";

    public static void init(String appPath) {
        if (StringUtils.isNotEmpty(stripStyleSheet)) return;

        stripStyleSheet = appPath + Geonet.Path.STYLESHEETS +"/strip.xslt";
    }
    /**
     * Creates an XML document representing the difference between 2 XML documents.
     *
     * @param m1 JDOM xml document
     * @param m2 JDOM xml document
     * @return JDOM xml difference document
     * @throws DiffException hmm
     */
    public static Element diff(Element m1, Element m2) throws DiffException {
        try {
            // strip whitespace-only nodes. This is only to be able to understand the resulting nodepaths better.

            Log.debug(Geonet.DIFF, "stripStyleSheet: " + stripStyleSheet);
            m1 = Xml.transform(m1, stripStyleSheet);
            m2 = Xml.transform(m2, stripStyleSheet);

            NormalizedNamespaceMap normalizedNamespaceMap = NamespaceNormalizer.normalize(m1, m2);

            String m1$ = outputter.outputString(m1);
            String m2$ = outputter.outputString(m2);

            XyDiff xydiff = new XyDiff(m1$, m2$, null);

            String delta$ = xydiff.diff().getDeltaDocument().toString();
            String additionalNamespaces = "";
            for(String normalizedPrefix : normalizedNamespaceMap.normalizedPrefixes()) {
                String uri = normalizedNamespaceMap.getNormalizedNamespaceURI(normalizedPrefix);
                additionalNamespaces += " xmlns:" + normalizedPrefix + "=\"" + uri + "\"" ;
            }
            for(Namespace ns : normalizedNamespaceMap.keySet()){
                String prefix = ns.getPrefix();
                String uri = ns.getURI();
                if(StringUtils.isNotEmpty(prefix) && prefix.startsWith("xxx")) {
                    additionalNamespaces += " xmlns:" + prefix + "=\"" + uri + "\"" ;
                }
            }
            delta$ = delta$.replace("<delta>", "<delta" + additionalNamespaces + ">");

            Log.debug(Geonet.DIFF, "*** DELTADOCUMENT:\n" + delta$);

            Element differences = Xml.loadString(delta$, false);

            differences = processNamespaces(differences);
            differences = pairDeleteAndInserts(differences);

            NamespaceNormalizer.denormalize(m1, m2);

            return differences;
        }
        catch(Exception x) {
            Log.error(Geonet.DIFF, x.getMessage());
            x.printStackTrace();
            throw new DiffException(x.getMessage(), x);
        }
    }

    /**
     * Updates all delete elements that are part of an update, with their corresponding insert element.
     *
     * @param delta
     * @return
     */
    protected static Element pairDeleteAndInserts(Element delta) {
        // for each delete
        List deletedList = delta.getChildren("Deleted");
        for(Object deletedO : deletedList) {
            Element deleted = (Element) deletedO;
            // with update='yes'
            if(deleted.getAttribute("update") != null) {
                pairDeleteAndInsert(deleted, delta);
            }
        }
        return delta;
    }

    /**
     * Updates a delete element that is part of an update, with its corresponding insert element. Warning: this method
     * uses voodoo !
     *
     * @param deleted
     * @param delta
     * @return
     */
    protected static Element pairDeleteAndInsert(Element deleted, Element delta) {
        Log.debug(Geonet.DIFF, "Delete to be paired: " + Xml.getString(deleted));

        String position = deleted.getAttributeValue("pos");
        NodePosition nodePosition = new NodePosition(position);
        Log.debug(Geonet.DIFF, "original position: " + nodePosition);

        // calculate interesting set
        Set<String> interestingSet = nodePosition.interestingSet();
        Log.debug(Geonet.DIFF, "interesting set: " + interestingSet);

        // process deletes in the interesting set
        List deletedInInterestingSetList = delta.getChildren("Deleted");
        for(Object deletedInInterestingSetO : deletedInInterestingSetList) {
            Element deletedInInterestingSet = (Element) deletedInInterestingSetO;
            if(deletedInInterestingSet.getAttribute("update") == null && interestingSet.contains(deletedInInterestingSet.getAttributeValue("pos"))) {
                Log.debug(Geonet.DIFF, "Delete in interesting set: " + Xml.getString(deletedInInterestingSet));
                String[] deletedPositionParts = deletedInInterestingSet.getAttributeValue("pos").split(":");
                nodePosition.decrementAt(deletedPositionParts.length-1);
            }
        }
        Log.debug(Geonet.DIFF, "position after applying deletes: " + nodePosition);

        List insertedList = delta.getChildren("Inserted");
        for(Object insertedO : insertedList) {
            Element inserted = (Element) insertedO;
            interestingSet = nodePosition.interestingSet();
            Log.debug(Geonet.DIFF, "interesting set: " + interestingSet);
            if(inserted.getAttribute("update") == null && interestingSet.contains(inserted.getAttributeValue("pos"))) {
                Log.debug(Geonet.DIFF, "Update in interesting set: " + Xml.getString(inserted));
                String[] insertedPositionParts = inserted.getAttributeValue("pos").split(":");
                nodePosition.incrementAt(insertedPositionParts.length-1);
                Log.debug(Geonet.DIFF, "position after applying insert: " + nodePosition);
            }
        }

        // remove old pos from this delete
        deleted.removeAttribute("pos");

        // add new pos to this delete
        deleted.setAttribute("pos", nodePosition.toString());

        // add updateElement attribute to this delete
        org.jdom.Namespace geonet = org.jdom.Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork");
        deleted.setAttribute("updatedElement", nodePosition.toString(), geonet);

        return deleted;
    }

    /**
     * TODO javadoc.
     *
     * @param delta
     * @return
     */
    private static Element processNamespaces(Element delta) {
        Element processedDelta = new Element("delta");

        List<Element> namespaceChanges = new ArrayList<Element>();
        List<Element> namespaceChangesToStay = new ArrayList<Element>();

        //
        // collect all namespace changes
        //
        for(Object c : delta.getChildren()) {
            Element child = (Element) c;
            if(child.getName().startsWith("Attribute") &&child.getAttribute("name").getValue().startsWith("xmlns")) {
                Log.debug(Geonet.DIFF, "found : " + Xml.getString(child));
                namespaceChanges.add(child);
            }
        }

        //
        // determine if namespace change may stay or will be discarded
        //

        for(Element attributeChange : namespaceChanges) {
            String elementName = attributeChange.getName();
            String pos = attributeChange.getAttribute("pos").getValue();
            String name = attributeChange.getAttribute("name").getValue();
            // the attribute change may stay only if:
            // there exists another attributechange with the same element name and pos, whose name starts with xmlns:xxx
            // default namespace is excluded also
            boolean mayStay = false;
            if(!(name.equals("xmlns") || name.startsWith("xmlns:xxx"))) {
                for(Element ac : namespaceChanges) {
                    String en = ac.getName();
                    String p = ac.getAttribute("pos").getValue();
                    String n = ac.getAttribute("name").getValue();
                    if(en.equals(elementName) && p.equals(pos) && n.startsWith("xmlns:xxx")) {
                        mayStay = true;
                        break;
                    }
                }
            }
            if(mayStay) {
                namespaceChangesToStay.add(attributeChange);
            }
            if(Log.isDebugEnabled(Geonet.DIFF)) {
                Log.debug(Geonet.DIFF, "attribute change " + Xml.getString(attributeChange) + " may stay? " + mayStay);
            }
        }
        //Log.debug(Geonet.DIFF, "attribute changes that may stay: " + namespaceChangesToStay.size());

        //
        // reconstruct delta without the namespace changes that may not stay
        //

        for(Object o : delta.getChildren()) {
            Element e = (Element) o;
            // namespace attribute change
            if(e.getName().startsWith("Attribute") && e.getAttribute("name").getValue().startsWith("xmlns")) {
                boolean mayStay = false;
                // only leave it if it is in attributeChangesToStay
                for(Element staying :  namespaceChangesToStay) {
                    if(staying.equals(e)) {
                        mayStay = true;
                        break;
                    }
                }
                if(mayStay) {
                    if(Log.isDebugEnabled(Geonet.DIFF)) {
                        Log.debug(Geonet.DIFF, "may stay: " + Xml.getString(e));
                    }
                    processedDelta.addContent((Element)e.clone());
                }
            }
            else {
                processedDelta.addContent((Element)e.clone());
            }
        }

        return processedDelta;
    }

}