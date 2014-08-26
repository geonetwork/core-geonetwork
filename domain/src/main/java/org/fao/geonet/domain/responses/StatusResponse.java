package org.fao.geonet.domain.responses;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "response")
public class StatusResponse implements Serializable {
    private static final long serialVersionUID = 4870087654478620950L;

    public StatusResponse() {
	}
    
    public StatusResponse(String status) {
    	setStatus(status);
	}
    
    private String status = "unknown";

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


}
