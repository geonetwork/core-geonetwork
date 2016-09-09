
package org.fao.geonet.api.records.model.validation;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.fao.geonet.api.records.model.validation package. 
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

    private final static QName _Msg_QNAME = new QName("", "msg");
    private final static QName _Total_QNAME = new QName("", "total");
    private final static QName _Test_QNAME = new QName("", "test");
    private final static QName _Success_QNAME = new QName("", "success");
    private final static QName _DisplayPriority_QNAME = new QName("", "displayPriority");
    private final static QName _Details_QNAME = new QName("", "details");
    private final static QName _Id_QNAME = new QName("", "id");
    private final static QName _Label_QNAME = new QName("", "label");
    private final static QName _Requirement_QNAME = new QName("", "requirement");
    private final static QName _Title_QNAME = new QName("", "title");
    private final static QName _Error_QNAME = new QName("", "error");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.fao.geonet.api.records.model.validation
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Patterns }
     * 
     */
    public Patterns createPatterns() {
        return new Patterns();
    }

    /**
     * Create an instance of {@link Pattern }
     * 
     */
    public Pattern createPattern() {
        return new Pattern();
    }

    /**
     * Create an instance of {@link Rules }
     * 
     */
    public Rules createRules() {
        return new Rules();
    }

    /**
     * Create an instance of {@link Report }
     * 
     */
    public Report createReport() {
        return new Report();
    }

    /**
     * Create an instance of {@link Rule }
     * 
     */
    public Rule createRule() {
        return new Rule();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "msg")
    public JAXBElement<String> createMsg(String value) {
        return new JAXBElement<String>(_Msg_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "total")
    public JAXBElement<String> createTotal(String value) {
        return new JAXBElement<String>(_Total_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "test")
    public JAXBElement<String> createTest(String value) {
        return new JAXBElement<String>(_Test_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "success")
    public JAXBElement<String> createSuccess(String value) {
        return new JAXBElement<String>(_Success_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "displayPriority")
    public JAXBElement<BigInteger> createDisplayPriority(BigInteger value) {
        return new JAXBElement<BigInteger>(_DisplayPriority_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "details")
    public JAXBElement<String> createDetails(String value) {
        return new JAXBElement<String>(_Details_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createId(String value) {
        return new JAXBElement<String>(_Id_QNAME, String.class, null, value);
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
    @XmlElementDecl(namespace = "", name = "requirement")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createRequirement(String value) {
        return new JAXBElement<String>(_Requirement_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "title")
    public JAXBElement<String> createTitle(String value) {
        return new JAXBElement<String>(_Title_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "error")
    public JAXBElement<BigInteger> createError(BigInteger value) {
        return new JAXBElement<BigInteger>(_Error_QNAME, BigInteger.class, null, value);
    }

}
