
package org.fao.geonet.api.records.model.validation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{}report" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "report"
})
@XmlRootElement(name = "reports")
public class Reports {

    protected List<Report> report;

    /**
     * Gets the value of the report property.
     *
     * @return
     *     possible object is
     *     {@link Report }
     *
     */
    public List<Report> getReport() {
        if (report == null) {
            report = new ArrayList<Report>();
        }
        return report;
    }

    /**
     * Sets the value of the report property.
     *
     * @param value
     *     allowed object is
     *     {@link Report }
     *
     */
    public void setReport(List<Report> value) {
        this.report = value;
    }
}
