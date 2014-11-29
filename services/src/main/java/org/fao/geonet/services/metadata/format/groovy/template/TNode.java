package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import org.apache.xalan.xsltc.runtime.AttributeList;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.List;

/**
 * A "Template Node". A node in the template render tree.  A TNode defines how a section of a template is rendered.  For example
 * a node might be an nonEmpty node where the node will only be rendered if the attribute is nonEmpty
 * (non-empty/non-null string or collection).
 *
 * @author Jesse on 11/29/2014.
 */
public abstract class TNode {
    private final String start, end;
    private List<TNode> children = Lists.newArrayList();
    protected static final Attributes EMPTY_ATTRIBUTES = new AttributeList();


    public TNode(String localName, String qName, Attributes attributes) throws IOException {
        StringBuilder start = new StringBuilder();
        StringBuilder end = new StringBuilder();
        start.append("<").append(qName);
        writeAttributes(attributes, start);

        end.append("\n</").append(qName).append(">");

        this.start = start.toString();
        this.end = end.toString();
    }

    public void writeAttributes(Attributes attributes, Appendable appendable) throws IOException {
        for (int i = 0; i < attributes.getLength(); i++) {
            appendable.append(" ").append(attributes.getQName(i)).append("=\"").append(attributes.getValue(i)).append("\"");
        }
    }

    /**
     * Render the currentNode.
     */
    public final void render(TRenderContext context) throws IOException {
        if (canRender(context)) {
            context.append(start);
            writeAttributes(customAttributes(context), context);
            context.append(">");
            for (TNode child : children) {
                child.render(context);
            }
            writeCustomChildData(context);
            context.append(end);
        }
    }

    /**
     * Write the extra child data for this node.
     * <p/>
     * The child nodes will be processed by the render method.
     */
    protected abstract void writeCustomChildData(TRenderContext context);

    /**
     * Get attributes from implementation class.
     */
    protected abstract Attributes customAttributes(TRenderContext context);

    /**
     * Check if this node (and subtree) should be rendered.
     */
    protected abstract boolean canRender(TRenderContext context);

    /**
     * Add a child to this node.
     */
    public void addChild(TNode node) {
        this.children.add(node);
    }
}
