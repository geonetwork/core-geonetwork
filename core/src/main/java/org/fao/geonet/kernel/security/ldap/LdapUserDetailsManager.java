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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.AttributesMapperCallbackHandler;
import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.SearchExecutor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper;
import org.springframework.security.ldap.LdapUsernameToDnMapper;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.Assert;

/**
 * Made this class because of the stupid implementation of {@link org.springframework.security.ldap.userdetails.LdapUserDetailsManager#setGroupMemberAttributeName(String)}
 *
 * @author delawen
 */
public class LdapUserDetailsManager implements UserDetailsManager {
    private final Log logger = LogFactory.getLog(LdapUserDetailsManager.class);
    private final String rolePrefix = "ROLE_";
    private final LdapTemplate template;
    /**
     * The strategy for mapping usernames to LDAP distinguished names. This will be used when
     * building DNs for creating new users etc.
     */
    LdapUsernameToDnMapper usernameMapper = new DefaultLdapUsernameToDnMapper(
        "cn=users", "uid");
    /**
     * The DN under which groups are stored
     */
    private DistinguishedName groupSearchBase = new DistinguishedName(
        "cn=groups");
    /**
     * Password attribute name
     */
    private String passwordAttributeName = "userPassword";
    /**
     * The attribute which corresponds to the role name of a group.
     */
    private String groupRoleAttributeName = "cn";
    /**
     * The attribute which contains members of a group
     */
    private String groupMemberAttributeName = "uniquemember";
    /**
     * The pattern to be used for the user search. {0} is the user's DN
     */
    private String groupSearchFilter = "(uniquemember={0})";
    /**
     * The strategy used to create a UserDetails object from the LDAP context, username and list of
     * authorities. This should be set to match the required UserDetails implementation.
     */
    private UserDetailsContextMapper userDetailsMapper = new InetOrgPersonContextMapper();
    /**
     * Default context mapper used to create a set of roles from a list of attributes
     */
    private AttributesMapper roleMapper = new AttributesMapper() {

        public Object mapFromAttributes(Attributes attributes)
            throws NamingException {
            Attribute roleAttr = attributes.get(groupRoleAttributeName);

            NamingEnumeration<?> ne = roleAttr.getAll();
            // assert ne.hasMore();
            Object group = ne.next();
            String role = group.toString();

            return new SimpleGrantedAuthority(rolePrefix + role.toUpperCase());
        }
    };

    private String[] attributesToRetrieve;

    public LdapUserDetailsManager(ContextSource contextSource) {
        template = new LdapTemplate(contextSource);
    }

    public LdapUserDetailsManager(ContextSource contextSource,
                                  String groupMemberAttributeName, String query) {
        template = new LdapTemplate(contextSource);
        this.setGroupMemberAttributeName(groupMemberAttributeName);
        this.groupSearchFilter = "(" + query + ")";
    }

    public UserDetails loadUserByUsername(String username) {
        DistinguishedName dn = usernameMapper.buildDn(username);
        List<GrantedAuthority> authorities = getUserAuthorities(dn, username);

        logger.debug("Loading user '" + username + "' with DN '" + dn + "'");

        DirContextAdapter userCtx = loadUserAsContext(dn, username);

        return userDetailsMapper.mapUserFromContext(userCtx, username,
            authorities);
    }

    private DirContextAdapter loadUserAsContext(final DistinguishedName dn,
                                                final String username) {
        return (DirContextAdapter) template
            .executeReadOnly(new ContextExecutor() {
                public Object executeWithContext(DirContext ctx)
                    throws NamingException {
                    try {
                        Attributes attrs = ctx.getAttributes(dn,
                            attributesToRetrieve);
                        return new DirContextAdapter(attrs, LdapUtils
                            .getFullDn(dn, ctx));
                    } catch (NameNotFoundException notFound) {
                        throw new UsernameNotFoundException("User "
                            + username + " not found", notFound);
                    }
                }
            });
    }

    /**
     * Changes the password for the current user. The username is obtained from the security
     * context. <p> If the old password is supplied, the update will be made by rebinding as the
     * user, thus modifying the password using the user's permissions. If <code>oldPassword</code>
     * is null, the update will be attempted using a standard read/write context supplied by the
     * context source. </p>
     *
     * @param oldPassword the old password
     * @param newPassword the new value of the password.
     */
    public void changePassword(final String oldPassword,
                               final String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        Assert.notNull(
            authentication,
            "No authentication object found in security context. Can't change current user's password!");

        String username = authentication.getName();

        logger.debug("Changing password for user '" + username);

        final DistinguishedName dn = usernameMapper.buildDn(username);
        final ModificationItem[] passwordChange = new ModificationItem[]{new ModificationItem(
            DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
            passwordAttributeName, newPassword))};

        if (oldPassword == null) {
            template.modifyAttributes(dn, passwordChange);
            return;
        }

        template.executeReadWrite(new ContextExecutor() {

            public Object executeWithContext(DirContext dirCtx)
                throws NamingException {
                LdapContext ctx = (LdapContext) dirCtx;
                ctx.removeFromEnvironment("com.sun.jndi.ldap.connect.pool");
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, LdapUtils
                    .getFullDn(dn, ctx).toString());
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, oldPassword);
                // TODO: reconnect doesn't appear to actually change the
                // credentials
                try {
                    ctx.reconnect(null);
                } catch (javax.naming.AuthenticationException e) {
                    throw new BadCredentialsException(
                        "Authentication for password change failed.");
                }

                ctx.modifyAttributes(dn, passwordChange);

                return null;
            }
        });
    }

    /**
     * @param dn       the distinguished name of the entry - may be either relative to the base
     *                 context or a complete DN including the name of the context (either is
     *                 supported).
     * @param username the user whose roles are required.
     * @return the granted authorities returned by the group search
     */
    @SuppressWarnings("unchecked")
    List<GrantedAuthority> getUserAuthorities(final DistinguishedName dn,
                                              final String username) {
        SearchExecutor se = new SearchExecutor() {
            public NamingEnumeration<SearchResult> executeSearch(DirContext ctx)
                throws NamingException {
                DistinguishedName fullDn = LdapUtils.getFullDn(dn, ctx);
                SearchControls ctrls = new SearchControls();
                ctrls.setReturningAttributes(new String[]{groupRoleAttributeName});

                return ctx.search(groupSearchBase, groupSearchFilter,
                    new String[]{fullDn.toUrl(), username}, ctrls);
            }
        };

        AttributesMapperCallbackHandler roleCollector = new AttributesMapperCallbackHandler(
            roleMapper);

        template.search(se, roleCollector);
        return roleCollector.getList();
    }

    public void createUser(UserDetails user) {
        DirContextAdapter ctx = new DirContextAdapter();
        copyToContext(user, ctx);
        DistinguishedName dn = usernameMapper.buildDn(user.getUsername());

        logger.debug("Creating new user '" + user.getUsername() + "' with DN '"
            + dn + "'");

        template.bind(dn, ctx, null);

        // Check for any existing authorities which might be set for this DN and
        // remove them
        List<GrantedAuthority> authorities = getUserAuthorities(dn,
            user.getUsername());

        if (authorities.size() > 0) {
            removeAuthorities(dn, authorities);
        }

        addAuthorities(dn, user.getAuthorities());
    }

    public void updateUser(UserDetails user) {
        DistinguishedName dn = usernameMapper.buildDn(user.getUsername());

        logger.debug("Updating user '" + user.getUsername() + "' with DN '"
            + dn + "'");

        List<GrantedAuthority> authorities = getUserAuthorities(dn,
            user.getUsername());

        DirContextAdapter ctx = loadUserAsContext(dn, user.getUsername());
        ctx.setUpdateMode(true);
        copyToContext(user, ctx);

        // Remove the objectclass attribute from the list of mods (if present).
        List<ModificationItem> mods = new LinkedList<ModificationItem>(
            Arrays.asList(ctx.getModificationItems()));
        ListIterator<ModificationItem> modIt = mods.listIterator();

        while (modIt.hasNext()) {
            ModificationItem mod = (ModificationItem) modIt.next();
            Attribute a = mod.getAttribute();
            if ("objectclass".equalsIgnoreCase(a.getID())) {
                modIt.remove();
            }
        }

        template.modifyAttributes(dn,
            mods.toArray(new ModificationItem[mods.size()]));

        // template.rebind(dn, ctx, null);
        // Remove the old authorities and replace them with the new one
        removeAuthorities(dn, authorities);
        addAuthorities(dn, user.getAuthorities());
    }

    public void deleteUser(String username) {
        DistinguishedName dn = usernameMapper.buildDn(username);
        removeAuthorities(dn, getUserAuthorities(dn, username));
        template.unbind(dn);
    }

    public boolean userExists(String username) {
        DistinguishedName dn = usernameMapper.buildDn(username);

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

    /**
     * Creates a DN from a group name.
     *
     * @param group the name of the group
     * @return the DN of the corresponding group, including the groupSearchBase
     */
    protected DistinguishedName buildGroupDn(String group) {
        DistinguishedName dn = new DistinguishedName(groupSearchBase);
        dn.add(groupRoleAttributeName, group.toLowerCase());

        return dn;
    }

    protected void copyToContext(UserDetails user, DirContextAdapter ctx) {
        userDetailsMapper.mapUserToContext(user, ctx);
    }

    protected void addAuthorities(DistinguishedName userDn,
                                  Collection<? extends GrantedAuthority> authorities) {
        modifyAuthorities(userDn, authorities, DirContext.ADD_ATTRIBUTE);
    }

    protected void removeAuthorities(DistinguishedName userDn,
                                     Collection<? extends GrantedAuthority> authorities) {
        modifyAuthorities(userDn, authorities, DirContext.REMOVE_ATTRIBUTE);
    }

    private void modifyAuthorities(final DistinguishedName userDn,
                                   final Collection<? extends GrantedAuthority> authorities,
                                   final int modType) {
        template.executeReadWrite(new ContextExecutor() {
            public Object executeWithContext(DirContext ctx)
                throws NamingException {
                for (GrantedAuthority authority : authorities) {
                    String group = convertAuthorityToGroup(authority);
                    DistinguishedName fullDn = LdapUtils.getFullDn(userDn, ctx);
                    ModificationItem addGroup = new ModificationItem(modType,
                        new BasicAttribute(groupMemberAttributeName, fullDn
                            .toUrl()));

                    ctx.modifyAttributes(buildGroupDn(group),
                        new ModificationItem[]{addGroup});
                }
                return null;
            }
        });
    }

    private String convertAuthorityToGroup(GrantedAuthority authority) {
        String group = authority.getAuthority();

        if (group.startsWith(rolePrefix)) {
            group = group.substring(rolePrefix.length());
        }

        return group;
    }

    public void setUsernameMapper(LdapUsernameToDnMapper usernameMapper) {
        this.usernameMapper = usernameMapper;
    }

    public void setPasswordAttributeName(String passwordAttributeName) {
        this.passwordAttributeName = passwordAttributeName;
    }

    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = new DistinguishedName(groupSearchBase);
    }

    public void setGroupRoleAttributeName(String groupRoleAttributeName) {
        this.groupRoleAttributeName = groupRoleAttributeName;
    }

    public void setAttributesToRetrieve(String[] attributesToRetrieve) {
        Assert.notNull(attributesToRetrieve);
        this.attributesToRetrieve = Arrays.copyOf(attributesToRetrieve,
            attributesToRetrieve.length);
    }

    public void setUserDetailsMapper(UserDetailsContextMapper userDetailsMapper) {
        this.userDetailsMapper = userDetailsMapper;
    }

    /**
     * Sets the name of the multi-valued attribute which holds the DNs of users who are members of a
     * group. <p> Usually this will be <tt>uniquemember</tt> (the default value) or <tt>member</tt>.
     * </p>
     *
     * @param groupMemberAttributeName the name of the attribute used to store group members.
     */
    public void setGroupMemberAttributeName(String groupMemberAttributeName) {
        Assert.hasText(groupMemberAttributeName);
        this.groupMemberAttributeName = groupMemberAttributeName;
        this.groupSearchFilter = "(" + groupMemberAttributeName + "={0})";
    }

    public void setGroupMemberAttributeName(String groupMemberAttributeName,
                                            Integer index) {
        if (index == null || index < 0) {
            index = 0;
        } else if (index > 1) {
            index = 1;
        }
        Assert.hasText(groupMemberAttributeName);
        this.groupMemberAttributeName = groupMemberAttributeName;
        this.groupSearchFilter = "(" + groupMemberAttributeName + "={" + index
            + "})";
    }

    public void setRoleMapper(AttributesMapper roleMapper) {
        this.roleMapper = roleMapper;
    }
}
