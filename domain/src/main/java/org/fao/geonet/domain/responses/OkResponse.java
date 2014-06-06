package org.fao.geonet.domain.responses;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "response")
public class OkResponse {

    @XmlValue
    private String value = "ok";

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
