/**
 * JXyDiff: An XML Diff Written in Java
 *
 * Contact: pascal.molli@loria.fr
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of QPL/CeCill
 *
 * See licences details in QPL.txt and CeCill.txt
 *
 * Initial developer: Raphael Tani
 * Initial Developer: Gregory Cobena
 * Initial Developer: Gerald Oster
 * Initial Developer: Pascal Molli
 * Initial Developer: Serge Abiteboul
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package fr.loria.ecoo.so6.xml.node;

import fr.loria.ecoo.so6.xml.xydiff.Hash32;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;


public class ElementNode extends AbstractTreeNode {
    private String elementName;

    public ElementNode(String elementName) {
        super(true, true);
        this.elementName = elementName;
    }

    public void setElementName(String name) {
        this.elementName = name;
    }

    public String getElementName() {
        return this.elementName;
    }

    public void exportXML(Writer writer, boolean split)
            throws IOException {
        // write elements
        writer.write("<");
        writer.write(elementName);

        // write attributes
        for(Enumeration e = attributes.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            writer.write(" ");
            writer.write(key);

            String attrValue = (String) attributes.get(key);

            if(attrValue.indexOf("\"") == - 1) {
                writer.write("=\"");
                writer.write(attrValue);
                writer.write("\"");
            }
            else {
                writer.write("='");
                writer.write(attrValue);
                writer.write("'");
            }
        }

        if(children.isEmpty()) {
            // close element
            writer.write("/>");
        }
        else {
            writer.write(">");

            // write children
            for(Iterator i = children.iterator(); i.hasNext(); ) {
                TreeNode node = (TreeNode) i.next();
                node.exportXML(writer, split);
            }

            // close elements
            writer.write("</");
            writer.write(elementName);
            writer.write(">");
        }

        writer.flush();
    }

    public boolean equalsContent(Object obj) {
        if(obj instanceof ElementNode) {
            ElementNode elementObj = (ElementNode) obj;

            // check element name
            if(! elementObj.elementName.equals(elementName)) {
                return false;
            }

            // check attributes
            if(elementObj.attributes.size() != attributes.size()) {
                return false;
            }

            for(Enumeration e = attributes.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();

                if(! ((String) elementObj.attributes.get(key)).equals((String) attributes.get(key))) {
                    return false;
                }
            }

            // check children
            return super.equalsContent(obj);
        }
        else {
            return false;
        }
    }

    public Hash32 getHash32() {
        String s = getElementName();

        return new Hash32(s);
    }
}
