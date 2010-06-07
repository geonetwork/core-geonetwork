package org.fao.geonet.services.util.z3950;

import java.util.Set;


public class SRUParamTestDBO {
	
	private String op;
	private Set<String> notSupported;
	private Set<String> missingArgs;
	private Set<String> cannotParse ;
	
	
	public SRUParamTestDBO(String op, Set<String> notSupported, Set<String> missingArgs, Set<String> cannotParse ) {
		
		this.op=op;
		this.notSupported=notSupported;
		this.missingArgs=missingArgs;
		this.cannotParse=cannotParse;
		
	}
	
	public String getOp() {
		return op;
	}
	
	public Set<String> getArgNotSupported() {
		return notSupported;
	}
	
	public Set<String> getMissingArgs() {
		return missingArgs;
	}
	
	public Set<String> getCannotParseArg() {
		return cannotParse;
	}

}
