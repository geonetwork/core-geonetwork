package org.fao.geonet.services.util.z3950;

public class SRUArgumentParseException extends Exception {
    private static final long serialVersionUID = 1L;
    private String type;
	private String val;
	
	public SRUArgumentParseException(String type, String val,Exception e) 
	{
		super(e);
		this.val=val;
		this.type=type;
	}

	public String getType() {
		return type;
	}

	public String getVal() {
		return val;
	}
}
