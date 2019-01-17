package org.fao.geonet.api.pages;

import java.io.Serializable;
import java.util.List;

import org.fao.geonet.domain.page.Page;
import org.fao.geonet.domain.page.Page.PageFormat;
import org.fao.geonet.domain.page.Page.PageSection;
import org.fao.geonet.domain.page.Page.PageStatus;

// Wrapper to filter the fields shown on JSON
public class PageJSONWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    private Page page;

    public PageJSONWrapper(Page p) {
        page = p;
    }

    public String getLinkText() {
        return page.getPageIdentity().getLinkText();
    }

    public String getLanguage() {
        return page.getPageIdentity().getLanguage();
    }

    public PageFormat getFormat() {
        return page.getFormat();
    }

    public String getLink() {
        return page.getLink();
    }

    public List<PageSection> getSections() {
        return page.getSections();
    }

    public PageStatus getStatus() {
        return page.getStatus();
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getLinkText());
    }

}
