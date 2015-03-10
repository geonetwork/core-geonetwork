package org.fao.geonet.services.metadata.format.groovy.util;

/**
 * An Nav Bar item.
 *
 * @author Jesse on 12/1/2014.
 */
public class NavBarItem {
    private String abbrName;
    private String name, rel, href;

    public NavBarItem() {
        // no op
    }

    /**
     * Constructor.
     * @param name the translated full name of the group represented by this item.  This will be displayed as a tool tip (see view-header.html)
     * @param abbrName a shorter translated name to use in the display to keep the size of the items reasonable.
     * @param rel the value of the rel attribute
     */
    public NavBarItem(String name, String abbrName, String rel) {
        this(name, abbrName, rel, "");
    }
    /**
     * Constructor.
     * @param name the translated full name of the group represented by this item.  This will be displayed as a tool tip (see view-header.html)
     * @param abbrName a shorter translated name to use in the display to keep the size of the items reasonable.
     * @param rel the value of the rel attribute
     * @param href the value of the href attribute
     */
    public NavBarItem(String name, String abbrName, String rel, String href) {
        this.name = name;
        this.abbrName = abbrName;
        if (abbrName == null || abbrName.isEmpty()) {
            this.abbrName = name;
        }
        this.rel = rel;
        this.href = href;
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

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
