package org.fao.geonet.domain.responses;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

/**
 * Class to model CSW Virtual service parameter used for the CSW virtual get service response
 * (@see org.fao.geonet.domain.responses.CswVirtualServiceResponse).
 *
 * @author Jose García
 */
@XmlAccessorType(XmlAccessType.FIELD)
public  class ServiceParameter implements Serializable {
    private static final long serialVersionUID = -7682021379005431348L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlAttribute
    protected String name;

    @XmlValue
    protected String value;

    public ServiceParameter() {

    }

    public ServiceParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }
}