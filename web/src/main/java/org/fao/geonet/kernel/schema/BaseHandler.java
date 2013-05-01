package org.fao.geonet.kernel.schema;

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heikki doeleman
 */
public abstract class BaseHandler {

    /**
     * TODO Javadoc.
     *
     * @param elChild
     * @param alElements
     * @param ei
     */
    protected void handleSequence(Element elChild, ArrayList<ElementEntry> alElements, ElementInfo ei){
        List sequence = elChild.getChildren();

        for (Object aSequence : sequence) {
            Element elElem = (Element) aSequence;

            if (elElem.getName().equals("choice") || elElem.getName().equals("element") ||
                    elElem.getName().equals("group") || elElem.getName().equals("sequence")) {
                alElements.add(new ElementEntry(elElem, ei.file, ei.targetNS, ei.targetNSPrefix));
            }

            else {
                Logger.log();
            }
        }
    }

    /**
     * TODO Javadoc.
     *
     * @param ei
     * @param name
     * @return
     */
    protected String handleAttribs(ElementInfo ei, String name) {
        List attribs = ei.element.getAttributes();
        for (Object attrib : attribs) {
            Attribute at = (Attribute) attrib;

            String attrName = at.getName();
            if (attrName.equals("name")) {
                name = at.getValue();
                if ((name.indexOf(':') == -1) && (ei.targetNSPrefix != null)) {
                    name = ei.targetNSPrefix + ":" + at.getValue();
                }
            }
            else {
                Logger.log();
            }
        }
        return name;
    }
}