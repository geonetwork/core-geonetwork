package org.fao.geonet.api;

import jeeves.server.UserSession;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.UserUtil;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;

public class UserProfileUtil {

    /**
     * Checks if the user session's profile is allowed to do the transaction
     *
     * @param userSession current user session
     * @param settingManager setting manager bean
     * @param roleHierarchy role hierarchy bean
     * @param settingConfigPath setting config path check org.fao.geonet.kernel.setting.Settings
     * @param defaultProfile default configuration profile is no configuration found
     * @param errorText error text to the exception
     */
    public static void checkUserProfileLevel (UserSession userSession, SettingManager settingManager, RoleHierarchy roleHierarchy, String settingConfigPath, Profile defaultProfile, String errorText) {
        if (userSession.getProfile() != Profile.Administrator) {
            String allowedUserProfileFromConfiguration =
                StringUtils.defaultIfBlank(settingManager.getValue(settingConfigPath), defaultProfile.toString());

            // Is the user profile higher than the configuration profile allowed to do the transaction?
            if (!UserUtil.hasHierarchyRole(allowedUserProfileFromConfiguration, roleHierarchy)) {
                throw new NotAllowedException("The user has no permissions to " + errorText);
            }
        }

    }
}
