/*
 *  Copyright (C) 2014 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fao.geonet.kernel.security.shibboleth;

import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.springframework.util.StringUtils;

/**
 * Some basic configuration info for Shibboleth logins.
 *
 * Mainly header names mapping to attributes.
 *
 * @author ETj (etj at geo-solutions.it)
 * @author María Arias de Reyna (delawen)
 */
public class ShibbolethUserConfiguration implements SecurityProviderConfiguration {
    private final String SECURITY_PROVIDER = "SHIBBOLETH";
    private String usernameKey;
    private String surnameKey;
    private String firstnameKey;
    private String organisationKey;
    private String profileKey;
    private String groupKey;
    private String emailKey;
    private String roleGroupKey;

    private String defaultGroup;

    private boolean updateProfile;
    private boolean updateGroup;

    private String arraySeparator;
    private String roleGroupSeparator;

    private Boolean hideLogin;

    public String getUsernameKey() {
        return usernameKey;
    }

    public void setUsernameKey(String usernameKey) {
        this.usernameKey = usernameKey;
    }

    public String getSurnameKey() {
        return surnameKey;
    }

    public void setSurnameKey(String surnameKey) {
		if(StringUtils.isEmpty(surnameKey)) {
            surnameKey = "";
        }
        this.surnameKey = surnameKey;
    }

    public String getFirstnameKey() {
        return firstnameKey;
    }

    public void setFirstnameKey(String firstnameKey) {
		if(StringUtils.isEmpty(firstnameKey)) {
            firstnameKey = "";
        }
        this.firstnameKey = firstnameKey;
    }

    public String getOrganisationKey() {
        return organisationKey;
    }

    public void setOrganisationKey(String organisationKey) {
        if(StringUtils.isEmpty(organisationKey)) {
            organisationKey = "";
        }
        this.organisationKey = organisationKey;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
		if(StringUtils.isEmpty(profileKey)) {
            profileKey = "";
        }
        this.profileKey = profileKey;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
		if(StringUtils.isEmpty(groupKey)) {
            groupKey = "";
        }
        this.groupKey = groupKey;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
		if(StringUtils.isEmpty(defaultGroup)) {
            defaultGroup = "";
        }
        this.defaultGroup = defaultGroup;
    }

    public boolean isUpdateProfile() {
        return updateProfile;
    }

    public void setUpdateProfile(boolean updateProfile) {
        this.updateProfile = updateProfile;
    }

    public boolean isUpdateGroup() {
        return updateGroup;
    }

    public void setUpdateGroup(boolean updateGroup) {
        this.updateGroup = updateGroup;
    }

    public String getEmailKey() {
        return emailKey;
    }

    public void setEmailKey(String emailKey) {
		if(StringUtils.isEmpty(emailKey)) {
            emailKey = "";
        }
        this.emailKey = emailKey;
    }

	public String getArraySeparator() {
		return arraySeparator;
	}

	public void setArraySeparator(String arraySeparator) {
		if(StringUtils.isEmpty(arraySeparator)) {
			arraySeparator = ";";
		}
		this.arraySeparator = arraySeparator;
	}

	public Boolean getHideLogin() {
		return hideLogin;
	}

	public void setHideLogin(Boolean hideLogin) {
		if(hideLogin == null) {
			hideLogin = true;
		}
		this.hideLogin = hideLogin;
	}

	public String getRoleGroupKey() {
		return roleGroupKey;
	}

	public void setRoleGroupKey(String roleGroupKey) {
		this.roleGroupKey = roleGroupKey;
	}

	public String getRoleGroupSeparator() {
		return roleGroupSeparator;
	}

	public void setRoleGroupSeparator(String roleGroupSeparator) {
		if(StringUtils.isEmpty(arraySeparator)) {
			arraySeparator = ",";
		}
		this.roleGroupSeparator = roleGroupSeparator;
	}


	public String getSecurityProvider() {
		return SECURITY_PROVIDER;
	}

	@Override
	public String getLoginType() {
        if (this.hideLogin) {
            return LoginType.AUTOLOGIN.toString();
        } else {
            return LoginType.FORM.toString();
        }
    }

    @Override
    public boolean isUserProfileUpdateEnabled() {
        // TODO: returning true to keep old functionality the same.  Need someone to test Shibboleth functionality to validate if disabling the userProfileUpdate is desired.
        return true;
        // If updating profile from the security provider then disable the profile updates in the interface
        //return !updateProfile;
    }

    @Override
    public boolean isUserGroupUpdateEnabled() {
        // TODO: returning true to keep old functionality the same.  Need someone to test Shibboleth functionality to validate if disabling the userProfileUpdate is desired.
        return true;
        // If updating group from the security provider then disable the group updates in the interface
        //return !updateGroup;
    }
}

