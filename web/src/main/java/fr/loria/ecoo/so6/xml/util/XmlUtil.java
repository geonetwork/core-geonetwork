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
package fr.loria.ecoo.so6.xml.util;

import fr.loria.ecoo.so6.antlr.XMLLexer;
import fr.loria.ecoo.so6.antlr.XMLParser;
import fr.loria.ecoo.so6.xml.exception.AttributeAlreadyExist;
import fr.loria.ecoo.so6.xml.exception.AttributeNotAllowed;
import fr.loria.ecoo.so6.xml.exception.InvalidNodePath;
import fr.loria.ecoo.so6.xml.exception.ParseException;
import fr.loria.ecoo.so6.xml.node.Document;
import fr.loria.ecoo.so6.xml.node.ElementNode;
import fr.loria.ecoo.so6.xml.node.ProcessingInstructionNode;
import fr.loria.ecoo.so6.xml.node.TextNode;
import fr.loria.ecoo.so6.xml.node.TreeNode;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * TODO javadoc.
 *
 */
public class XmlUtil {

    /**
     * Path manipulation.
     *
     * @param nodePath
     * @return
     */
    public static String getParentPath(String nodePath) {
        // add for report
        if(nodePath.lastIndexOf(":") == - 1) {
            return nodePath;
        }

        //
        return nodePath.substring(0, nodePath.lastIndexOf(":"));
    }

    /**
     *
     * @param nodePath
     * @return
     */
    public static int getChildPosition(String nodePath) {
        return Integer.parseInt(nodePath.substring(nodePath.lastIndexOf(":") + 1));
    }

    /**
     *
     * @param fileName
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static String getEncoding(String fileName) throws IOException, ParseException {
        FileReader fr = new FileReader(fileName);
        XMLLexer lexer = new XMLLexer(fr);
        XMLParser parser = new XMLParser(lexer);
        Document doc = new Document();

        // Change set parseProlog()
        try {
            parser.prolog(doc);
        }
        catch(Exception e) {
            // Default encoding UTF-8
            return "UTF-8";

            //throw new ParseException(e.getMessage());
        }
        finally {
            fr.close();
        }

        return doc.getEncoding();
    }

    /**
     * Gets encoding from InputStream. Added by Heikki Doeleman.
     *
     * @param inputStream
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static String getEncoding(InputStream inputStream) throws IOException, ParseException {
        XMLLexer lexer = new XMLLexer(inputStream);
        XMLParser parser = new XMLParser(lexer);
        Document doc = new Document();

        // Change set parseProlog()
        try {
            parser.prolog(doc);
        }
        catch(Exception e) {
            // Default encoding UTF-8
            return "UTF-8";

            //throw new ParseException(e.getMessage());
        }
        finally {
            inputStream.close();
        }

        return doc.getEncoding();
    }

    /**
     * Loads XML from a String. Added by heikki doeleman.
     *
     * @param xml
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static Document loadString(String xml) throws IOException, ParseException {
        try {
            XMLLexer lexer = new XMLLexer(new StringReader(xml));
            XMLParser parser = new XMLParser(lexer);
            Document d = parser.document();

            //System.out.println("*** XmlUtil loadString document: " +d.toString());

            return d;
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new ParseException(e.getMessage());
        }
    }

        /**
        * Loads Document (Call the parser) from file.
        *
        * @param fileName file name
        * @return Document
        * @throws IOException hmm
        * @throws ParseException hmm
        */
    public static Document load(String fileName) throws IOException, ParseException {
        //System.out.println("loading file " + fileName);
        FileInputStream fis = new FileInputStream(fileName);
        String charset = getEncoding(fileName);
        InputStreamReader inr;

        if(charset != null) {
            inr = new InputStreamReader(fis, charset);
        }
        else {
            inr = new InputStreamReader(fis, "UTF-8");
        }

        XMLLexer lexer = new XMLLexer(inr);
        XMLParser parser = new XMLParser(lexer);

        try {
            Document d = parser.document();

            //System.out.println(d.toString());

            return d;
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new ParseException(e.getMessage());
        }
        finally {
            fis.close();
        }
    }

    //
    // Import / Export : Nodes
    //

    /**
     *
     * @param base64Object
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static TreeNode importNode(String base64Object) throws IOException, ParseException {
        String s = new String(Base64.decode(base64Object), "UTF-8");

        //System.out.println("---\n" + s + "\n---");
        StringReader reader = new StringReader(s);
        XMLLexer lexer = new XMLLexer(reader);
        XMLParser parser = new XMLParser(lexer);

        try {
            return (TreeNode) parser.node();
        }
        catch(Exception e) {
            throw new ParseException(e.getMessage());
        }
    }

    /**
     *
     * @param node
     * @return
     * @throws Exception
     */
    public static TreeNode clone(TreeNode node) throws Exception {
        return clone(node, true);
    }

    /**
     *
     * @param node
     * @param withChildren
     * @return
     * @throws Exception
     */
    public static TreeNode clone(TreeNode node, boolean withChildren)
            throws Exception {
        StringWriter writer = new StringWriter();
        node.toBase64(writer);
        writer.close();

        TreeNode n = importNode(writer.toString());

        if(! withChildren) {
            n.getChildren().clear();
        }

        return n;
    }

    //
    // Operations
    //

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param nodeToInsert
     * @throws IOException
     * @throws InvalidNodePath
     * @throws ParseException
     */
    public static void insertNode(String xmlFileName, String nodePath, TreeNode nodeToInsert)
            throws IOException, InvalidNodePath, ParseException {
        Document doc = load(xmlFileName);
        TreeNode node = doc.getNode(getParentPath(nodePath));
        node.insertChild(getChildPosition(nodePath), nodeToInsert);
        doc.save(xmlFileName, true);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @throws IOException
     * @throws InvalidNodePath
     * @throws ParseException
     */
    public static void deleteNode(String xmlFileName, String nodePath)
            throws IOException, InvalidNodePath, ParseException {
        Document doc = load(xmlFileName);
        TreeNode node = doc.getNode(getParentPath(nodePath));
        node.removeChild(getChildPosition(nodePath));
        doc.save(xmlFileName, true);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param attributeName
     * @param attributeValue
     * @throws IOException
     * @throws InvalidNodePath
     * @throws ParseException
     * @throws AttributeNotAllowed
     */
    public static void setAttribute(String xmlFileName, String nodePath, String attributeName, String attributeValue)
            throws IOException, InvalidNodePath, ParseException, AttributeNotAllowed {
        try {
            setAttribute(xmlFileName, nodePath, attributeName, attributeValue, false);
        }
        catch(AttributeAlreadyExist e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param attributeName
     * @param attributeValue
     * @throws IOException
     * @throws AttributeAlreadyExist
     * @throws InvalidNodePath
     * @throws ParseException
     * @throws AttributeNotAllowed
     */
    public static void insertAttribute(String xmlFileName, String nodePath, String attributeName, String attributeValue)
            throws IOException, AttributeAlreadyExist, InvalidNodePath, ParseException, AttributeNotAllowed {
        setAttribute(xmlFileName, nodePath, attributeName, attributeValue, true);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param attributeName
     * @param attributeValue
     * @throws IOException
     * @throws AttributeAlreadyExist
     * @throws InvalidNodePath
     * @throws ParseException
     * @throws AttributeNotAllowed
     */
    public static void updateAttribute(String xmlFileName, String nodePath, String attributeName, String attributeValue)
            throws IOException, AttributeAlreadyExist, InvalidNodePath, ParseException, AttributeNotAllowed {
        setAttribute(xmlFileName, nodePath, attributeName, attributeValue, false);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param attributeName
     * @param attributeValue
     * @param throwExceptionIfExist
     * @throws IOException
     * @throws AttributeAlreadyExist
     * @throws InvalidNodePath
     * @throws ParseException
     * @throws AttributeNotAllowed
     */
    public static void setAttribute(String xmlFileName, String nodePath, String attributeName, String attributeValue,
                                    boolean throwExceptionIfExist)
            throws IOException, AttributeAlreadyExist, InvalidNodePath, ParseException, AttributeNotAllowed {
        Document doc = load(xmlFileName);
        TreeNode node = doc.getNode(nodePath);

        if(node.allowAttributes()) {
            if(throwExceptionIfExist && ((node.getAttribute(attributeName)) != null)) {
                throw new AttributeAlreadyExist(attributeName);
            }

            node.setAttribute(attributeName, attributeValue);
        }
        else {
            throw new AttributeNotAllowed(node.toString());
        }

        doc.save(xmlFileName, true);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param attributeName
     * @throws IOException
     * @throws InvalidNodePath
     * @throws ParseException
     * @throws AttributeNotAllowed
     */
    public static void deleteAttribute(String xmlFileName, String nodePath, String attributeName)
            throws IOException, InvalidNodePath, ParseException, AttributeNotAllowed {
        Document doc = load(xmlFileName);
        TreeNode node = doc.getNode(nodePath);

        if(node.allowAttributes()) {
            node.removeAttribute(attributeName);
        }
        else {
            throw new AttributeNotAllowed(node.toString());
        }

        doc.save(xmlFileName, true);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param newElementName
     * @throws IOException
     * @throws InvalidNodePath
     * @throws ParseException
     */
    public static void updateElementName(String xmlFileName, String nodePath, String newElementName)
            throws IOException, InvalidNodePath, ParseException {
        Document doc = load(xmlFileName);
        TreeNode node = doc.getNode(nodePath);
        ((ElementNode) node).setElementName(newElementName);
        doc.save(xmlFileName, true);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param newContent
     * @throws IOException
     * @throws InvalidNodePath
     * @throws ParseException
     */
    public static void updateTextNode(String xmlFileName, String nodePath, String newContent)
            throws IOException, InvalidNodePath, ParseException {
        Document doc = load(xmlFileName);
        TreeNode node = doc.getNode(nodePath);
        ((TextNode) node).setContent(newContent);
        doc.save(xmlFileName, true);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param target
     * @param newContent
     * @throws IOException
     * @throws ParseException
     * @throws InvalidNodePath
     */
    public static void updateProcessingInstructionNode(String xmlFileName, String nodePath, String target,
                                                       String newContent)
            throws IOException, ParseException, InvalidNodePath {
        Document doc = load(xmlFileName);
        TreeNode node = doc.getNode(nodePath);
        ((ProcessingInstructionNode) node).setTarget(target);
        ((ProcessingInstructionNode) node).setContent(newContent);
        doc.save(xmlFileName, true);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param newContent
     * @throws IOException
     * @throws InvalidNodePath
     * @throws ParseException
     */
    public static void updateCommentNode(String xmlFileName, String nodePath, String newContent)
            throws IOException, InvalidNodePath, ParseException {
        updateTextNode(xmlFileName, nodePath, newContent);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param newContent
     * @throws IOException
     * @throws InvalidNodePath
     * @throws ParseException
     */
    public static void updateCDateNode(String xmlFileName, String nodePath, String newContent)
            throws IOException, InvalidNodePath, ParseException {
        updateTextNode(xmlFileName, nodePath, newContent);
    }

    /**
     *
     * @param xmlFileName
     * @param nodePath
     * @param newContent
     * @throws IOException
     * @throws InvalidNodePath
     * @throws ParseException
     */
    public static void updateDocTypeNode(String xmlFileName, String nodePath, String newContent)
            throws IOException, InvalidNodePath, ParseException {
        updateTextNode(xmlFileName, nodePath, newContent);
    }
}