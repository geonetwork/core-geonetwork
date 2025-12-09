
package org.fao.geonet.kernel.schema.labels;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.fao.geonet.kernel.schema.labels package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Help_QNAME = new QName("", "help");
    private final static QName _Condition_QNAME = new QName("", "condition");
    private final static QName _Description_QNAME = new QName("", "description");
    private final static QName _Label_QNAME = new QName("", "label");
    private final static QName _InspireInfo_QNAME = new QName("", "inspireInfo");
    private final static QName _Example_QNAME = new QName("", "example");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.fao.geonet.kernel.schema.labels
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Codelists }
     * 
     */
    public Codelists createCodelists() {
        return new Codelists();
    }

    /**
     * Create an instance of {@link Codelists.Codelist }
     * 
     */
    public Codelists.Codelist createCodelistsCodelist() {
        return new Codelists.Codelist();
    }

    /**
     * Create an instance of {@link Helper }
     * 
     */
    public Helper createHelper() {
        return new Helper();
    }

    /**
     * Create an instance of {@link Option }
     * 
     */
    public Option createOption() {
        return new Option();
    }

    /**
     * Create an instance of {@link Labels }
     * 
     */
    public Labels createLabels() {
        return new Labels();
    }

    /**
     * Create an instance of {@link Element }
     * 
     */
    public Element createElement() {
        return new Element();
    }

    /**
     * Create an instance of {@link Codelists.Codelist.Entry }
     * 
     */
    public Codelists.Codelist.Entry createCodelistsCodelistEntry() {
        return new Codelists.Codelist.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "help")
    public JAXBElement<String> createHelp(String value) {
        return new JAXBElement<String>(_Help_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "condition")
    public JAXBElement<String> createCondition(String value) {
        return new JAXBElement<String>(_Condition_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "description")
    public JAXBElement<String> createDescription(String value) {
        return new JAXBElement<String>(_Description_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "label")
    public JAXBElement<String> createLabel(String value) {
        return new JAXBElement<String>(_Label_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "inspireInfo")
    public JAXBElement<String> createInspireInfo(String value) {
        return new JAXBElement<String>(_InspireInfo_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "example")
    public JAXBElement<String> createExample(String value) {
        return new JAXBElement<String>(_Example_QNAME, String.class, null, value);
    }

}
