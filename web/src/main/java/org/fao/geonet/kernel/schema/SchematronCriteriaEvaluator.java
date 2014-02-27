package org.fao.geonet.kernel.schema;

import jeeves.resources.dbms.Dbms;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.List;

/**
 * A strategy object that evaluates schematron criteria.
 *
 * Created by Jesse on 2/6/14.
 */
public interface SchematronCriteriaEvaluator {
    public abstract boolean accepts(Dbms dbms, String value, int metadataId, Element metadata,
                                    List<Namespace> metadataNamespaces);
}
