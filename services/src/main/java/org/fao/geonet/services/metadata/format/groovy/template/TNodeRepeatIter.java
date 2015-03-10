package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.SystemInfo;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Jesse on 11/29/2014.
 */
public class TNodeRepeatIter extends TNode {
    private static final String INDEX_KEY = "$index";
    private static final String FIRST_KEY = "$first";
    private static final String LAST_KEY = "$last";
    private static final String ODD_KEY = "$odd";
    private static final String EVEN_KEY = "$even";
    private static final String MIDDLE_KEY = "$middle";
    private final String key;
    private final String rowContextKey;
    private final boolean onlyChildren;

    public TNodeRepeatIter(SystemInfo info, TextContentParser parser, boolean onlyChildren, String qName, Attributes attributes, String key, String rowContextKey) throws IOException {
        super(info, parser, qName, attributes);
        this.key = key;
        this.rowContextKey = rowContextKey;
        this.onlyChildren = onlyChildren;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(TRenderContext context) throws IOException {
        final Object modelValue = context.getModelValue(this.key);

        if (modelValue == null) {
            String options = Joiner.on(", ").join(context.getAllModelKeys());
            throw new TemplateException("There is no model item with the key: " + this.key + ".  Options include: " + options);
        }

        Iterable<Object> iter;
        int size;
        if (modelValue instanceof Collection) {
            final Collection collection = (Collection) modelValue;
            iter = collection;
            size = collection.size();
        } else if (modelValue instanceof GPathResult) {
            GPathResult value = (GPathResult) modelValue;
            iter = value;
            size = value.size();
        } else if (modelValue instanceof Map) {
            Map map = (Map) modelValue;
            iter = map.entrySet();
            size = map.size();
        } else if (modelValue.getClass().isArray()) {
            Object[] value = (Object[]) modelValue;
            iter = Arrays.asList(value);
            size = value.length;
        } else {
            iter = Collections.singletonList(modelValue);
            size = 1;
        }

        int i = 0;
        for (Object row : iter) {
            Map<String, Object> newModelMap = Maps.newHashMap();
            newModelMap.put(this.rowContextKey, row);
            addIndexInfo(newModelMap, i, size);

            TRenderContext childContext = context.childContext(newModelMap);
            if (!this.onlyChildren) {
                context.append("<").append(qName);
                attributes.render(childContext);
                context.append(">");
            }

            for (TNode node : getChildren()) {
                node.render(childContext);
            }

            if (!this.onlyChildren) {
                end.render(childContext);
            }

            i++;
        }

        if (i == 0 && this.info.isDevMode()) {
            context.append("<!-- fmt-repeat: ").append(rowContextKey).append(" in ").append(this.key).append(" is empty -->");
        }
    }

    static void addIndexInfo(Map<String, Object> newModelMap, int index, int total) {
        newModelMap.put(INDEX_KEY, index);
        newModelMap.put(FIRST_KEY, index == 0);
        newModelMap.put(LAST_KEY, (total - 1) == index);
        newModelMap.put(ODD_KEY, Math.abs(index % 2) == 1);
        newModelMap.put(EVEN_KEY, Math.abs(total % 2) == 0);
        newModelMap.put(MIDDLE_KEY, (total / 2) == index);
    }

    @Override
    protected Attributes customAttributes(TRenderContext context) {
        return null;
    }

    @Override
    protected Optional<String> canRender(TRenderContext context) {
        return Optional.absent();
    }
}
