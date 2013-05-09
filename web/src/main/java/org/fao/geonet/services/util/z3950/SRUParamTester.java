package org.fao.geonet.services.util.z3950;

import org.fao.geonet.services.main.SRUSearch;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class SRUParamTester {

	private Set<String> explainArgs ;
	private Set<String> explainMandatoryArgs ;
	private Set<String> searchRetrieveArgs ;
	private Set<String> searchRetrieveMandatoryArgs ;

	private Hashtable<String,String> paramTypes;


	public SRUParamTester() {


		explainMandatoryArgs = new HashSet<String>();

		explainMandatoryArgs.add("version");
		explainMandatoryArgs.add("operation");

		explainArgs = new HashSet<String>();

		explainArgs.add("recordpacking");
		explainArgs.add("stylesheet");
		explainArgs.add("extrarequestdata");

		searchRetrieveMandatoryArgs = new HashSet<String>();

		searchRetrieveMandatoryArgs.add("operation");
		searchRetrieveMandatoryArgs.add("version");
		searchRetrieveMandatoryArgs.add("query");

		searchRetrieveArgs = new HashSet<String>();

		searchRetrieveArgs.add("startrecord");
		searchRetrieveArgs.add("maximumrecords");
		searchRetrieveArgs.add("recordpacking");
		searchRetrieveArgs.add("recordschema");
		searchRetrieveArgs.add("recordxpath ");
		searchRetrieveArgs.add("resultsetttl");
		searchRetrieveArgs.add("sortkeys");
		searchRetrieveArgs.add("stylesheet");
		searchRetrieveArgs.add("extrarequestdata");



		paramTypes = new Hashtable<String, String>();

		paramTypes.put(SRUSearch.OP_SR_QUERY, "string");
		paramTypes.put(SRUSearch.OP_SR_VERSION, "string");
		paramTypes.put(SRUSearch.OP_SR_STYLESH, "string");
		paramTypes.put(SRUSearch.OP_SR_STARTREC, "int");
		paramTypes.put(SRUSearch.OP_SR_MAXREC, "int");
		paramTypes.put(SRUSearch.OP_SR_RECPACK, "string");
		paramTypes.put(SRUSearch.OP_SR_RECSCHEMA, "string");
		paramTypes.put(SRUSearch.OP_SR_RECXPATH, "string");
		paramTypes.put(SRUSearch.OP_SR_SORTKEYS, "string");
		paramTypes.put(SRUSearch.OP_SR_EXTRADATA, "string");



	}

	public SRUParamTestDBO testParams(String op, Hashtable<String,String> params) {

		Set<String> notSupported = testNotSupported(op, params);
		Set<String> missingArgs = testMissingArgs(op, params);
		Set<String> cannotParse = testCannotParse(op, params);

		SRUParamTestDBO  ret = new SRUParamTestDBO(op, notSupported, missingArgs, cannotParse);


		return ret;
	}


	private Set<String> testNotSupported(String op, Hashtable<String,String> params) {

		HashSet<String> temp  = new HashSet<String>() ;
		HashSet<String> ret = new HashSet<String>();

		if (op.equals("explain")) {
			temp.addAll(explainArgs);
			temp.addAll(explainMandatoryArgs);
		}
		else if (op.equals("searchretrieve")) {
			temp.addAll(searchRetrieveArgs);
			temp.addAll(searchRetrieveMandatoryArgs);
		}
		else {
			return ret;
		}

		for (String param : params.keySet()) {
			if (!temp.contains(param)) ret.add(param);
		}

		return ret;
	}

	private Set<String> testMissingArgs(String op, Hashtable<String,String> params) {

		HashSet<String> temp  = new HashSet<String>() ;
		HashSet<String> ret = new HashSet<String>();

		if (op.equals("explain")) {
			temp.addAll(explainMandatoryArgs);
		}
		if (op.equals("searchretrieve")) {
			temp.addAll(searchRetrieveMandatoryArgs);
		}

		for (String param :  temp ) {
			if (!params.containsKey(param)) ret.add(param);
		}

		return ret;
	}

	private Set<String> testCannotParse(String op, Hashtable<String,String> params) {

		HashSet<String> ret = new HashSet<String>();

		if (op.equals("searchretrieve")) {

			for (Map.Entry<String, String> entry : params.entrySet()) {
			    String key = entry.getKey();
				String val = entry.getValue();

				if (paramTypes.containsKey(key) && paramTypes.get(key).equals("int") ) {
					try {
						Integer.parseInt(val);
					} 
					catch (NumberFormatException e) {
						ret.add(key);
					}
				}
			}

		}

		return ret;
		
	}

}