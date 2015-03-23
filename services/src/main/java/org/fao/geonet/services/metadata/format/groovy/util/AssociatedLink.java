package org.fao.geonet.services.metadata.format.groovy.util;

/**
 * Represent a link and the text for the link.
 *
 * @author Jesse on 11/18/2014.
 */
public class AssociatedLink extends Link {
    private String abstr, logo, metadataId, parentUuid;

    public AssociatedLink(String href, String text) {
        this(href, text, null);

    }

    public AssociatedLink(String href, String text, String cls) {
        super(href, text, cls);
    }

    public String getAbstract() {
        return abstr;
    }

    public void setAbstract(String abstr) {
        this.abstr = abstr;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = "../../" + logo;
        this.logo = this.logo.replaceAll("/+", "/");
    }

    public String getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }
}
