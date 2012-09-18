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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;


public class Document extends AbstractTreeNode {
    public Document() {
        this("1.0", null);
    }

    public Document(String version, String encoding) {
        super(true, true);

        if(encoding != null) {
            this.setAttribute("encoding", encoding);
        }

        if(version != null) {
            this.setAttribute("version", version);
        }
    }

    public String getVersion() {
        return (String) attributes.get("version");
    }

    public String getEncoding() {
        return (String) attributes.get("encoding");
    }

    public void setEncoding(String encoding) {
        attributes.put("encoding", encoding);
    }

    public String getStandalone() {
        return (String) attributes.get("standalone");
    }

    public void setStandalone(String standalone) {
        attributes.put("standalone", standalone);
    }

    public void setVersion(String version) {
        attributes.put("version", version);
    }

    public void save(String fileName) throws IOException {
        this.save(fileName, false);
    }

    public void save(String fileName, boolean split) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        String charset = getEncoding();
        OutputStreamWriter writer;

        if(charset != null) {
            writer = new OutputStreamWriter(fos, charset);
        }
        else {
            writer = new OutputStreamWriter(fos, "UTF-8");
        }

        exportXML(writer, split);
        writer.flush();
        writer.close();
        fos.close();
    }

    public void exportXML(Writer writer, boolean split)
            throws IOException {
        // write header
        writer.write("<?xml version=\"");
        writer.write(getVersion());
        writer.write("\"");

        if(getEncoding() != null) {
            writer.write(" encoding=\"");
            writer.write(getEncoding());
            writer.write("\"");
        }

        if(getStandalone() != null) {
            writer.write(" standalone=\"");
            writer.write(getStandalone());
            writer.write("\"");
        }

        writer.write("?>");

        // write children
        for(Iterator i = children.iterator(); i.hasNext(); ) {
            TreeNode node = (TreeNode) i.next();
            node.exportXML(writer, split);
        }

        writer.flush();
    }

    public Hash32 getHash32() {
        String s = getVersion() + "|" + getEncoding() + "|" + getStandalone();

        return new Hash32(s);
    }
}
