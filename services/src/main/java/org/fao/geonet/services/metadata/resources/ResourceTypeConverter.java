package org.fao.geonet.services.metadata.resources;

import org.apache.commons.lang.WordUtils;

import java.beans.PropertyEditorSupport;

/**
 * Created by francois on 31/12/15.
 */
public class ResourceTypeConverter extends PropertyEditorSupport {
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        setValue(ResourceType.parse(text.trim()));
    }
}
