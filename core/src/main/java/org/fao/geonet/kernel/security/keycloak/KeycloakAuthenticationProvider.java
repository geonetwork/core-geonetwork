/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.keycloak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.account.KeycloakRole;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *  Class for managing the KeycloakAuthenticationProvider GrantedAuthority
 *  Based on the following
 *  https://gist.github.com/thomasdarimont/860a8a8420762c14d57766425b036c13
 *
 *  It will use initial roles from Keycloak and additional hierarchical spring role
 *
 */
public class KeycloakAuthenticationProvider extends org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider {

    private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @Autowired
    private KeycloakUserUtils keycloakUserUtils;

    /**
     * Constructor for setting up the initial mappers and getting the spring roleHierarchy.
     * @param roleHierarchy from spring configuration
     */
    @Autowired
    public KeycloakAuthenticationProvider(RoleHierarchy roleHierarchy) {
        SimpleAuthorityMapper grantedAuthorityMapper = new SimpleAuthorityMapper();
        // Remove the role prefix
        grantedAuthorityMapper.setPrefix("");

        // Create new KeycloakRoleHierarchyAuthoritiesMapper based on existing roleHierarchy and the
        KeycloakRoleHierarchyAuthoritiesMapper resolvingMapper = new KeycloakRoleHierarchyAuthoritiesMapper(roleHierarchy,
            grantedAuthorityMapper);

        this.grantedAuthoritiesMapper = resolvingMapper;
    }

    /**
     * Authenticate current keycloak token
     *
     * @param authentication to be authenticated
     * @return Authentication token
     * @throws AuthenticationException if there were errors.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;

        Collection<? extends GrantedAuthority> keycloakAuthorities = mapAuthorities(getKeycloakRoles(token));
        Collection<? extends GrantedAuthority> grantedAuthorities = addUserSpecificAuthorities(authentication,
            keycloakAuthorities);

        return new KeycloakAuthenticationToken(token.getAccount(), token.isInteractive(), grantedAuthorities);
    }

    /**
     * This is where the roles based on keycloak roles are modified and merged
     * @param authentication to get the current keycloak roles from
     * @param authorities existing authorities from keycloak.
     * @return new authorities containing the merged roles.
     */
    protected Collection<? extends GrantedAuthority> addUserSpecificAuthorities(
        Authentication authentication,
        Collection<? extends GrantedAuthority> authorities
    ) {

        List<GrantedAuthority> result = new ArrayList<>();
        result.addAll(authorities);

        if (authentication.getPrincipal() instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) authentication.getPrincipal();
            if (keycloakPrincipal != null) {
                UserDetails userDetails = keycloakUserUtils.getUserDetails(keycloakPrincipal.getKeycloakSecurityContext().getToken(), true);
                if (userDetails != null && userDetails.getAuthorities() != null) {
                    result.addAll(userDetails.getAuthorities());
                }
            }
        }

        return result;
    }

    /**
     * Get keycloak roles from token.
     *
     * @param token containing the roles
     * @return the roles as grantedAuthorities
     */
    protected Collection<? extends GrantedAuthority> getKeycloakRoles(KeycloakAuthenticationToken token) {

        Collection<GrantedAuthority> keycloakRoles = new ArrayList<>();

        for (String role : token.getAccount().getRoles()) {
            keycloakRoles.add(new KeycloakRole(role));
        }

        return keycloakRoles;
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return grantedAuthoritiesMapper != null ? grantedAuthoritiesMapper.mapAuthorities(authorities) : authorities;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return KeycloakAuthenticationToken.class.isAssignableFrom(aClass);
    }

    /**
     * Keycloak Role Hierarchy Authorities Mapper class will be used merge the existing keycloak roles
     * and spring RoleHierarchy into one
     */
    public class KeycloakRoleHierarchyAuthoritiesMapper extends RoleHierarchyAuthoritiesMapper {

        private final GrantedAuthoritiesMapper delegate;

        /**
         * Constructor to initialize the object.
         *
         * @param roleHierarchy spring roleHierarchy
         * @param delegate granted authorities mapper
         */
        public KeycloakRoleHierarchyAuthoritiesMapper(RoleHierarchy roleHierarchy, GrantedAuthoritiesMapper delegate) {
            super(roleHierarchy);
            this.delegate = delegate;
        }

        /**
         * Get map athorities
         *
         * @param authorities
         * @return collection of GrantedAuthority
         */
        @Override
        public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {

            // Transform roles if necessary
            Collection<? extends GrantedAuthority> transformedAuthorities = delegate.mapAuthorities(authorities);

            // Roles resolved via role hierarchy
            Collection<? extends GrantedAuthority> expanededAuthorities = super.mapAuthorities(transformedAuthorities);

            return expanededAuthorities;
        }
    }
}
