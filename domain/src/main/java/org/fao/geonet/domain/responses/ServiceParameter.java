package org.fao.geonet.domain.responses;

import org.fao.geonet.domain.ServiceParam;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

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

    public Character getOccur() {
        return occur;
    }

    public void setOccur(Character occur) {
        this.occur = occur;
    }

    @XmlAttribute
    protected String name;

    @XmlValue
    protected String value;

    @XmlValue
    protected Character occur;

    public ServiceParameter() {

    }

    public ServiceParameter(ServiceParam param) {
        this.name = param.getId().getName();
        this.value = param.getId().getValue();
        this.occur = param.getOccur();
    }
}