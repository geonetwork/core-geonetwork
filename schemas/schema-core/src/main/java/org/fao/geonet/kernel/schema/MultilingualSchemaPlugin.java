/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.schema;

import org.jdom.Element;
import org.jdom.JDOMException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by francois on 8/20/14.
 */
public interface MultilingualSchemaPlugin {
    /**
     * Return the sub element matching the requested language.
     *
     * @param element            The element to search in
     * @param languageIdentifier The translation language to search for
     */
    public abstract List<Element> getTranslationForElement(Element element, String languageIdentifier);

    /**
     * Updates an element with the related multilingual information using the language and value provided.
     *
     * @param element               XML element to update.
     * @param languageIdentifier    Language identifier.
     * @param value                 Value for the element.
     */
    public abstract void addTranslationToElement(Element element, String languageIdentifier, String value);

    /**
     * Remove all multilingual aspect of an element.
     *
     * @param element               XML element to update.
     * @param mdLang                Metadata languages.
     * @return
     * @throws JDOMException
     */
    public abstract  Element removeTranslationFromElement(Element element, List<String> mdLang) throws JDOMException;

    /**
     * Retrieves the list of metadata languages used in the metadata.
     * @param metadata
     * @return
     */
    public abstract List<String> getMetadataLanguages(Element metadata);

    /**
     * Checks if an element type is multilingual. For example, in DCAT schema, rdf:PlainLiteral type.
     *
     * @param elementType   Element type to check.
     * @return              true if the element type is multilingual, otherwise false.
     */
    public abstract boolean isMultilingualElementType(String elementType);


    /**
     * Flag to indicate when adding an element to the metadata editor, if should be duplicated for each metadata language.
     * For example, in DCAT schema adding vcard:organization-name in a metadata that has English and French languages
     * (similar case for Dublin Core), should duplicate the element for each language:
     *
     *    <vcard:organization-name xml:lang="en"/>
     *    <vcard:organization-name xml:lang="fr"/>
     *
     * For ISO profiles should be set to false, as the multilingual elements are not duplicated. Multilingual values
     * are added as children elements, requiring a different processing. Adding gmd:organisationName in a metadata
     * that has English and French languages:
     *
     * <gmd:organisationName xsi:type="gmd:PT_FreeText_PropertyType">
     *     <gco:CharacterString></gco:CharacterString>
     *     <gmd:PT_FreeText>
     *         <gmd:textGroup>
     *             <gmd:LocalisedCharacterString locale="#EN"></gmd:LocalisedCharacterString>
     *         </gmd:textGroup>
     *         <gmd:textGroup>
     *             <gmd:LocalisedCharacterString locale="#FR"></gmd:LocalisedCharacterString>
     *         </gmd:textGroup>
     *     </gmd:PT_FreeText>
     * </gmd:organisationName>
     *
     * @return
     */
    public abstract boolean duplicateElementsForMultilingual();
}
