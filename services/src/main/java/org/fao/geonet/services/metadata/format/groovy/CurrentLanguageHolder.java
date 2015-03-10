package org.fao.geonet.services.metadata.format.groovy;

/**
 * @author Jesse on 11/28/2014.
 */
public interface CurrentLanguageHolder {
    /**
     * 3 letter language code of the UI.
     */
    String getLang3();

    /**
     * 2 letter language code of the UI.
     */
    String getLang2();
}
