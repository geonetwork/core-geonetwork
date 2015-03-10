package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.fao.geonet.SystemInfo;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * An "if" node that renders if the model has a truthy value for the given key.
 * <p/>
 * In this case truthy means, non-null, non-empty (if string, collection, array or map), a number != 0 or true value.
 * <p/>
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
 * @author Jesse on 11/29/2014.
 */
public class TNodeIf extends TNode {
    public static final double PRECISION = 0.000000001;
    private final String expr;
    private final boolean not;
    private final Set<String> scriptVariables;
    private final boolean onlyChildren;

    public TNodeIf(SystemInfo info, TextContentParser parser, String qName, Attributes attributes, String expr, boolean onlyChildren) throws IOException {
        super(info, parser, qName, attributes);
        this.onlyChildren = onlyChildren;
        final Matcher matcher = TextContentParser.INTERPOLATION_PATTERN.matcher(expr);
        if (matcher.find()) {
            this.scriptVariables = Sets.newHashSet();
            int start = 0;
            StringBuilder builder = new StringBuilder();
            do {
                String key = matcher.group(1).trim();
                this.scriptVariables.add(key);
                builder.append(expr.substring(start, matcher.start()));
                builder.append(key);

                start = matcher.end();
            } while (matcher.find());

            builder.append(expr.substring(start, expr.length()));

            this.expr = builder.toString();
            this.not = false;
        } else {
            this.scriptVariables = null;
            if (expr.startsWith("!")) {
                this.not = true;
                this.expr = expr.substring(1);
            } else {
                this.not = false;
                this.expr = expr;
            }
        }
    }

    @Override
    protected Optional<String> canRender(TRenderContext context) {
        if (this.scriptVariables == null) {
            final Object val = context.getModelValue(this.expr);
            final String truthy = isTruthy(val);
            if (not) {
                return truthy != null ? Optional.<String>absent() :
                        Optional.of("fmt-if=!" + this.expr + " is false (" + this.expr + " is true)");
            } else {
                if (truthy != null) {
                    return Optional.of("fmt-if=" + this.expr + " is " + truthy);
                }
                return Optional.absent();
            }
        } else {
            Binding binding = new Binding();
            for (String scriptVariable : this.scriptVariables) {
                binding.setVariable(scriptVariable, context.getModelValue(scriptVariable));
            }
            GroovyShell shell = new GroovyShell(binding);

            Object value = shell.evaluate(this.expr);

            if (value instanceof Boolean && (Boolean) value) {
                return Optional.absent();
            } else {
                return Optional.of(this.expr + " resulted in a non-true value: '" + value + "'");
            }
        }
    }

    @Override
    public void render(TRenderContext context) throws IOException {
        final Optional<String> canRenderOptional = canRender(context);
        if (!canRenderOptional.isPresent()) {
            if (onlyChildren) {
                for (TNode child : getChildren()) {
                    child.render(context);
                }
            } else {
                super.render(context);
            }
        } else {
            addCannontRenderComment(context, canRenderOptional);
        }
    }

    @VisibleForTesting
    static String isTruthy(Object val) {
        if (val == null) {
            return "null";
        }
        if (val instanceof String) {
            String sVal = (String) val;
            return sVal.isEmpty() ? "empty" : null;
        } else if (val instanceof Iterable) {
            Iterable itVal = (Iterable) val;
            return itVal.iterator().hasNext() ? null : "empty";
        } else if (val instanceof Enumeration) {
            Enumeration itVal = (Enumeration) val;
            return itVal.hasMoreElements() ? null : "empty";
        } else if (val instanceof Iterator) {
            Iterator itVal = (Iterator) val;
            return itVal.hasNext() ? null : "empty";
        } else if (val instanceof Map) {
            Map mapVal = (Map) val;
            return mapVal.isEmpty() ? "empty" : null;
        } else if (val instanceof Boolean) {
            return (Boolean) val ? null : "false";
        } else if (val instanceof Double) {
            return Math.abs((Double) val) > PRECISION ? null : "0";
        } else if (val instanceof Float) {
            return Math.abs((Float) val) > PRECISION ? null : "0";
        } else if (val instanceof Number) {
            return ((Number) val).intValue() != 0 ? null : "0";
        } else if (val instanceof Character) {
            return null;
        } else if (val.getClass().isArray()) {
            return ((Object[]) val).length > 0 ? null : "empty";
        } else {
            throw new AssertionError("Not a recognized type: " + val.getClass() + ": " + val);
        }
    }

}
