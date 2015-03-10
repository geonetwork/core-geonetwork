package org.fao.geonet.domain.responses;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "response")
public class IdResponse {

	public IdResponse() {
	}
	public IdResponse(String id) {
		this.id = id;
	}

    private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
