package org.fao.geonet.api.records.model.suggestion;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for suggestionsType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="suggestionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="suggestion" type="{}suggestionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "suggestionsType", propOrder = {
    "suggestion"
})
@XmlRootElement(name = "suggestions")
public class SuggestionsType {

    protected List<SuggestionType> suggestion;

    /**
     * Gets the value of the suggestion property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the suggestion property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSuggestion().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SuggestionType }
     */
    public List<SuggestionType> getSuggestion() {
        if (suggestion == null) {
            suggestion = new ArrayList<SuggestionType>();
        }
        return this.suggestion;
    }

}
