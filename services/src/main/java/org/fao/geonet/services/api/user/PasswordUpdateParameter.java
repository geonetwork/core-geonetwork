package org.fao.geonet.services.api.user;

/**
 * Created by francois on 09/02/16.
 */
public class PasswordUpdateParameter {
    private String password;
    private String changeKey;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChangeKey() {
        return changeKey;
    }

    public void setChangeKey(String changeKey) {
        this.changeKey = changeKey;
    }
}
