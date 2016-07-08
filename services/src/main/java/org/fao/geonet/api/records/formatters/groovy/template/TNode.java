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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.apache.xalan.xsltc.runtime.AttributeList;
import org.fao.geonet.SystemInfo;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.List;

/**
 * A "Template Node". A node in the template render tree.  A TNode defines how a section of a
 * template is rendered.  For example a node might be an nonEmpty node where the node will only be
 * rendered if the attribute is nonEmpty (non-empty/non-null string or collection).
 *
 * @author Jesse on 11/29/2014.
 */
public abstract class TNode {
    protected static final Attributes EMPTY_ATTRIBUTES = new AttributeList();
    protected final TextBlock attributes, end;
    protected final String qName;
    protected final SystemInfo info;
    protected final TextContentParser textContentParser;
    private List<TNode> children = Lists.newArrayList();
    private long unparsedSize;

    public TNode(SystemInfo info, TextContentParser textContentParser, String qName, Attributes attributes) throws IOException {
        this.info = info;
        this.qName = qName;
        StringBuilder start = new StringBuilder();
        StringBuilder end = new StringBuilder();
        renderAttributes(attributes, start);

        end.append("</").append(qName).append(">");

        this.textContentParser = textContentParser;
        this.attributes = textContentParser.parse(start.toString());
        this.end = textContentParser.parse(end.toString());
    }

    public List<TNode> getChildren() {
        return children;
    }

    public void renderAttributes(Attributes attributes, Appendable appendable) throws IOException {
        for (int i = 0; i < attributes.getLength(); i++) {
            appendable.append(" ").append(attributes.getQName(i)).append("=\"").append(attributes.getValue(i)).append("\"");
        }
    }

    /**
     * Render the currentNode.
     */
    public void render(TRenderContext context) throws IOException {
        final Optional<String> reasonToNotRender = canRender(context);
        if (reasonToNotRender.isPresent()) {
            addCannontRenderComment(context, reasonToNotRender);
        } else {
            context.append("<").append(qName);

            if (writeAttributes(context)) {
                attributes.render(context);
            }
            final Attributes customAttributes = customAttributes(context);
            if (customAttributes != null) {
                renderAttributes(customAttributes, context);
            }
            context.append(">");

            if (writeChildren(context)) {
                for (TNode child : children) {
                    appendCustomChildData(context, child);
                    child.render(context);
                }
            }

            end.render(context);
        }
    }

    public void addCannontRenderComment(TRenderContext context, Optional<String> reasonToNotRender) throws IOException {
        if (SystemInfo.getInfo(this.info).isDevMode()) {
            context.append("<!-- ").append(reasonToNotRender.get()).append(" -->");
        }
    }

    /**
     * If true and canRender then write all the children.
     */
    protected boolean writeChildren(TRenderContext context) {
        return true;
    }

    /**
     * If true and canRender then the attributes defined on the element will be on the element
     * definition. Otherwise no attributes will be written.
     */
    protected boolean writeAttributes(TRenderContext context) {
        return true;
    }

    /**
     * Write the extra child data for this node.
     * <p/>
     * The child nodes will be processed by the render method.
     */
    protected void appendCustomChildData(TRenderContext context, TNode nextChild) throws IOException {
        // no op
    }

    /**
     * Get attributes from implementation class.
     */
    protected Attributes customAttributes(TRenderContext context) {
        return null;
    }

    /**
     * Check if this node (and subtree) should be rendered.
     */
    protected abstract Optional<String> canRender(TRenderContext context);

    /**
     * Add a child to this node.
     */
    public void addChild(TNode node) {
        this.children.add(node);
    }

    public void setTextContent(String text) throws IOException {
        if (text.isEmpty()) {
            return;
        }
        addChild(new TNodeTextContent(info, textContentParser, textContentParser.parse(text)));
    }

    public long getUnparsedSize() {
        return unparsedSize;
    }

    public void setUnparsedSize(long unparsedSize) {
        this.unparsedSize = unparsedSize;
    }
}
