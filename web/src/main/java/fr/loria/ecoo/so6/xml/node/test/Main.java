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
package fr.loria.ecoo.so6.xml.node.test;

import fr.loria.ecoo.so6.xml.exception.ParseException;
import fr.loria.ecoo.so6.xml.node.CDataNode;
import fr.loria.ecoo.so6.xml.node.CommentNode;
import fr.loria.ecoo.so6.xml.node.Document;
import fr.loria.ecoo.so6.xml.node.ElementNode;
import fr.loria.ecoo.so6.xml.node.TextNode;
import fr.loria.ecoo.so6.xml.node.TreeNode;
import fr.loria.ecoo.so6.xml.util.XmlUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;


public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        Document document = new Document();
        ElementNode element = new ElementNode("root");
        ElementNode a = new ElementNode("a");
        document.appendChild(new TextNode("\n"));
        document.appendChild(new CommentNode(" un petit commentaire "));
        document.appendChild(new TextNode("\n\n"));
        document.appendChild(element);
        document.appendChild(new TextNode("\n\n"));
        document.appendChild(new CommentNode(" un petit commentaire "));
        document.appendChild(new TextNode("\n"));

        //
        element.appendChild(new TextNode("\n "));
        element.appendChild(a);
        element.appendChild(new TextNode("\n "));
        element.appendChild(new ElementNode("b"));
        element.appendChild(new TextNode("\n "));
        element.appendChild(new ElementNode("c"));
        element.appendChild(new TextNode("\n "));
        element.appendChild(new ElementNode("d"));
        element.appendChild(new TextNode("\n"));

        //
        a.appendChild(new CommentNode(" un petit commentaire "));
        a.appendChild(new CDataNode("un cdata!!"));

        //
        element.setAttribute("xml:ns", "http://www.libresource.org");
        element.setAttribute("ola", "tutu");

        //
        document.exportXML(new PrintWriter(System.out), true);

        //
        StringWriter writer = new StringWriter();
        element.toBase64(writer);
        writer.close();

        //System.out.println(writer.toString());
        TreeNode importedNode = XmlUtil.importNode(writer.toString());
        //System.out.println("Equal: " + importedNode.equals(element));
        importedNode.exportXML(new PrintWriter(System.out), true);
    }
}
