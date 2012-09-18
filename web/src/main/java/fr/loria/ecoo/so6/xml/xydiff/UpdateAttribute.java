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


public class UpdateAttribute extends XMLCommand {
    private String attributeName;
    private String oldValue;
    private String newValue;
    private String oldPath;

    public UpdateAttribute(String nodePath, String oldPath, String name, String oldValue, String value) {
        super(nodePath);
        //System.out.println("UpdateAttribute " + nodePath);
        this.attributeName = name;
        this.oldValue = oldValue;
        this.newValue = value;
        this.oldPath = oldPath;
        this.type = XMLCommand.UPDATE_ATTRIBUTE;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String toString() {
        return ("UpdateAttribute: name " + attributeName + " path " + this.nodePath + " from " + oldValue + " to " +
                newValue + " oldpath " + this.oldPath);
    }

    public ElementNode toXML() {
        ElementNode ua = new ElementNode("AttributeUpdated");
        ua.setAttribute("pos", nodePath);
        if(this.oldPath != null) {
            ua.setAttribute("oldpos", oldPath);
        }
        ua.setAttribute("name", attributeName);
        ua.setAttribute("ov", oldValue);
        ua.setAttribute("nv", newValue);

        return ua;
    }
}
