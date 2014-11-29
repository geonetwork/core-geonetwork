package org.fao.geonet.services.metadata.format.groovy.template;

import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * An "if" node that renders if the model has a non-null, non-empty value with the given key.
 * <p/>
 * Example:
 * <p/>
 * For the template:
 * <pre><code>
 *     &amp;div non-empty="key">data&amp;/div>
 * </code></pre>
 * the div will be rendered if there is a value "key" in the model that is a string or Iterable that is non-empty.
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class NonEmptyNode extends TNode {
    private final String expr;

    public NonEmptyNode(String localName, String qName, Attributes attributes, String expr) throws IOException {
        super(localName, qName, attributes);
        this.expr = expr;
    }

    @Override
    protected void writeCustomChildData(TRenderContext context) {
        // no implementation
    }

    @Override
    protected Attributes customAttributes(TRenderContext context) {
        return EMPTY_ATTRIBUTES;
    }

    @Override
    protected boolean canRender(TRenderContext context) {
        final Object val = context.getModelValue(this.expr);
        if (val == null) {
            return false;
        }
        if (val instanceof String) {
            String sVal = (String) val;
            return !sVal.isEmpty();
        } else if (val instanceof Iterable) {
            Iterable itVal = (Iterable) val;
            return itVal.iterator().hasNext();
        } else {
            throw new AssertionError("Not a recognized type: " + val.getClass() + ": " + val);
        }
    }
}
