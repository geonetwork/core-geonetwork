package org.fao.geonet.kernel.schema;

import jeeves.resources.dbms.Dbms;
import org.jdom.Element;
import org.jdom.Namespace;

import javax.persistence.*;
import java.util.List;

/**
 * An entity representing a schematron criteria. This is for the extended
 * validation framework.
 * 
 * @author delawen
 */
public class SchematronCriteria {
	private SchematronCriteriaType type;
	private String value;

	/**
	 * @return the type
	 */
	@Column(nullable = false, name = "type")
	public SchematronCriteriaType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(SchematronCriteriaType type) {
		this.type = type;
	}

	/**
	 * @return the value
	 */
	@Column(nullable = false, name = "value")
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

    public boolean accepts(Dbms dbms, int metadataId, Element metadata, List<Namespace> metadataNamespaces) {
        return getType().accepts(dbms, getValue(), metadataId, metadata, metadataNamespaces);
    }
}
