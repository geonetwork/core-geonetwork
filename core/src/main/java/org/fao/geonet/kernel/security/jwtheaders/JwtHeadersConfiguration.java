package org.fao.geonet.kernel.security.jwtheaders;

import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.geoserver.security.config.RoleSource;
import org.geoserver.security.jwtheaders.JwtConfiguration;

public class JwtHeadersConfiguration  implements SecurityProviderConfiguration {


    public JwtHeadersConfiguration() {
        jwtConfiguration = new JwtConfiguration();
    }


    public LoginType loginType = LoginType.AUTOLOGIN;

    /**
     *  true -> update the DB with the information from OIDC (don't allow user to edit profile in the UI)
     *  false -> don't update the DB (user must edit profile in UI).
     */
    public boolean updateProfile =true;

    /**
     *  true -> update the DB (user's group) with the information from OIDC (don't allow admin to edit user's groups in the UI)
     *  false -> don't update the DB (admin must edit groups in UI).
     */
    public boolean updateGroup = true;




    // getters/setters


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



    //---- abstract class methods


    @Override
    public String getLoginType() {
        return loginType.toString();
    }

    @Override
    public String getSecurityProvider() {
        return "JWT-HEADERS";
    }

    @Override
    public boolean isUserProfileUpdateEnabled() {
        // If updating profile from the security provider then disable the profile updates in the interface
        return !updateProfile;
    }

    @Override
    public boolean isUserGroupUpdateEnabled() {
        // If updating group from the security provider then disable the group updates in the interface
        return !updateGroup;
    }

    //========================================================================

    protected JwtConfiguration jwtConfiguration;


    public org.geoserver.security.jwtheaders.JwtConfiguration getJwtConfiguration() {
        return jwtConfiguration;
    }

    public void setJwtConfiguration(
        org.geoserver.security.jwtheaders.JwtConfiguration jwtConfiguration) {
        jwtConfiguration = jwtConfiguration;
    }

    //========================================================================

    /** what formats we support for roles in the header. */
    public enum JWTHeaderRoleSource implements RoleSource {
        JSON,
        JWT;

        @Override
        public boolean equals(RoleSource other) {
            return other != null && other.toString().equals(toString());
        }
    }

    //========================================================================



    public String getRoleSource() {
        var val = jwtConfiguration.getJwtHeaderRoleSource();
        return val;
    }

    public void setRoleSource(String roleSource) {
        jwtConfiguration.setJwtHeaderRoleSource(roleSource);
    }



    // ---------------------------------------------------------------------

//    public JwtConfiguration.UserNameHeaderFormat getUserNameFormatChoice() {
//        return jwtConfiguration.getUserNameFormatChoice();
//    }
//
//    public void setUserNameFormatChoice(
//        JwtConfiguration.UserNameHeaderFormat userNameFormatChoice) {
//        jwtConfiguration.setUserNameFormatChoice(userNameFormatChoice);
//    }

    public String getUserNameFormat() {
        if (jwtConfiguration.getUserNameFormatChoice() == null) {
            return null;
        }
        return jwtConfiguration.getUserNameFormatChoice().toString();
    }

    public void setUserNameFormat(String userNameFormat) {
        if (userNameFormat == null) {
            jwtConfiguration.setUserNameFormatChoice(null);
            return;
        }
        var choice = JwtConfiguration.UserNameHeaderFormat.valueOf(userNameFormat);
        jwtConfiguration.setUserNameFormatChoice(choice);
    }

    public String getUserNameJsonPath() {
        return jwtConfiguration.getUserNameJsonPath();
    }

    public void setUserNameJsonPath(String userNameJsonPath) {
        jwtConfiguration.setUserNameJsonPath(userNameJsonPath);
    }

    public String getRolesHeaderName() {
        return jwtConfiguration.getRolesHeaderName();
    }

    public void setRolesHeaderName(String rolesHeaderName) {
        jwtConfiguration.setRolesHeaderName(rolesHeaderName);
    }

    public String getRolesJsonPath() {
        return jwtConfiguration.getRolesJsonPath();
    }

    public void setRolesJsonPath(String rolesJsonPath) {
        jwtConfiguration.setRolesJsonPath(rolesJsonPath);
    }

    public String getRoleConverterString() {
        return jwtConfiguration.getRoleConverterString();
    }

    public void setRoleConverterString(String roleConverterString) {
        jwtConfiguration.setRoleConverterString(roleConverterString);
    }

    public boolean isOnlyExternalListedRoles() {
        return jwtConfiguration.isOnlyExternalListedRoles();
    }

    public void setOnlyExternalListedRoles(boolean onlyExternalListedRoles) {
        jwtConfiguration.setOnlyExternalListedRoles(onlyExternalListedRoles);
    }

    public boolean isValidateToken() {
        return jwtConfiguration.isValidateToken();
    }

    public void setValidateToken(boolean validateToken) {
        jwtConfiguration.setValidateToken(validateToken);
    }

    public boolean isValidateTokenExpiry() {
        return jwtConfiguration.isValidateTokenExpiry();
    }

    public void setValidateTokenExpiry(boolean validateTokenExpiry) {
        jwtConfiguration.setValidateTokenExpiry(validateTokenExpiry);
    }

    public boolean isValidateTokenSignature() {
        return jwtConfiguration.isValidateTokenSignature();
    }

    public void setValidateTokenSignature(boolean validateTokenSignature) {
        jwtConfiguration.setValidateTokenSignature(validateTokenSignature);
    }

    public String getValidateTokenSignatureURL() {
        return jwtConfiguration.getValidateTokenSignatureURL();
    }

    public void setValidateTokenSignatureURL(String validateTokenSignatureURL) {
        jwtConfiguration.setValidateTokenSignatureURL(validateTokenSignatureURL);
    }

    public boolean isValidateTokenAgainstURL() {
        return jwtConfiguration.isValidateTokenAgainstURL();
    }

    public void setValidateTokenAgainstURL(boolean validateTokenAgainstURL) {
        jwtConfiguration.setValidateTokenAgainstURL(validateTokenAgainstURL);
    }

    public String getValidateTokenAgainstURLEndpoint() {
        return jwtConfiguration.getValidateTokenAgainstURLEndpoint();
    }

    public void setValidateTokenAgainstURLEndpoint(String validateTokenAgainstURLEndpoint) {
        jwtConfiguration.setValidateTokenAgainstURLEndpoint(validateTokenAgainstURLEndpoint);
    }

    public boolean isValidateSubjectWithEndpoint() {
        return jwtConfiguration.isValidateSubjectWithEndpoint();
    }

    public void setValidateSubjectWithEndpoint(boolean validateSubjectWithEndpoint) {
        jwtConfiguration.setValidateSubjectWithEndpoint(validateSubjectWithEndpoint);
    }

    public boolean isValidateTokenAudience() {
        return jwtConfiguration.isValidateTokenAudience();
    }

    public void setValidateTokenAudience(boolean validateTokenAudience) {
        jwtConfiguration.setValidateTokenAudience(validateTokenAudience);
    }

    public String getValidateTokenAudienceClaimName() {
        return jwtConfiguration.getValidateTokenAudienceClaimName();
    }

    public void setValidateTokenAudienceClaimName(String validateTokenAudienceClaimName) {
        jwtConfiguration.setValidateTokenAudienceClaimName(validateTokenAudienceClaimName);
    }

    public String getValidateTokenAudienceClaimValue() {
        return jwtConfiguration.getValidateTokenAudienceClaimValue();
    }

    public void setValidateTokenAudienceClaimValue(String validateTokenAudienceClaimValue) {
        jwtConfiguration.setValidateTokenAudienceClaimValue(validateTokenAudienceClaimValue);
    }

    public String getUserNameHeaderAttributeName() {
        return jwtConfiguration.getUserNameHeaderAttributeName();
    }

    public void setUserNameHeaderAttributeName(String userNameHeaderAttributeName) {
        jwtConfiguration.setUserNameHeaderAttributeName(userNameHeaderAttributeName);
    }

}
