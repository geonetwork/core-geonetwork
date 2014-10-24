package org.fao.geonet.kernel.schema;

import org.jdom.Element;

import java.util.List;

/**
 * Created by francois on 8/20/14.
 */
public interface MultilingualSchemaPlugin {
    /**
     * Return the sub element matching the requested language.
     *
     * @param element   The element to search in
     * @param languageIdentifier    The translation language to search for
     * @return
     */
    public abstract List<Element> getTranslationForElement(Element element, String languageIdentifier);

    public abstract void addTranslationToElement(Element element, String languageIdentifier, String value);

}
