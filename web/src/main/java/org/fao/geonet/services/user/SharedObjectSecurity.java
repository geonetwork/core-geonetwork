package org.fao.geonet.services.user;

import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

public class SharedObjectSecurity {

    public static void checkPermitted(Element affectedProfile, String editorProfile) {
        if(affectedProfile!=null) {
            checkPermitted(affectedProfile.getTextNormalize(),editorProfile);
        }
    }
    public static void checkPermitted(String affectedProfile, String editorProfile) {
        boolean hasAccess = !affectedProfile.equals(Geocat.Profile.SHARED) || editorProfile.equalsIgnoreCase(Geonet.Profile.ADMINISTRATOR);
        if (!hasAccess) {
            throw new SecurityException("Current User is not permitted to edit shared objects");
        }

    }
}
