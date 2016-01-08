package org.fao.geonet.services.metadata.resources;

import java.beans.PropertyEditorSupport;

/**
 * Created by francois on 31/12/15.
 */
public class SortConverter extends PropertyEditorSupport {
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        setValue(Sort.valueOf(text.trim()));
    }
}
