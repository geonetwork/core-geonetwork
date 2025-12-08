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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.events.group.GroupCreated;
import org.springframework.context.ApplicationListener;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.directory.Attributes;

/**
 * When a group is created on the database, create it on the LDAP
 *
 * @author delawen
 */
public class AutoCreateGroups implements ApplicationListener<GroupCreated> {
    private static Integer lastGidNumber = null;
    private final Log logger = LogFactory.getLog(AutoCreateGroups.class);
    @SuppressWarnings("unused")
    private ContextSource contextSource;
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
    public void onApplicationEvent(GroupCreated event) {
        Group group = event.getGroup();
        String identifier = group.getName();

        if (!withProfiles) {
            saveGroup(group, identifier);
        } else {
            for (Profile p : Profile.values()) {
                String profile = (profileMapping.containsKey(p.name()) ? profileMapping
                    .get(p.name()) : p.name());
                String id = profilePattern;
                id = id.replace("{0}", profile);
                id = id.replace("{1}", identifier);
                saveGroup(group, id);
            }
        }
    }

    private void saveGroup(Group group, String identifier) {
        // Check the group is not already created:
        if (!groupExists(identifier)) {

            DirContextAdapter ctx = new DirContextAdapter();
            copyToContext(identifier, group, ctx);
            DistinguishedName dn = buildDn(identifier);

            logger.trace("Creating group on LDAP  '" + identifier
                + "' with DN '" + dn + "'");

            template.bind(dn, ctx, null);
        }
    }

    protected void copyToContext(String identifier, Group group,
                                 DirContextAdapter ctx) {
        ctx.setAttributeValues("objectclass", new String[]{"top",
            "posixGroup"});
        ctx.setAttributeValue("cn", identifier);

        if (!ObjectUtils.isEmpty(group.getDescription())) {
            ctx.setAttributeValue("description", group.getDescription());
        }
        // Need unique gidNumber value
        ctx.setAttributeValue("gidNumber", getGidNumber().toString());
    }

    /**
     * @return
     */
    private synchronized Integer getGidNumber() {
        if (lastGidNumber == null) {
            AttributesMapper mapper = new GidAttributesMapper();
            @SuppressWarnings("unchecked")
            List<Integer> existingGroups = template.search(baseDn,
                "(&(objectClass=posixGroup))", mapper);
            for (Integer g : existingGroups) {
                if (lastGidNumber == null || lastGidNumber < g) {
                    AutoCreateGroups.lastGidNumber = g;
                }
            }

            if (AutoCreateGroups.lastGidNumber == null) {
                AutoCreateGroups.lastGidNumber = 0;
            }
        }

        return ++AutoCreateGroups.lastGidNumber;
    }

    public DistinguishedName buildDn(String username) {
        DistinguishedName dn = new DistinguishedName(baseDn);
        dn.add(groupAttribute, username);
        return dn;
    }

    public boolean groupExists(String group) {
        DistinguishedName dn = buildDn(group);

        try {
            Object obj = template.lookup(dn);
            if (obj instanceof Context) {
                LdapUtils.closeContext((Context) obj);
            }
            return true;
        } catch (org.springframework.ldap.NameNotFoundException e) {
            return false;
        }
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
        this.contextSource = contextSource;
        this.template = new LdapTemplate(contextSource);
    }

    public void setProfileMapping(Map<String, String> profileMapping) {
        this.profileMapping = profileMapping;
    }

    private static class GidAttributesMapper implements AttributesMapper {

        public Integer mapFromAttributes(Attributes attributes)
            throws javax.naming.NamingException {
            return Integer.valueOf(attributes.get("gidNumber").get().toString());
        }
    }
}
