package org.fao.geonet.services.metadata.format.groovy.util;

/**
 * An Nav Bar item.
 *
 * @author Jesse on 12/1/2014.
 */
public class NavBarItem {
    private String abbrName;
    private String name, rel;

    public NavBarItem() {
        // no op
    }
    public NavBarItem(String name, String abbrName, String rel) {
        this.name = name;
        this.abbrName = abbrName;
        if (abbrName == null || abbrName.isEmpty()) {
            this.abbrName = name;
        }
        this.rel = rel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getAbbrName() {
        return abbrName;
    }

    public void setAbbrName(String abbrName) {
        this.abbrName = abbrName;
    }
}
