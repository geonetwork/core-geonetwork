package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * Creates fmt-if nodes.
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TNodeFactoryIf extends TNodeFactoryByAttName {

    public static final String IF = "if";

    public TNodeFactoryIf() {
        super(IF);
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final String value = getValue(attributes, IF);
        final FilteredAttributes filteredAttributes = new FilteredAttributes(attributes, IF);
        return new TNodeIf(qName, filteredAttributes, value);
    }

    /**
     * An "if" node that renders if the model has a truthy value for the given key.
     * <p/>
     * In this case truthy means, non-null, non-empty (if string, collection, array or map), a number != 0 or true value.
     *
     * If the expression starts with a ! then check is not-ed.
     * <p/>
     * Example:
     * <p/>
     * For the template:
     * <pre><code>
     *     &amp;div fmt-if="key">data&amp;/div>
     * </code></pre>
     * the div will be rendered if there is a value "key" in the model that is a string or Iterable that is non-empty.
     *
     *
     * @author Jesse on 11/29/2014.
     */
    public static class TNodeIf extends TNode {
        public static final double PRECISION = 0.000000001;
        private final String expr;
        private final boolean not;

        public TNodeIf(String qName, Attributes attributes, String expr) throws IOException {
            super(qName, attributes);
            if (expr.startsWith("!")) {
                this.not = true;
                this.expr = expr.substring(1);
            } else {
                this.not = false;
                this.expr = expr;
            }

        }

        @Override
        protected boolean canRender(TRenderContext context) {
            final Object val = context.getModelValue(this.expr);
            final boolean truthy = isTruthy(val);
            return not ? !truthy : truthy;
        }

        @VisibleForTesting
        static boolean isTruthy(Object val) {
            if (val == null) {
                return false;
            }
            if (val instanceof String) {
                String sVal = (String) val;
                return !sVal.isEmpty();
            } else if (val instanceof Iterable) {
                Iterable itVal = (Iterable) val;
                return itVal.iterator().hasNext();
            } else if (val instanceof Enumeration) {
                Enumeration itVal = (Enumeration) val;
                return itVal.hasMoreElements();
            } else if (val instanceof Iterator) {
                Iterator itVal = (Iterator) val;
                return itVal.hasNext();
            } else if (val instanceof Map) {
                Map mapVal = (Map) val;
                return !mapVal.isEmpty();
            } else if (val instanceof Boolean) {
                return (Boolean) val;
            } else if (val instanceof Double) {
                return Math.abs((Double) val) > PRECISION;
            } else if (val instanceof Float) {
                return Math.abs((Float) val) > PRECISION;
            } else if (val instanceof Number) {
                return ((Number)val).intValue() != 0;
            } else if (val instanceof Character) {
                return true;
            } else if (val.getClass().isArray()) {
                return ((Object[])val).length > 0;
            } else {
                throw new AssertionError("Not a recognized type: " + val.getClass() + ": " + val);
            }
        }
    }
}
