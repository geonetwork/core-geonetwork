package org.fao.geonet.services.metadata.format.groovy.template;

import org.xml.sax.Attributes;

/**
 * Implementation where the applicable method is based on analyzing the element's attributes and checking if the attribute is one
 * of the "applicable" names.
 *
 * @author Jesse on 11/29/2014.
 */
public abstract class TNodeFactoryByAttName extends TNodeFactory {
    private final String applicableAtt;

    protected TNodeFactoryByAttName(String applicableAtt) {
        this.applicableAtt = applicableAtt;
    }

    @Override
    public final boolean applicable(String localName, String qName, Attributes attributes) {
        return hasAttribute(attributes, applicableAtt);
    }
}
