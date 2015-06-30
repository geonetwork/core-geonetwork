package org.fao.geonet.transifex.xml;

/**
 * Handles looking up the key and value of a translation from an xml document.
 *
 * @author Jesse on 6/18/2015.
 */
public class TranslationResolver {
    /**
     * The name of the resolver.  Will be used in the name of the transifex file and as an identifier.
     */
    final String name;
    /**
     * The xpath to the element that has the key text of the translation.
     */
    final String keyElem;
    /**
     * The xpath relative to the owner of the key text that selects the translation value.
     * The options here are restricted to attribute selectors, parent selector and child name selectors.  For example:
     *
     * <code>label/@text</code>
     *
     * as is <code>../label/@text</code>
     *
     * but <code>..//label/@text</code> is not allowed
     *
     *
     */
    final String valueXPath;
    public boolean includeTextInKey = true;

    public TranslationResolver(String name, String keyElem, String valueXPath) {
        this.name = name;
        this.keyElem = keyElem;
        this.valueXPath = valueXPath;
    }
}
