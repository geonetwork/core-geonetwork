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
        @SuppressWarnings("unchecked")
        List<Element> sequence = elChild.getChildren();

        for (Element elElem : sequence) {

            if (isChoiceOrElementOrGroupOrSequence(elElem)) {
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
     * @param elElem
     * @return
     */
    protected boolean isChoiceOrElementOrGroupOrSequence(Element elElem) {
        return elElem.getName().equals("choice") || elElem.getName().equals("element") ||
                elElem.getName().equals("group") || elElem.getName().equals("sequence");
    }

    /**
     * TODO Javadoc.
     *
     * @param ei
     * @param name
     * @return
     */
    protected String handleAttribs(ElementInfo ei, String name) {
        @SuppressWarnings("unchecked")
        List<Attribute> attribs = ei.element.getAttributes();
        for (Attribute at : attribs) {

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