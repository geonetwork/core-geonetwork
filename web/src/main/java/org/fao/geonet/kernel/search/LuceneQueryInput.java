//==============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Input to {@link LuceneQueryBuilder}, consisting of user-provided parameters plus system-provided parameters.
 *
 * @author heikki doeleman
 * 
 */
public class LuceneQueryInput extends UserQueryInput {

    private String owner;
    private Set<String> groups;
    private Set<String> groupOwners;
    private boolean isReviewer;
    private boolean isUserAdmin;
    private boolean isAdmin;

    private String requestedLanguageOnly;

    public String isRequestedLanguageOnly() {
        return requestedLanguageOnly;
    }

    public void setRequestedLanguageOnly(String requestedLanguageOnly) {
        this.requestedLanguageOnly = requestedLanguageOnly;
    }

    /**
     * Creates this from a JDOM element.
     * 
     * @param jdom input
     */
    public LuceneQueryInput(Element jdom) {

        super(jdom);

        setOwner(jdom.getChildText(SearchParameter.OWNER));

        @SuppressWarnings("unchecked")
        List<Element> groupsE = (List<Element>)jdom.getChildren(SearchParameter.GROUP);
        if(groupsE != null) {
            Set<String> groups = new HashSet<String>();
            for(Element groupE : groupsE) {
                groups.add(groupE.getText());
            }
            setGroups(groups);
        }
        @SuppressWarnings("unchecked")
        List<Element> groupOwnersE = (List<Element>)jdom.getChildren(SearchParameter.GROUPOWNER);
        Set<String> groupOwners = new HashSet<String>();
        for(Element groupOwnerE : groupOwnersE) {
            groupOwners.add(groupOwnerE.getText());
        }
        setGroupOwners(groupOwners);

        Element isReviewerE = jdom.getChild(SearchParameter.ISREVIEWER);
        setReviewer(isReviewerE != null);

        Element isUserAdminE = jdom.getChild(SearchParameter.ISUSERADMIN);
        setUserAdmin(isUserAdminE != null);

        Element isAdminE = jdom.getChild(SearchParameter.ISADMIN);
        setAdmin(isAdminE != null);

        Element isEditable = jdom.getChild(SearchParameter.EDITABLE);
        if(isEditable != null && StringUtils.isNotEmpty(isEditable.getText())) {
            setEditable(isEditable.getText());
        }
        else {
            setEditable("false");
        }

    }

    /**
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer groupsToString = new StringBuffer();
        for(String group : groups) {
            groupsToString.append(" group: " + group);
        }
        StringBuffer groupOwnersToString = new StringBuffer();
        for(String groupOwner : groupOwners) {
            groupOwnersToString.append(" groupOwner: " + groupOwner);
        }
        return new StringBuilder().append("owner:").append(owner)
                .append(groupsToString)
                .append(groupOwnersToString)
                .append(" isReviewer:").append(isReviewer)
                .append(" isUserAdmin:").append(isUserAdmin)
                .append(" isAdmin:").append(isAdmin)
                .append(" ").append(super.toString()).toString();
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        if(this.groups == null) {
            this.groups = new HashSet<String>();
        }
        this.groups = groups;
    }

    public boolean getReviewer() {
        return isReviewer;
    }

    public void setReviewer(boolean reviewer) {
        isReviewer = reviewer;
    }

    public boolean getUserAdmin() {
        return isUserAdmin;
    }

    public void setUserAdmin(boolean userAdmin) {
        isUserAdmin = userAdmin;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Set<String> getGroupOwners() {
        return groupOwners;
    }

    public void setGroupOwners(Set<String> groupOwners) {
        if(this.groupOwners == null) {
            this.groupOwners = new HashSet<String>();
        }
        this.groupOwners = groupOwners;
    }
}