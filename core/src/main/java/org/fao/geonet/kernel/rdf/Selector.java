package org.fao.geonet.kernel.rdf;

import java.util.Arrays;
import java.util.Collection;

import org.jdom.Namespace;

/**
 * Represents the columnName and Paths for reading a row and attributes from
 * a thesaurus.
 * 
 * @author jeichar
 */
public class Selector {
    /**
     * The columnName that will be created for the Path by the QueryBuilder
     */
	public final String id;
	/**
	 * The path for performing the selection
	 */
	private final String path;
	/**
	 * The namespaces needed by this path
	 */
	private final Namespace[] namespaces;

	public Selector(String id, String path, Namespace... namespaces) {
		this.id = id;
		this.path = path;
		this.namespaces = namespaces;
	}
	
	public Selector(Selector path2) {
	    this.id = path2.id;
	    this.path = path2.path;
	    this.namespaces = path2.namespaces;
    }

    public Collection<Namespace> getNamespaces() {
        return Arrays.asList(namespaces);
    }

	public String getVariable() {
	    return id;
	}
	
	public String getPath() {
	    return path;
	}

    public Selector where(final Where whereClause) {
        return new PathDecorator(this) {
            @Override
            public String getPath() {
                return super.getPath()+" WHERE "+whereClause.getClause();
            }
        };
    }
}