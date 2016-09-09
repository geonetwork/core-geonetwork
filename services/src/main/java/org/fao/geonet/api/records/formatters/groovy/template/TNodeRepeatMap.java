/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters.groovy.template;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import org.fao.geonet.SystemInfo;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.Map;

/**
 * @author Jesse on 11/29/2014.
 */
public class TNodeRepeatMap extends TNode {
    private final String key, rowKeyName, rowValueName;
    private final boolean onlyChildren;

    public TNodeRepeatMap(SystemInfo info, TextContentParser parser, boolean onlyChildren, String qName, Attributes attributes, String key, String rowKeyName, String rowValueName)
        throws IOException {
        super(info, parser, qName, attributes);
        this.key = key;
        this.rowKeyName = rowKeyName;
        this.rowValueName = rowValueName;
        this.onlyChildren = onlyChildren;
    }

    @Override
    public void render(TRenderContext context) throws IOException {
        final Object modelValue = context.getModelValue(this.key);

        if (!(modelValue instanceof Map)) {
            if (modelValue == null) {
                String options = Joiner.on(", ").join(context.getAllModelKeys());
                throw new TemplateException("There is no model item with the key: " + this.key + ".  Options include: " + options);
            } else {
                throw new TemplateException(
                    "Expected a map for (" + rowKeyName + ", " + rowValueName + ") in " + this.key + " but got a " + modelValue);
            }

        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map) modelValue;

        if (map.isEmpty() && this.info.isDevMode()) {
            context.append("<!-- fmt-repeat: (").append(rowKeyName).append(", ").append(rowValueName).append(") in ").append(this.key)
                .append(" is empty -->");
        }

        int i = 0;
        int size = map.size();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Map<String, Object> newModelMap = Maps.newHashMap();
            newModelMap.put(this.rowKeyName, entry.getKey());
            newModelMap.put(this.rowValueName, entry.getValue());
            TNodeRepeatIter.addIndexInfo(newModelMap, i, size);

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
