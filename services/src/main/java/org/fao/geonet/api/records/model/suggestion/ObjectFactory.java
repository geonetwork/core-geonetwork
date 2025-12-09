package org.fao.geonet.api.records.model.suggestion;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.fao.geonet.api.records package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Suggestions_QNAME = new QName("", "suggestions");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.fao.geonet.api.records
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SuggestionsType }
     */
    public SuggestionsType createSuggestionsType() {
        return new SuggestionsType();
    }

    /**
     * Create an instance of {@link SuggestionType }
     */
    public SuggestionType createSuggestionType() {
        return new SuggestionType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SuggestionsType }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "suggestions")
    public JAXBElement<SuggestionsType> createSuggestions(SuggestionsType value) {
        return new JAXBElement<SuggestionsType>(_Suggestions_QNAME, SuggestionsType.class, null, value);
    }

}
