package org.fao.geonet.api.records.model.validation;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigInteger;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}id"/>
 *         &lt;element ref="{}displayPriority"/>
 *         &lt;element ref="{}label"/>
 *         &lt;element ref="{}error"/>
 *         &lt;element ref="{}success"/>
 *         &lt;element ref="{}total"/>
 *         &lt;element ref="{}requirement"/>
 *         &lt;element ref="{}patterns"/>
 *         &lt;element ref="{}schematronVerificationError"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "id",
    "displayPriority",
    "label",
    "error",
    "success",
    "total",
    "requirement",
    "patterns",
    "schematronVerificationError"
})
@XmlRootElement(name = "report")
// Use different schema name for report to resolve conflict with org.fao.geonet.api.processing.report.Report
@Schema(name = "ValidationReport")
public class Report {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String id;
    @XmlElement(required = true)
    protected BigInteger displayPriority;
    @XmlElement(required = true)
    protected String label;
    @XmlElement(required = true)
    protected BigInteger error;
    @XmlElement(required = true)
    protected String success;
    @XmlElement(required = true)
    protected String total;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String requirement;
    @XmlElement(required = true)
    protected Patterns patterns;
    @XmlElement(required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String schematronVerificationError;

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the displayPriority property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getDisplayPriority() {
        return displayPriority;
    }

    /**
     * Sets the value of the displayPriority property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setDisplayPriority(BigInteger value) {
        this.displayPriority = value;
    }

    /**
     * Gets the value of the label property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the error property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setError(BigInteger value) {
        this.error = value;
    }

    /**
     * Gets the value of the success property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSuccess() {
        return success;
    }

    /**
     * Sets the value of the success property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSuccess(String value) {
        this.success = value;
    }

    /**
     * Gets the value of the total property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTotal(String value) {
        this.total = value;
    }

    /**
     * Gets the value of the requirement property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRequirement() {
        return requirement;
    }

    /**
     * Sets the value of the requirement property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRequirement(String value) {
        this.requirement = value;
    }

    /**
     * Gets the value of the patterns property.
     *
     * @return possible object is
     * {@link Patterns }
     */
    public Patterns getPatterns() {
        return patterns;
    }

    /**
     * Sets the value of the patterns property.
     *
     * @param value allowed object is
     *              {@link Patterns }
     */
    public void setPatterns(Patterns value) {
        this.patterns = value;
    }

    /**
     * Gets the value of schematronVerificationError. This is set when a failure occurred while a schematron run.
     *
     * @return the schematronVerificationError property.
     */
    public String getSchematronVerificationError() {
        return schematronVerificationError;
    }

    /**
     * Sets the value of schematronVerificationError property.
     *
     * @param schematronVerificationError the error message.
     */
    public void setSchematronVerificationError(String schematronVerificationError) {
        this.schematronVerificationError = schematronVerificationError;
    }

}
