/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.domain.page;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.Nullable;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.Group;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;
/**
 * A page with content and properties
 */
@Entity(name = "SPG_Page")
@Table(name = "SPG_Page")
public class Page extends GeonetEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private PageIdentity pageIdentity;
    private byte[] data;
    private String link;
    private PageFormat format;
    private List<PageSection> sections;
    private PageStatus status;
    private Set<Group> groups = new LinkedHashSet<>();

    private String label;
    private String icon;

    public Page() {

    }

    public Page(PageIdentity pageIdentity, byte[] data, String link, PageFormat format, List<PageSection> sections, PageStatus status, String label, String icon, Set<Group> groups) {
        super();
        this.pageIdentity = pageIdentity;
        this.data = data;
        this.link = link;
        this.format = format;
        this.sections = sections;
        this.status = status;
        this.label = label;
        this.icon = icon;
        this.groups = groups;
    }

    public enum PageStatus {
        PUBLIC, PUBLIC_ONLY, GROUPS, PRIVATE, HIDDEN;
    }

    public enum PageFormat {
        LINK, HTML, HTMLPAGE, TEXT, EMAILLINK;
    }

    // These are the sections where is shown the link to the Page object
    public enum PageSection {
        TOP, FOOTER, MENU, SUBMENU, RECORD_VIEW_MENU, CUSTOM_MENU1, CUSTOM_MENU2, CUSTOM_MENU3;
    }

    public enum PageExtension {
        HTML,
        TXT,
        MD
    }

    @EmbeddedId
    public PageIdentity getPageIdentity() {
        return pageIdentity;
    }

    @Column
    @Nullable
    @Lob
    @Basic(fetch = FetchType.LAZY)
    public byte[] getData() {
        return data;
    }

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(unique = true)
    public String getLink() {
        return link;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public PageFormat getFormat() {
        return format;
    }

    @ElementCollection(targetClass = PageSection.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "SPG_Sections")
    @Column(name = "section")
    public List<PageSection> getSections() {
        return sections;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public PageStatus getStatus() {
        return status;
    }

    @Column(nullable = false)
    public String getLabel() {
        return label;
    }

    @Column
    public String getIcon() {
        return icon;
    }

    /**
     * Get all the page's groups.
     *
     * @return all the page's groups.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "spg_page_group", joinColumns = {@JoinColumn(name = "language"), @JoinColumn(name = "linktext")},
        inverseJoinColumns = {@JoinColumn(name = "groupid", referencedColumnName = "id", unique = false)})
    public Set<Group> getGroups() {
        return groups;
    }

    /**
     * Set all the page's groups.
     *
     * @param groups all the page's groups.
     * @return this group object
     */
    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public void setPageIdentity(PageIdentity pageIdentity) {
        this.pageIdentity = pageIdentity;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setFormat(PageFormat format) {
        this.format = format;
    }

    public void setSections(List<PageSection> sections) {
        this.sections = sections;
    }

    public void setStatus(PageStatus status) {
        this.status = status;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getPageIdentity().getLinkText());
    }

}
