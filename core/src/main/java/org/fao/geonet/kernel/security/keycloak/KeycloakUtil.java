package org.fao.geonet.kernel.security.keycloak;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.utils.Log;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.annotation.PostConstruct;

public class KeycloakUtil {
    public static String signinPath = null;
    private static LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

    @Autowired
    private LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint0;

    @PostConstruct
    private void init () {
        loginUrlAuthenticationEntryPoint = this.loginUrlAuthenticationEntryPoint0;
    }

    public static String getSigninPath() {
        if (signinPath == null) {
            try {
                signinPath = loginUrlAuthenticationEntryPoint.getLoginFormUrl().split("\\?")[0];
            } catch(BeansException e) {
                // If we cannot find the bean then we will just use a default.
            }
            // If signinPath is null then something may have gone wrong.
            // This should generally not happen - if it does then lets set to what it currently expected and then log a warning.
            if (StringUtils.isEmpty(signinPath)) {
                signinPath = "/signin";
                Log.warning(Log.JEEVES,
                        "Could not detect signin path from configuration. Using /signin");
            }
        }
        return signinPath;
    }
}
