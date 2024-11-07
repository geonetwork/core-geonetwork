package org.fao.geonet.api.pages;

import org.apache.commons.collections4.CollectionUtils;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.page.Page;
import org.fao.geonet.domain.page.Page.PageFormat;
import org.fao.geonet.domain.page.Page.PageSection;
import org.fao.geonet.domain.page.Page.PageStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PageProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private String language;
    private String pageId;
    private List<Page.PageSection> sections;
    private Page.PageStatus status;
    private String link;
    private String content;
    private String label;
    private Page.PageFormat format;
    private List<String> groups;
    private Page page;

    public PageProperties() {
    }

    public PageProperties(Page p) {
        page = p;
        language = p.getPageIdentity().getLanguage();
        pageId = p.getPageIdentity().getLinkText();
        format = p.getFormat();
        link = p.getLink();
        sections = p.getSections();
        status = p.getStatus();
        label = p.getLabel();
        if (CollectionUtils.isNotEmpty(p.getGroups())) {
            groups = new ArrayList<>();
            for (Group g : p.getGroups()) {
                groups.add(g.getName());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getPageId());
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public List<PageSection> getSections() {
        return sections;
    }

    public void setSections(List<PageSection> sections) {
        this.sections = sections;
    }

    public PageStatus getStatus() {
        return status;
    }

    public void setStatus(PageStatus status) {
        this.status = status;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PageFormat getFormat() {
        return format;
    }

    public void setFormat(PageFormat format) {
        this.format = format;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
