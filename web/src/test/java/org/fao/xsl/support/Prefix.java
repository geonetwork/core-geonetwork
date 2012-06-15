package org.fao.xsl.support;

import org.jdom.Element;

public class Prefix implements Requirement {
    String prefix;
    public Prefix(String prefix) {
        this.prefix = prefix;
    }
    public boolean eval(Element e) {
        return prefix.equals(e.getNamespacePrefix());
    }

    @Override
    public String toString() {
        return "Prefix: "+prefix;
    }
}
