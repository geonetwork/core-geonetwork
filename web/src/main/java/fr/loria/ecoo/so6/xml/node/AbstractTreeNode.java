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

import fr.loria.ecoo.so6.xml.exception.InvalidNodePath;
import fr.loria.ecoo.so6.xml.util.Base64;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;


public abstract class AbstractTreeNode implements TreeNode, Serializable {
    protected TreeNode parent;
    protected boolean allowAttributes;
    protected boolean allowChildren;
    protected String lastComputedPath;
    protected ArrayList children;
    protected Hashtable attributes;

    public AbstractTreeNode(boolean allowChildren, boolean allowAttributes) {
        children = new ArrayList();
        this.allowChildren = allowChildren;
        this.allowAttributes = allowAttributes;
        this.attributes = new Hashtable();
    }

    public void setAllowAttributes() {
        this.allowAttributes = true;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public ArrayList getChildren() {
        return children;
    }

    public TreeNode getChild(int childPos) {
        if(children.isEmpty()) {
            return null;
        }
        else {
            return (TreeNode) children.get(childPos);
        }
    }

    public void insertChild(int pos, TreeNode child) {
        if(! allowChildren) {
            throw new RuntimeException("Can't insert child to this node");
        }

        child.setParent(this);
        children.add(pos, child);
    }

    public boolean removeChild(TreeNode child) {
        TreeNode prev = child.getPreviousSibling();
        TreeNode next = child.getNextSibling();

        child.setParent(null);

        boolean result = children.remove(child);

        if(prev != null && next != null
                && isReallyTextNode(prev)
                && isReallyTextNode(next)) {
            String joinedContent = ((TextNode) prev).getContent() + ((TextNode) next).getContent();
            int firstTextNodePos = this.getChildPosition(prev);

            children.remove(prev);
            children.remove(next);

            TextNode joinedNode = new TextNode(joinedContent);
            this.insertChild(firstTextNodePos, joinedNode);
        }

        return result;
    }

    private boolean isReallyTextNode(TreeNode node) {
        boolean result = node instanceof TextNode &&
                ! (node instanceof CommentNode
                        || node instanceof CDataNode
                        || node instanceof ProcessingInstructionNode
                        || node instanceof DocTypeNode);

        return result;
    }

    public TreeNode removeChild(int pos) {
        TreeNode child = this.getChild(pos);

        this.removeChild(child);

        return child;
    }

    public void appendChild(TreeNode child) {
        if(! allowChildren) {
            throw new RuntimeException("Can't insert child to this node");
        }

        child.setParent(this);
        children.add(child);
    }

    public int getChildPosition(TreeNode child) {
        return children.indexOf(child);
    }

    public double getWeight() {
        double weight = 1.0;

        for(Iterator i = getChildren().iterator(); i.hasNext(); ) {
            weight += ((AbstractTreeNode) i.next()).getWeight();
        }

        //System.out.println("weight " + weight + " toString " +
        // this.toString());
        return weight;
    }

    public String getId() {
        return getHash32().toHexString();
    }

    public String getPath() {
        if(getParent() == null) {
            return "0";
        }

        return getParent().getPath() + ":" + getParent().getChildPosition(this);
    }

    public void computePath() {
        lastComputedPath = getPath();

        for(Iterator i = children.iterator(); i.hasNext(); ) {
            ((TreeNode) i.next()).computePath();
        }
    }

    public String getLastComputedPath() {
        return lastComputedPath;
    }

    public void toBase64(Writer osw) throws IOException {
        StringWriter writer = new StringWriter();
        exportXML(writer, false);
        writer.close();
        osw.write(Base64.encodeBytes(writer.toString().getBytes("UTF-8")));
    }

    public boolean equalsContent(Object obj) {
        if(obj instanceof TreeNode) {
            TreeNode objCompare = (TreeNode) obj;
            int size = getChildren().size();

            if(objCompare.getChildren().size() != size) {
                return false;
            }

            for(int i = 0; i < size; i++) {
                if(! objCompare.getChild(i).equalsContent(getChild(i))) {
                    return false;
                }
            }

            return true;
        }
        else {
            return false;
        }
    }

    public TreeNode getNode(String nodePath) throws InvalidNodePath {
        TreeNode root = this;
        int[] path = convertPath(nodePath);

        while(root.getParent() != null) {
            root = root.getParent();
        }

        TreeNode node = root;

        try {
            for(int i = 1; i < path.length; i++) {
                node = node.getChild(path[i]);
            }
        }
        catch(RuntimeException e) {
            throw new InvalidNodePath(e.getMessage());
        }

        return node;
    }

    public TreeNode getNextSibling() {
        try {
            int childPos = this.getParent().getChildPosition(this);

            return this.getParent().getChild(childPos + 1);
        }
        catch(RuntimeException e) {
            return null;
        }
    }

    public TreeNode getPreviousSibling() {
        try {
            return this.getParent().getChild(this.getParent().getChildPosition(this) - 1);
        }
        catch(RuntimeException e) {
            return null;
        }
    }

    // Node path manipulation
    public static int[] convertPath(String path) {
        String[] split = path.split(":");
        int[] result = new int[split.length];

        for(int i = 0; i < result.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }

        return result;
    }

    public boolean hasChildren() {
        if(this.getChildren().isEmpty()) {
            return false;
        }
        else {
            return true;
        }
    }

    public TreeNode getFirstChild() {
        return this.getChild(0);
    }

    public TreeNode getLastChild() {
        ArrayList list = this.getChildren();
        int nbChildren = list.size();

        return this.getChild(nbChildren - 1);
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public Hashtable getAttributes() {
        return this.attributes;
    }

    public String getAttribute(String name) {
        return (String) this.attributes.get(name);
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public boolean hasAttributes() {
        if(this.attributes.isEmpty()) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean allowAttributes() {
        return allowAttributes;
    }

    public boolean allowChildren() {
        return allowChildren;
    }

    public String toString() {
        StringWriter writer = new StringWriter();

        try {
            exportXML(writer, false);
            writer.close();

            return writer.toString();
        }
        catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "error";
    }
}
