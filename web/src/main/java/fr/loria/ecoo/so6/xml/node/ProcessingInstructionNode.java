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


public class ProcessingInstructionNode extends TextNode {
    private String target;

    public ProcessingInstructionNode(String target) {
        super();
        this.target = target;
    }

    public ProcessingInstructionNode() {
        super();
    }

    public ProcessingInstructionNode(String target, String data) {
        super(data);
        this.target = target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public void exportXML(Writer writer, boolean split)
            throws IOException {
        writer.write("<?");
        writer.write(target);

        //writer.write(" ");
        writer.write(content.toString());
        writer.write("?>");
        writer.flush();
    }

    public boolean equalsContent(Object obj) {
        if(obj instanceof ProcessingInstructionNode) {
            return ((ProcessingInstructionNode) obj).target.equals(target) && ((ProcessingInstructionNode) obj)
                    .content.toString().equals(content.toString());
        }
        else {
            return false;
        }
    }

    public Hash32 getHash32() {
        return new Hash32(this.target + this.getContent());
    }

    public double getWeight() {
        if((this.target + this.getContent()).length() == 0) {
            // prevent detect operations on an empty node
            return 0.00001;
        }

        return Math.log((this.target + this.getContent()).length() + 1);
    }
}
