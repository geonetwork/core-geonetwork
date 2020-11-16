//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.ldap;

import com.google.common.annotations.VisibleForTesting;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.security.ldap.LDAPConstants;
import org.fao.geonet.kernel.security.ldap.LDAPUserDetailsContextMapper;
import org.fao.geonet.kernel.security.ldap.SextantLDAPUserDetailsContextMapper;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.utils.Log;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.MessageFormat;
import java.util.List;

/**
 * Check all LDAP users in database and check the profile in the LDAP.
 * If the LDAP profile does not match, update the db profile to the LDAP profile.
 */
public class LDAPProfileToDbUserSynchronizer {

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    private LdapTemplate ldapTemplate;

    /** Let's say it won't work with another contextMapper */
    @Autowired
    private SextantLDAPUserDetailsContextMapper ldapUserContextMapper;

    @Autowired
    private UserRepository userRepository;

    @Value("${ldap.base.search.filter:(uid={0})}")
    private String userBaseSearchFilter;

    @Value("${ldap.base.search.base:ou=annuaire}")
    private String userSearchBase;

    protected Logger logger = Log.createLogger(Log.JEEVES);

    @VisibleForTesting
    public void doExecute() {
        List users = findAllUsersWithLdapAuth();
        for (Object user : users) {
            User usr = (User) user;
            String userName = usr.getUsername();
            User usrFromLdap = findSingleUserInLdap(userName);

            if (usrFromLdap == null) {
                logger.warning(String.format("User %s exists in db but cannot be found in the LDAP", userName));
                continue;
            }

            if (! usrFromLdap.getProfile().equals(usr.getProfile())) {
                logger.info(String.format("Profile in the LDAP does not match actual user's profile" +
                    " for user %s (%s -> %s)", userName, usrFromLdap.getProfile(), usr.getProfile()));
                usr.setProfile(usrFromLdap.getProfile());
                userRepository.saveAndFlush(usr);
            }
        }
    }

    /**
     * find all users flagged as LDAP users '
     * @return a
     */
    @VisibleForTesting
    public List<User> findAllUsersWithLdapAuth() {
        return userRepository.findAll(
            (root, query, cb) ->
                cb.equal(root.get(User_.security).get(UserSecurity_.authType), LDAPConstants.LDAP_FLAG)
        );
    }

    @VisibleForTesting
    public User findSingleUserInLdap(String uid) {
        String filter = MessageFormat.format(this.userBaseSearchFilter, uid);
        Object usr = null;
        try {
            usr = ldapTemplate.searchForObject(userSearchBase, filter, new ContextMapper() {
                @Override
                public Object mapFromContext(Object ctx) {
                    DirContextAdapter adapted = (DirContextAdapter) ctx;
                    String uid = adapted.getStringAttribute("uid");
                    LDAPUser lu = ldapUserContextMapper.getGeoNetworkUserFromContext(uid, adapted);
                    return lu.getUser();
                }
            });
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
        return (User) usr;
    }

    private Profile lookupUserProfileInLdap(String username) {
        return Profile.RegisteredUser;
    }

}
