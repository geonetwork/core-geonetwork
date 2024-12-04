//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package jeeves.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.fao.geonet.kernel.security.SecurityProviderUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import javax.servlet.http.HttpSession;

import java.io.Serializable;
import java.util.Hashtable;

//=============================================================================

/**
 * Abstraction layer from the user session.
 */
public class UserSession implements Serializable  {

    @JsonProperty
    private Hashtable<String, Object> htProperties = new Hashtable<String, Object>(10, .75f);


    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public UserSession() {
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------


    /**
     * Sets a generic property.
     */
    public void setProperty(String name, Object value) {
        htProperties.put(name, value);
    }

    //--------------------------------------------------------------------------

    /**
     * Gets a generic property.
     */
    public Object getProperty(String name) {
        return htProperties.get(name);
    }

    //--------------------------------------------------------------------------

    /**
     * Removes a generic property.
     */
    public void removeProperty(String name) {
        htProperties.remove(name);
    }

    /**
     * Clears user session properties and authentication.
     */
    public void clear() {
        htProperties.clear();
        SecurityContextHolder.clearContext();
    }

    //--------------------------------------------------------------------------

    public void loginAs(User user) {
        SecurityContextImpl secContext = new SecurityContextImpl();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user, null);
        secContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(secContext);
    }

    @JsonIgnore
    public boolean isAuthenticated() {
        return !(auth() instanceof AnonymousAuthenticationToken);
    }

    //--------------------------------------------------------------------------

    @JsonIgnore
    public String getUserId() {
        User userDetails = getPrincipal();
        if (userDetails == null) {
            return null;
        } else {
            return String.valueOf(userDetails.getId());
        }
    }

    @JsonIgnore
    public String getUsername() {
        User userDetails = getPrincipal();
        if (userDetails == null) {
            return null;
        } else {
            return userDetails.getUsername();
        }
    }

    @JsonIgnore
    public String getName() {
        User userDetails = getPrincipal();
        if (userDetails == null) {
            return null;
        } else {
            return userDetails.getName();
        }
    }

    @JsonIgnore
    public String getSurname() {
        User userDetails = getPrincipal();
        if (userDetails == null) {
            return null;
        } else {
            return userDetails.getSurname();
        }
    }

    @JsonIgnore
    public Profile getProfile() {
        User userDetails = getPrincipal();
        if (userDetails == null) {
            return null;
        } else {
            return userDetails.getProfile();
        }
    }

    @JsonIgnore
    public String getEmailAddr() {
        User userDetails = getPrincipal();
        if (userDetails == null) {
            return null;
        } else {
            return userDetails.getEmail();
        }
    }

    @JsonIgnore
    public String getOrganisation() {
        User userDetails = getPrincipal();
        if (userDetails == null) {
            return null;
        } else {
            return userDetails.getOrganisation();
        }
    }

    @JsonIgnore
    public int getUserIdAsInt() {
        String id = getUserId();
        return id == null ? -1 : Integer.parseInt(getUserId());
    }

    private SecurityContext secContext() {
        return SecurityContextHolder.getContext();
    }

    private Authentication auth() {
        SecurityContext secContext = secContext();
        if (secContext == null) {
            return null;
        } else {
            Authentication authentication = secContext.getAuthentication();
            return authentication;
        }
    }

    @JsonIgnore
    public User getPrincipal() {
        Authentication auth = auth();
        if (auth != null) {
            if (auth.getPrincipal() instanceof User) {
                return (User) auth.getPrincipal();
            } else if (auth.getPrincipal() instanceof LDAPUser) {
                return ((LDAPUser) auth.getPrincipal()).getUser();
            } else {
                // Try to get user Details from other security providers
                SecurityProviderUtil securityProviderUtil = SecurityProviderConfiguration.getSecurityProviderUtil();
                if (securityProviderUtil != null) {
                    return (User)securityProviderUtil.getUserDetails(auth);
                }
            }
        }
        return null;
    }

}

//=============================================================================
