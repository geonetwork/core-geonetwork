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

import java.io.IOException;
import java.io.Writer;


public class DocTypeNode extends TextNode {
    public DocTypeNode() {
        super();
    }

    public DocTypeNode(String content) {
        super(content);
    }

    public void exportXML(Writer writer, boolean split)
            throws IOException {
        writer.write("<!DOCTYPE");
        writer.write(content.toString());
        writer.write(">");
        writer.flush();
    }
}
