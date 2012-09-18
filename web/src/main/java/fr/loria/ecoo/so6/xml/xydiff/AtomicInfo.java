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

public class AtomicInfo {
    public final static byte NODEEVENT_NOP = 0;
    public final static byte NODEEVENT_DELETED = 1;
    public final static byte NODEEVENT_INSERTED = 2;
    public final static byte NODEEVENT_STRONGMOVE = 3;
    public final static byte NODEEVENT_WEAKMOVE = 4;
    public final static byte NODEEVENT_UPDATE_OLD = 5;
    public final static byte NODEEVENT_UPDATE_NEW = 6;
    int myID;
    int myPosition;
    int myParent;
    int firstChild;
    int nextSibling;
    byte myEvent;
    int myMatchID;
    double myWeight;
    Hash32 mySubtreeHash = new Hash32(); // identify the subtree rooted at node [hash32]
    Hash32 myOwnHash = new Hash32(); // identify node's label

    // *must* be equal to match any pair of nodes
    // 'null' if text node -> can match any text nodes
    // = hash(label, keyattrname, keyattrvalue) if ID attribute is here
    // So if KeyAttr is here, matching is either mandatory or forbidden
    boolean hasIdAttr;
    boolean isUnimportant;

    public AtomicInfo() {
        this.myID = - 1;
        this.myPosition = - 1;
        this.myParent = 0;
        this.firstChild = 0;
        this.nextSibling = 0;
        this.myEvent = NODEEVENT_NOP;
        this.myMatchID = 0;
        this.myWeight = - 995000.;
        this.hasIdAttr = false;
        this.isUnimportant = false;
    }
}
