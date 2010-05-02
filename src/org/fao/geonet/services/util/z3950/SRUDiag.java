package org.fao.geonet.services.util.z3950;

public class SRUDiag {

	private String url;
	private String details;
	private String message;
	
	
	public SRUDiag(String url,String message,String details)
	{
		this.url=url;
		this.details=details;
		this.message=message;
	}
	
	public String getUrl() {
		return url;
	}
	public String getDetails() {
		return details;
	}
	public String getMessage() {
		return message;
	}
	

	
	

}
