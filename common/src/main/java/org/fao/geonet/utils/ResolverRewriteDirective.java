package org.fao.geonet.utils;

/**
 * Represents a strategy for rewriting the href of a XSLT or XML import.
 *
 * @author Jesse on 11/28/2014.
 */
public interface ResolverRewriteDirective {
    /**
     * Test if this should be applied to the href.
     */
    boolean appliesTo(String href);
    String rewrite(String href);
}
