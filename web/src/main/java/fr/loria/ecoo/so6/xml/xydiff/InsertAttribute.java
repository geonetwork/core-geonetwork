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
package fr.loria.ecoo.so6.xml.xydiff;

import fr.loria.ecoo.so6.xml.node.ElementNode;


public class InsertAttribute extends XMLCommand {
    private String name;
    private String value;
    private String oldPath;

    public InsertAttribute(String nodePath, String oldPath, String name, String value) {
        super(nodePath);
        //System.out.println("InsertAttribute " + nodePath);
        this.name = name;
        this.value = value;
        this.oldPath = oldPath;
        this.type = XMLCommand.INSERT_ATTRIBUTE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return ("InsertAttribute: name " + this.name + " value " + this.value + " path " + this.nodePath + " oldpath " + this.oldPath);
    }

    public ElementNode toXML() {
        try {
            ElementNode ai = new ElementNode("AttributeInserted");
            ai.setAttribute("pos", this.nodePath);
            if(this.oldPath != null) {
                ai.setAttribute("oldpos", this.oldPath);
            }
            ai.setAttribute("name", this.name);
            ai.setAttribute("value", this.value);

            return ai;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}