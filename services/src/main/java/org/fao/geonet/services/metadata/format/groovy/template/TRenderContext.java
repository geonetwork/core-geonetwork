package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import groovy.util.slurpersupport.GPathResult;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.Constants;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A render context for rendering a Template Tree.
 *
 * @author Jesse on 11/29/2014.
 */
public class TRenderContext implements Appendable, Closeable {
    private final TRenderContext parent;
    private final Map<String, Object> model;
    private final OutputStream outputStream;
    private final Writer writer;

    private TRenderContext(TRenderContext parent, Map<String, Object> model, OutputStream outputStream, Writer writer) {
        this.parent = parent;
        this.model = model;
        this.outputStream = outputStream;
        this.writer = writer;
    }

    public TRenderContext(OutputStream outputStream, Map<String, Object> model) {
        this(outputStream, Constants.CHARSET, model);
    }

    public TRenderContext(OutputStream outputStream, Charset charset, Map<String, Object> model) {
        this.parent = null;
        this.outputStream = outputStream;
        this.model = model;
        this.writer = new OutputStreamWriter(outputStream, charset);
    }

    @Override
    public TRenderContext append(CharSequence csq) throws IOException {
        writer.append(csq);
        writer.flush();
        return this;
    }

    @Override
    public TRenderContext append(CharSequence csq, int start, int end) throws IOException {
        writer.append(csq, start, end);
        writer.flush();
        return this;
    }

    @Override
    public TRenderContext append(char c) throws IOException {
        writer.append(c);
        writer.flush();
        return this;
    }

    public Set<String> getAllModelKeys() {
        Set<String> keys = Sets.newHashSet();
        if (this.parent != null) {
            keys.addAll(this.parent.getAllModelKeys());
        }
        keys.addAll(this.model.keySet());
        return keys;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(this.writer);
        IOUtils.closeQuietly(this.outputStream);
    }

    public Object getModelValue(String expr) {
        final int indexOfDot = expr.indexOf('.');
        if (indexOfDot > -1) {
            String key = expr.substring(0, indexOfDot);
            final String property = expr.substring(indexOfDot + 1);
            return getModelValue(key, property);
        }

        return getModelValue(expr, null);
    }

    private Object getModelValue(String expr, String property) {
        Object value = this.model.get(expr.trim());
        if (value == null && parent != null) {
            value = parent.getModelValue(expr);
        }
        if (property == null) {
            return value;
        }

        try {
            if (value == null) {
                throw new TemplateException(
                        "There is no object in the model map with the id '" + expr + "' in the model map.  The model selection " +
                        "expression is: " + expr.trim() + "." + property.trim() + "'.\nThe current options are: \n" + getAllModelKeys());
            }
            return getProperty(value, property);
        } catch (NullPropertyException e) {
            throw new TemplateException("The value of the property '" + e + "' is null. The full selection expression is: " +
                                        "'" + expr.trim() + "." + property.trim() + "'");
        } catch (EmptyPropertyException e) {
            throw new TemplateException("Model expression: '" + expr.trim() + "." + property.trim() + "' contains an empty section.");
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new TemplateException(
                    "Error accessing the properties in the property path of: '" +
                    expr.trim() + "." + property + "'.  " + e, e);
        } catch (NoSuchPropertyException e) {
            throw new TemplateException(
                    "One of the properties in the property path: '" + expr.trim() + "." + property + "' does not exist on " +
                    "the object selected at that point. The property missing is: " + e);
        }
    }

    private Object getProperty(@Nonnull Object baseValue, String property) throws InvocationTargetException, IllegalAccessException {
        Object value = baseValue;
        int indexOfDot = property.indexOf('.');
        while (indexOfDot > -1) {
            String prop = property.substring(0, indexOfDot);
            property = property.substring(indexOfDot + 1);

            value = safeGetProperty(value, prop);

            indexOfDot = property.indexOf('.');


            if (value == null) {
                throw new NullPropertyException(prop);
            }
        }

        return safeGetProperty(value, property);
    }

    private Object safeGetProperty(@Nonnull Object value, String prop) throws InvocationTargetException, IllegalAccessException {
        prop = prop.trim();
        if (prop.trim().isEmpty()) {
            throw new EmptyPropertyException();
        }
        if (value instanceof GPathResult) {
            GPathResult result = (GPathResult) value;
            if (prop.replace("\\s+", "").equals("name()")) {
                return result.name();
            }
            return result.getProperty(prop);
        }
        if (value instanceof Map) {
            Map map = (Map) value;
            return map.get(prop);
        } else if (value instanceof List) {
            List list = (List) value;
            try {
                return list.get(Integer.parseInt(prop));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "When accessing a list the property must be a number.  Property:" + prop + ".  List: " + list);
            }
        } else {
            final PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(value.getClass(), prop.trim());
            if (propertyDescriptor == null) {
                try {
                    final Field declaredField = value.getClass().getDeclaredField(prop.trim());
                    declaredField.setAccessible(true);
                    return declaredField.get(value);
                } catch (NoSuchFieldException e) {
                    // skip
                }
                throw new NoSuchPropertyException(prop + " on object: " + value + " (" + value.getClass() + ")");
            }
            Method method = propertyDescriptor.getReadMethod();
            method.setAccessible(true);
            value = method.invoke(value);
            return value;
        }
    }

    public TRenderContext childContext(Map<String, Object> newModel) {
        return new TRenderContext(this, newModel, outputStream, writer);
    }

    public Map<String, Object> getModel(boolean mergeParentModels) {
        Map<String, Object> fullModel;
        if(mergeParentModels && this.parent != null) {
            fullModel = this.parent.getModel(true);
            fullModel.putAll(this.model);
        } else {
            fullModel = Maps.newHashMap(this.model);
        }

        return fullModel;
    }

    private static class EmptyPropertyException extends RuntimeException {
    }

    private static class NoSuchPropertyException extends RuntimeException {
        private NoSuchPropertyException(String message) {
            super(message);
        }
    }

    private static class NullPropertyException extends RuntimeException {
        private final String prop;

        private NullPropertyException() {
            prop = "";
        }

        private NullPropertyException(String prop) {
            this.prop = prop;
        }

        @Override
        public String toString() {
            return prop;
        }
    }
}
