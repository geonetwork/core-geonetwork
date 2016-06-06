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

/**
 *
 */
package org.fao.geonet.kernel.security.ldap;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.User;
import org.fao.geonet.events.user.GroupLeft;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.ldap.LdapUtils;

import java.util.LinkedList;
import java.util.Map;

import javax.naming.Context;

/**
 * When a user-group relation is removed, removed it also on LDAP
 *
 * @author delawen
 */
public class AutoUpdateUserGroupsOnRemove implements
    ApplicationListener<GroupLeft> {

    @Autowired
    private AbstractLDAPUserDetailsContextMapper ldapMapper;

    private LdapTemplate template;
    private String baseDn = "ou=groups";
    private String groupAttribute = "cn";
    private Boolean withProfiles = true;
    private String profilePattern = "{0}_{1}";
    private Map<String, String> profileMapping;

    /**
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(GroupLeft event) {
        Group group = event.getUserGroup().getGroup();
        User user = event.getUserGroup().getUser();
        String identifier = group.getName();
        String username = user.getUsername();
        String p = event.getUserGroup().getProfile().name();

        if (!withProfiles) {
            saveUserGroup(identifier, username);
        } else {
            String profile = (profileMapping.containsKey(p) ? profileMapping
                .get(p) : p);
            String id = profilePattern;
            id = id.replace("{0}", profile);
            id = id.replace("{1}", identifier);
            saveUserGroup(id, username);
        }
    }

    private void saveUserGroup(String identifier, String username) {
        DirContextAdapter group = getGroup(identifier);

        if (group != null) {
            String[] memberuids = group.getStringAttributes("memberUid");
            if (memberuids == null) {
                memberuids = new String[0];
            }

            java.util.List<String> members = new LinkedList<String>();

            for (int i = 0; i < memberuids.length; i++) {
                if (!memberuids[i].equalsIgnoreCase(username)) {
                    members.add(memberuids[i]);
                }
            }

            String[] newmemberuids = new String[memberuids.length - 1];
            newmemberuids = members.toArray(newmemberuids);

            group.setAttributeValues("memberUid", newmemberuids);

            template.modifyAttributes(group);
            LdapUtils.closeContext((Context) group);
        }
    }

    public DistinguishedName buildDn(String s) {
        DistinguishedName dn = new DistinguishedName(baseDn);
        dn.add(groupAttribute, s);
        return dn;
    }

    public DirContextAdapter getGroup(String group) {
        DistinguishedName dn = buildDn(group);

        try {
            Object obj = template.lookup(dn);
            if (obj instanceof DirContextAdapter) {
                return (DirContextAdapter) obj;
            }
            return null;
        } catch (org.springframework.ldap.NameNotFoundException e) {
            return null;
        }
    }

    public void setLdapMapper(AbstractLDAPUserDetailsContextMapper ldapMapper) {
        this.ldapMapper = ldapMapper;
    }

    public void setTemplate(LdapTemplate template) {
        this.template = template;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public void setGroupAttribute(String groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

    public void setWithProfiles(Boolean withProfiles) {
        this.withProfiles = withProfiles;
    }

    public void setProfilePattern(String profilePattern) {
        this.profilePattern = profilePattern;
    }

    public void setContextSource(ContextSource contextSource) {
        this.template = new LdapTemplate(contextSource);
    }

    public void setProfileMapping(Map<String, String> profileMapping) {
        this.profileMapping = profileMapping;
    }
}
