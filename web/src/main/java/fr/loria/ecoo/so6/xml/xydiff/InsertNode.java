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
import fr.loria.ecoo.so6.xml.node.TreeNode;
import fr.loria.ecoo.so6.xml.util.XmlUtil;


public class InsertNode extends XMLCommand {
    private TreeNode node;
    private String oldPath;
    private boolean isMoved;
    private boolean isUpdated;

    public InsertNode(String nodePath, String oldPath, TreeNode node) {
        super(nodePath);
        //System.out.println("InsertNode " + nodePath);
        this.node = node;
        this.oldPath = oldPath;
        this.type = XMLCommand.INSERT_NODE;
        this.isMoved = false;
        this.isUpdated = false;
    }

    public void setIsMoved(boolean isMoved) {
        this.isMoved = isMoved;
    }

    public boolean getIsMoved() {
        return this.isMoved;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public void setIsUpdated(boolean isUpdated) {
        this.isUpdated = isUpdated;
    }

    public boolean getIsUpdated() {
        return this.isUpdated;
    }

    public TreeNode getNode() {
        return node;
    }

    public void setNode(TreeNode node) {
        this.node = node;
    }

    public String toString() {
        String s = ("InsertNode: " + this.node.toString() + " path " + this.nodePath+ " oldpath " + this.oldPath);

        if(this.isMoved) {
            s += " (move)";
        }

        if(this.isUpdated) {
            s += " (update)";
        }

        return s;
    }

    public ElementNode toXML() {
        try {
            ElementNode i = new ElementNode("Inserted");
            i.setAttribute("pos", this.nodePath);
            if(this.oldPath != null) {
                i.setAttribute("oldpos", this.oldPath);
            }

            if(this.isMoved) {
                i.setAttribute("move", "yes");
            }

            if(this.isUpdated) {
                i.setAttribute("update", "yes");
            }

            i.appendChild(XmlUtil.clone(node));

            return i;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
