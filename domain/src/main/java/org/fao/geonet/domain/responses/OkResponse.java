package org.fao.geonet.domain.responses;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

@XmlRootElement(name = "response")
public class OkResponse implements Serializable {
    private static final long serialVersionUID = 4870077749478620950L;

    @XmlValue
    private String value = "ok";

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
