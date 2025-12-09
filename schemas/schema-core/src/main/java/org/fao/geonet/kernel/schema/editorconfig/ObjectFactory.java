/*
 *
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

package org.fao.geonet.kernel.schema.editorconfig;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each Java content interface and Java element interface
 * generated in the org.fao.geonet.kernel.schema.editorconfig package. <p>An ObjectFactory allows
 * you to programatically construct new instances of the Java representation for XML content. The
 * Java representation of XML content can consist of schema derived interfaces and classes
 * representing the binding of schema type definitions, element declarations and model groups.
 * Factory methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Name_QNAME = new QName("", "name");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes
     * for package: org.fao.geonet.kernel.schema.editorconfig
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Key }
     */
    public Key createKey() {
        return new Key();
    }

    /**
     * Create an instance of {@link ThesaurusList }
     */
    public ThesaurusList createThesaurusList() {
        return new ThesaurusList();
    }

    /**
     * Create an instance of {@link BatchEditing }
     */
    public BatchEditing createBatchEditing() {
        return new BatchEditing();
    }

    /**
     * Create an instance of {@link BatchEditing.Section }
     */
    public BatchEditing.Section createBatchEditingSection() {
        return new BatchEditing.Section();
    }

    /**
     * Create an instance of {@link Template }
     */
    public Template createTemplate() {
        return new Template();
    }

    /**
     * Create an instance of {@link Values }
     */
    public Values createValues() {
        return new Values();
    }

    /**
     * Create an instance of {@link Key.Codelist }
     */
    public Key.Codelist createKeyCodelist() {
        return new Key.Codelist();
    }

    /**
     * Create an instance of {@link Key.Helper }
     */
    public Key.Helper createKeyHelper() {
        return new Key.Helper();
    }

    /**
     * Create an instance of {@link DirectiveAttributes }
     */
    public DirectiveAttributes createDirectiveAttributes() {
        return new DirectiveAttributes();
    }

    /**
     * Create an instance of {@link Snippet }
     */
    public Snippet createSnippet() {
        return new Snippet();
    }

    /**
     * Create an instance of {@link Editor }
     */
    public Editor createEditor() {
        return new Editor();
    }

    /**
     * Create an instance of {@link Fields }
     */
    public Fields createFields() {
        return new Fields();
    }

    /**
     * Create an instance of {@link For }
     */
    public For createFor() {
        return new For();
    }

    /**
     * Create an instance of {@link FieldsWithFieldset }
     */
    public FieldsWithFieldset createFieldsWithFieldset() {
        return new FieldsWithFieldset();
    }

    /**
     * Create an instance of {@link MultilingualFields }
     */
    public MultilingualFields createMultilingualFields() {
        return new MultilingualFields();
    }

    /**
     * Create an instance of {@link Expanded }
     */
    public Expanded createExpanded() {
        return new Expanded();
    }

    /**
     * Create an instance of {@link Exclude }
     */
    public Exclude createExclude() {
        return new Exclude();
    }

    /**
     * Create an instance of {@link Views }
     */
    public Views createViews() {
        return new Views();
    }

    /**
     * Create an instance of {@link View }
     */
    public View createView() {
        return new View();
    }

    /**
     * Create an instance of {@link Tab }
     */
    public Tab createTab() {
        return new Tab();
    }

    /**
     * Create an instance of {@link org.fao.geonet.kernel.schema.editorconfig.Section }
     */
    public org.fao.geonet.kernel.schema.editorconfig.Section createSection() {
        return new org.fao.geonet.kernel.schema.editorconfig.Section();
    }

    /**
     * Create an instance of {@link org.fao.geonet.kernel.schema.editorconfig.Field }
     */
    public org.fao.geonet.kernel.schema.editorconfig.Field createField() {
        return new org.fao.geonet.kernel.schema.editorconfig.Field();
    }

    /**
     * Create an instance of {@link Action }
     */
    public Action createAction() {
        return new Action();
    }

    /**
     * Create an instance of {@link Text }
     */
    public Text createText() {
        return new Text();
    }

    /**
     * Create an instance of {@link Fieldset }
     */
    public Fieldset createFieldset() {
        return new Fieldset();
    }

    /**
     * Create an instance of {@link FlatModeExceptions }
     */
    public FlatModeExceptions createFlatModeExceptions() {
        return new FlatModeExceptions();
    }

    /**
     * Create an instance of {@link ThesaurusList.Thesaurus }
     */
    public ThesaurusList.Thesaurus createThesaurusListThesaurus() {
        return new ThesaurusList.Thesaurus();
    }

    /**
     * Create an instance of {@link BatchEditing.Section.Field }
     */
    public BatchEditing.Section.Field createBatchEditingSectionField() {
        return new BatchEditing.Section.Field();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }

}
