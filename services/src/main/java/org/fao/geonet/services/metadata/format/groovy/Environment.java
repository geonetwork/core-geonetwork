package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Multimap;
import org.springframework.security.core.Authentication;

import java.util.Collection;

/**
 * Contains methods for accessing data regarding the current user and request, such as the current UI language, username, the
 * parameters passed to the format request, etc...
 *
 * <strong>IMPORTANT:</strong> This class cannot be used while registering handlers, sorters, etc...  Only when the XML processing
 * is under way can it be used.  If used during setup of the handlers then an exception will be thrown.  The reason this object is
 * bound to the script is so that it can be used within any handler and object created can be passed the object for use without having
 * to do any crazy gymnastics.
 *
 * @author Jesse on 10/20/2014.
 */
public interface Environment {
    /**
     * 3 letter language code of the UI.
     */
    public String getLang3();
    /**
     * 2 letter language code of the UI.
     */
    public String getLang2();
    /**
     * Return the id of the metadata
     */
    public int getMetadataId();
    /**
     * Return the UUID of the metadata
     */
    public String getMetadataUUID();
    /**
     * The URL to use in the html when accessing resources in the formatter resource package.  Simply append the relative path of the
     * resource from the formatter bundle directory.
     */
    public String getResourceUrl();
    /**
     * Get the current user's authentication information.
     */
    public Authentication getAuth();
    /**
     * Return the map of all parameters passed to the Format service.
     */
    public Multimap<String, ParamValue> params();
    /**
     * Return the value of the first parameter with the provided name.  Null is returned if there is no parameter with the given name.
     */
    public ParamValue param(String paramName);
    /**
     * Return ALL values of parameter with the provided name.
     */
    public Collection<ParamValue> paramValues(String paramName);
}
